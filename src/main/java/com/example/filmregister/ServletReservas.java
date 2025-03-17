package com.example.filmregister;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.*;
import logica.Pelicula;
import logica.Usuario;
import org.mariadb.jdbc.Connection;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import logica.Reserva;
import jakarta.servlet.annotation.WebServlet;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.PageSize;


@WebServlet("/servletReservas")
public class ServletReservas extends HttpServlet{

    private EntityManagerFactory entityManagerFactory;

    @Override
    public void init() throws ServletException {
        Map<String, String> properties = new HashMap<>();
        Dotenv dotenv = Dotenv.load();

        try {
            Class.forName("org.mariadb.jdbc.Driver");

            String dbUrl = dotenv.get("DB_URL");
            String dbUser = dotenv.get("DB_USER");
            String dbPassword = dotenv.get("DB_PASSWORD");

            if (dbUrl == null || dbUser == null || dbPassword == null) {
                throw new ServletException("Las variables de entorno no están configuradas correctamente.");
            }

            try (Connection connection = (Connection) DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
                System.out.println("Conexión exitosa a la base de datos.");
            } catch (SQLException ex) {
                System.err.println("Error al conectar a la base de datos: " + ex.getMessage());
                throw new ServletException("No se pudo establecer la conexión con la base de datos", ex);
            }

            properties.put("jakarta.persistence.jdbc.url", dbUrl);
            properties.put("jakarta.persistence.jdbc.user", dbUser);
            properties.put("jakarta.persistence.jdbc.password", dbPassword);
            properties.put("jakarta.persistence.jdbc.driver", "org.mariadb.jdbc.Driver");

            entityManagerFactory = Persistence.createEntityManagerFactory("miUnidadDePersistencia", properties);

        } catch (Exception e) {
            System.err.println("Error al inicializar el EntityManagerFactory: " + e.getMessage());
            e.printStackTrace();
            throw new ServletException("Error al inicializar el EntityManagerFactory", e);
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        if (action == null) {
            response.sendRedirect("index.jsp");
            return;
        }

        switch (action) {
            case "CreoticketPDF":
                CreoTicket(request,response);
            case "realizarReserva":
                CreoReserva(request, response);
                break;
            case "borrarReserva":
                BorroReserva(request, response);
                break;
            default:
                response.sendRedirect("index.jsp");
                break;
        }
    }

    private void CreoReserva(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        EntityManager em = entityManagerFactory.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            // Obtener parámetros del formulario
            String nombre = request.getParameter("nombre");
            String apellidos = request.getParameter("apellidos");
            String usuario = request.getParameter("usuario");
            String idPeliculaStr = request.getParameter("idPelicula");



            int peliculaId;
            try {
                peliculaId = Integer.parseInt(idPeliculaStr);
            } catch (NumberFormatException e) {
                response.sendRedirect("error.jsp");
                return;
            }

            // Buscar usuario en la base de datos
            Usuario usuarioEncontrado = null;
            try {
                usuarioEncontrado = (Usuario) em.createQuery("SELECT u FROM Usuario u WHERE u.usuario = :usuario")
                        .setParameter("usuario", usuario)
                        .getSingleResult();
            } catch (NoResultException e) {
                response.sendRedirect("error.jsp");
                return;
            }

            // Obtener la fecha actual
            LocalDate fechaReserva = LocalDate.now();

            // Crear reserva
            Reserva nuevaReserva = new Reserva();
            nuevaReserva.setUsuario(usuarioEncontrado);
            nuevaReserva.setPelicula(em.find(Pelicula.class, peliculaId));
            nuevaReserva.setFechaReserva(fechaReserva);
            nuevaReserva.setEstado("ACTIVA");

            // Guardar en la base de datos
            tx.begin();
            em.persist(nuevaReserva);
            tx.commit();

            // Guardar la reserva en la sesión para mostrarla en el JSP
            HttpSession session = request.getSession();
            List<Reserva> reservas = (List<Reserva>) session.getAttribute("reservas");
            if (reservas == null) {
                reservas = new ArrayList<>();
            }
            reservas.add(nuevaReserva);
            session.setAttribute("reservas", reservas);

            // Redirigir a la página de confirmación (PDF)
            response.sendRedirect("reservaRealizada_PDF.jsp?id=" + nuevaReserva.getId());

        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            response.sendRedirect("error.jsp");
        } finally {
            em.close();
        }
    }

    private void CreoTicket(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Obtener ID de la reserva
        String idReservaStr = request.getParameter("idReserva");

        if (idReservaStr == null || idReservaStr.isEmpty()) {
            response.sendRedirect("error.jsp");
            return;
        }

        int idReserva;
        try {
            idReserva = Integer.parseInt(idReservaStr);
        } catch (NumberFormatException e) {
            response.sendRedirect("error.jsp");
            return;
        }

        // Buscar reserva en la base de datos
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            Reserva reserva = em.find(Reserva.class, idReserva);
            if (reserva == null) {
                response.sendRedirect("error.jsp");
                return;
            }

            // Obtener datos de la película y usuario
            Pelicula pelicula = reserva.getPelicula();
            Usuario usuario = reserva.getUsuario();

            // Configurar respuesta como PDF
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=ticket_reserva_" + idReserva + ".pdf");

            // Crear documento con orientación horizontal
            Document documento = new Document(PageSize.A4.rotate()); // A4 en horizontal
            PdfWriter.getInstance(documento, response.getOutputStream());
            documento.open();

            // Estilos de fuente
            Font fuenteTitulo = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD);
            Font fuenteSubtitulo = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
            Font fuenteTexto = new Font(Font.FontFamily.HELVETICA, 12);

