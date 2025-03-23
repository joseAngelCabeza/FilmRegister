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
                break;
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
            // Obtengo los parámetros del formulario
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

            // Busco usuario en la base de datos
            Usuario usuarioEncontrado = null;
            try {
                usuarioEncontrado = (Usuario) em.createQuery("SELECT u FROM Usuario u WHERE u.usuario = :usuario")
                        .setParameter("usuario", usuario)
                        .getSingleResult();
            } catch (NoResultException e) {
                response.sendRedirect("error.jsp");
                return;
            }

            // Obtengo la fecha del dia para la Reserva
            LocalDate fechaReserva = LocalDate.now();

            // Creo la reserva
            Reserva nuevaReserva = new Reserva();
            nuevaReserva.setUsuario(usuarioEncontrado);
            nuevaReserva.setPelicula(em.find(Pelicula.class, peliculaId));
            nuevaReserva.setFechaReserva(fechaReserva);
            nuevaReserva.setEstado("ACTIVA");

            // Guardo en la base de datos
            tx.begin();
            em.persist(nuevaReserva);
            tx.commit();

            // Tambien guardo la reserva en la sesión para mostrarla en el JSP
            HttpSession session = request.getSession();
            List<Reserva> reservas = (List<Reserva>) session.getAttribute("reservas");
            if (reservas == null) {
                reservas = new ArrayList<>();
            }
            reservas.add(nuevaReserva);
            session.setAttribute("reservas", reservas);

            // Redirigo a la interfaz de informacion de la reserva y para imprimir el ticket
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
        // Obtengo el id de la reserva para poder buscarla
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

        // Busco la reserva en la base de datos
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            Reserva reserva = em.find(Reserva.class, idReserva);
            if (reserva == null) {
                response.sendRedirect("error.jsp");
                return;
            }

            // Obtengo los datos de la pelicula y el usuario para introducirlos al ticket
            Pelicula pelicula = reserva.getPelicula();
            Usuario usuario = reserva.getUsuario();

            // Configuro la respuesta como PDF
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=ticket_reserva_" + idReserva + ".pdf");


            // Creo el documento con orientación horizontal
            Document documento = new Document(PageSize.A4.rotate()); // A4 en horizontal
            PdfWriter.getInstance(documento, response.getOutputStream());
            documento.open();

            // Defino los estilos
            Font fuenteTitulo = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD);
            Font fuenteSubtitulo = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
            Font fuenteTexto = new Font(Font.FontFamily.HELVETICA, 12);

            // Creo el titulo del ticket
            Paragraph titulo = new Paragraph("Ticket de Reserva", fuenteTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            documento.add(titulo);
            documento.add(new Paragraph("\n"));

            // Creo dos columnas de datos para el ticket
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

            // **Columna Derecha (Título y Imagen)**
            PdfPCell celdaImagen = new PdfPCell();
            celdaImagen.setBorderWidth(1);
            celdaImagen.addElement(new Paragraph("Película Reservada", fuenteSubtitulo));
            celdaImagen.addElement(new Paragraph("Título: " + pelicula.getTitulo(), fuenteTexto));

            // Si existe la imagen la meto sino muestro un error
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

            // Creo un espacio entre datos
            documento.add(new Paragraph("\n"));

            // **Introduzco una fila inferior con detalles adicionales de la película**
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

            documento.add(new Paragraph("\n"));

            // Creo un mensaje de agradecimiento
            Paragraph mensajeFinal = new Paragraph("Gracias por reservar con nosotros. ¡Disfruta la película! 🍿", fuenteSubtitulo);
            mensajeFinal.setAlignment(Element.ALIGN_CENTER);
            documento.add(mensajeFinal);

            // Cierro el documento
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


        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect("index.jsp");
            return;
        }


        Usuario usuario = (Usuario) session.getAttribute("usuario");

        // Obtengo la lista de reservas desde la base de datos para este usuario
        EntityManager em = entityManagerFactory.createEntityManager();
        List<Reserva> reservas = null;

        try {
            // Busco las reservas del usuario autenticado
            reservas = em.createQuery("SELECT r FROM Reserva r WHERE r.usuario = :usuario", Reserva.class)
                    .setParameter("usuario", usuario)
                    .getResultList();
            System.out.println("Número de reservas recuperadas: " + reservas.size());

            // Guardar las reservas en la sesión
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

        // Redirigo al JSP para mostrar las reservas
        request.getRequestDispatcher("/verMisReservas.jsp").forward(request, response);
    }


    @Override
    public void destroy() {
        if (entityManagerFactory != null) {
            entityManagerFactory.close();
        }
    }
}