            // Título principal centrado
            Paragraph titulo = new Paragraph("Ticket de Reserva", fuenteTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            documento.add(titulo);
            documento.add(new Paragraph("\n"));

            // Crear tabla de 2 columnas con bordes
            PdfPTable tablaSuperior = new PdfPTable(2);
            tablaSuperior.setWidthPercentage(100);
            tablaSuperior.setSpacingBefore(10f);
            tablaSuperior.setWidths(new float[]{1.2f, 1.8f}); // Proporción de columnas

            // **Columna Izquierda (Detalles de Reserva)**
            PdfPCell celdaDetallesReserva = new PdfPCell();
            celdaDetallesReserva.setBorderWidth(1); // Borde en la celda
            celdaDetallesReserva.addElement(new Paragraph("Detalles de la Reserva", fuenteSubtitulo));
            celdaDetallesReserva.addElement(new Paragraph("ID de Reserva: " + reserva.getId(), fuenteTexto));
            celdaDetallesReserva.addElement(new Paragraph("Usuario: " + usuario.getUsuario(), fuenteTexto));
            tablaSuperior.addCell(celdaDetallesReserva);

            // **Columna Derecha (Título + Imagen)**
            PdfPCell celdaImagen = new PdfPCell();
            celdaImagen.setBorderWidth(1);
            celdaImagen.addElement(new Paragraph("Película Reservada", fuenteSubtitulo));
            celdaImagen.addElement(new Paragraph("Título: " + pelicula.getTitulo(), fuenteTexto));

            // Si hay imagen, agregarla; si no, mostrar mensaje
            if (pelicula.getImagen() != null) {
                try {
                    Image imagen = Image.getInstance(pelicula.getImagen());
                    imagen.scaleToFit(180, 180);
                    imagen.setAlignment(Element.ALIGN_CENTER);
                    celdaImagen.addElement(imagen);
                } catch (Exception e) {
                    celdaImagen.addElement(new Paragraph("Imagen no disponible", fuenteTexto));
                }
            } else {
                celdaImagen.addElement(new Paragraph("Imagen no disponible", fuenteTexto));
            }

            tablaSuperior.addCell(celdaImagen);
            documento.add(tablaSuperior); // Agregar la tabla superior al documento

            // Espacio entre secciones
            documento.add(new Paragraph("\n"));

            // **Tabla inferior con detalles adicionales de la película**
            PdfPTable tablaDetalles = new PdfPTable(2);
            tablaDetalles.setWidthPercentage(100);
            tablaDetalles.setWidths(new float[]{1, 1}); // Dos columnas iguales

            // Celda de Duración
            PdfPCell celdaDuracion = new PdfPCell(new Paragraph("⏳ Duración: " + pelicula.getDuracion() + " minutos", fuenteTexto));
            celdaDuracion.setBorderWidth(1);
            celdaDuracion.setPadding(10);
            celdaDuracion.setHorizontalAlignment(Element.ALIGN_CENTER);
            tablaDetalles.addCell(celdaDuracion);

            // Celda de Idioma
            PdfPCell celdaIdioma = new PdfPCell(new Paragraph("🌍 Idioma: " + pelicula.getIdioma(), fuenteTexto));
            celdaIdioma.setBorderWidth(1);
            celdaIdioma.setPadding(10);
            celdaIdioma.setHorizontalAlignment(Element.ALIGN_CENTER);
            tablaDetalles.addCell(celdaIdioma);

            documento.add(tablaDetalles);

            // Espacio antes del mensaje final
            documento.add(new Paragraph("\n"));

            // Mensaje de agradecimiento
            Paragraph mensajeFinal = new Paragraph("Gracias por reservar con nosotros. ¡Disfruta la película! 🍿", fuenteSubtitulo);
            mensajeFinal.setAlignment(Element.ALIGN_CENTER);
            documento.add(mensajeFinal);

            // Cerrar el documento
            documento.close();

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("error.jsp");
        } finally {
            em.close();
        }
    }

    private void BorroReserva(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false); // No crear sesión si no existe

        // Verificar si el usuario está autenticado
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect("index.jsp");
            return;
        }

        // Obtener la lista de películas desde la base de datos
        EntityManager em = entityManagerFactory.createEntityManager();
        List<Reserva> reservas = null;

        try {
            reservas = em.createQuery("SELECT p FROM Reserva p", Reserva.class).getResultList();
            System.out.println("Número de reservas recuperadas: " + reservas.size());

            // Guardar en sesión en lugar de en request
            session.setAttribute("reservas", reservas);

        } catch (Exception e) {
            System.err.println("Error al recuperar las reservas: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("error", "Se ha producido un error al recuperar las reservas.");
            request.getRequestDispatcher("/verMisReservas.jsp").forward(request, response);
            return;
        } finally {
            em.close();
        }

        // Redirigir al JSP
        request.getRequestDispatcher("/verMisReservas.jsp").forward(request, response);
    }

    @Override
    public void destroy() {
        if (entityManagerFactory != null) {
            entityManagerFactory.close();
        }
    }
}
