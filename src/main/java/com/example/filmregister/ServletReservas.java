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

            default:
                response.sendRedirect("index.jsp");
                break;
        }
    }

    private void CreoReserva(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        EntityManager em = entityManagerFactory.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            String idPeliculaStr = request.getParameter("idPelicula");
            String filaStr = request.getParameter("nFila");
            String asientoStr = request.getParameter("nAsiento");

            if (idPeliculaStr == null || filaStr == null || asientoStr == null) {
                request.getSession().setAttribute("errorReserva", "Faltan datos de la reserva.");
                response.sendRedirect("reservarPelicula.jsp?id=" + idPeliculaStr);
                return;
            }

            int peliculaId = Integer.parseInt(idPeliculaStr);
            int fila = Integer.parseInt(filaStr);
            int asiento = Integer.parseInt(asientoStr);

            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("usuario") == null) {
                request.getSession().setAttribute("errorReserva", "Debes iniciar sesión.");
                response.sendRedirect("login.jsp");  // o a donde deba ir
                return;
            }

            // Obtener usuario desde sesión (como String)
            String nombreUsuario = (String) session.getAttribute("usuario");

            // Buscar el usuario en base de datos
            Usuario usuario = (Usuario) em.createQuery("SELECT u FROM Usuario u WHERE u.usuario = :usuario")
                    .setParameter("usuario", nombreUsuario)
                    .getSingleResult();

            // Verificar si el asiento ya está ocupado
            Long ocupados = (Long) em.createQuery(
                            "SELECT COUNT(r) FROM Reserva r WHERE r.pelicula.id = :peliculaId AND r.nFila = :fila AND r.nAsiento = :asiento")
                    .setParameter("peliculaId", peliculaId)
                    .setParameter("fila", fila)
                    .setParameter("asiento", asiento)
                    .getSingleResult();

            if (ocupados != null && ocupados > 0) {
                session.setAttribute("errorReserva", "El asiento Fila " + fila + ", Asiento " + asiento + " ya está reservado.");
                response.sendRedirect("reservarPelicula.jsp?id=" + peliculaId);
                return;
            }

            Pelicula pelicula = em.find(Pelicula.class, peliculaId);
            if (pelicula == null) {
                session.setAttribute("errorReserva", "Película no encontrada.");
                response.sendRedirect("reservarPelicula.jsp");
                return;
            }

            // Crear reserva
            Reserva nuevaReserva = new Reserva();
            nuevaReserva.setUsuario(usuario);
            nuevaReserva.setPelicula(pelicula);
            nuevaReserva.setFechaReserva(LocalDate.now());
            nuevaReserva.setEstado("ACTIVA");
            nuevaReserva.setnFila(fila);
            nuevaReserva.setnAsiento(asiento);

            tx.begin();
            em.persist(nuevaReserva);
            tx.commit();

            // Agregar a sesión para mostrar luego si es necesario
            List<Reserva> reservas = (List<Reserva>) session.getAttribute("reservas");
            if (reservas == null) reservas = new ArrayList<>();
            reservas.add(nuevaReserva);
            session.setAttribute("reservas", reservas);

            response.sendRedirect("reservaRealizada_PDF.jsp?id=" + nuevaReserva.getId());

        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            request.getSession().setAttribute("errorReserva", "Error al procesar la reserva.");
            response.sendRedirect("reservarPelicula.jsp?id=" + request.getParameter("idPelicula"));
        } finally {
            em.close();
        }
    }

    private void CreoTicket(HttpServletRequest request, HttpServletResponse response) throws IOException {
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

        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            Reserva reserva = em.find(Reserva.class, idReserva);
            if (reserva == null) {
                response.sendRedirect("error.jsp");
                return;
            }

            Pelicula pelicula = reserva.getPelicula();
            Usuario usuario = reserva.getUsuario();

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=ticket_reserva_" + idReserva + ".pdf");

            Document documento = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(documento, response.getOutputStream());
            documento.open();

            Font fuenteTitulo = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD);
            Font fuenteSubtitulo = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
            Font fuenteTexto = new Font(Font.FontFamily.HELVETICA, 12);

            Paragraph titulo = new Paragraph("Ticket de Reserva", fuenteTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            documento.add(titulo);
            documento.add(new Paragraph("\n"));

            PdfPTable tablaSuperior = new PdfPTable(2);
            tablaSuperior.setWidthPercentage(100);
            tablaSuperior.setSpacingBefore(10f);
            tablaSuperior.setWidths(new float[]{1.2f, 1.8f});

            // Celda detalles reserva con fila y asiento incluidos
            PdfPCell celdaDetallesReserva = new PdfPCell();
            celdaDetallesReserva.setBorderWidth(1);
            celdaDetallesReserva.addElement(new Paragraph("Detalles de la Reserva", fuenteSubtitulo));
            celdaDetallesReserva.addElement(new Paragraph("ID de Reserva: " + reserva.getId(), fuenteTexto));
            celdaDetallesReserva.addElement(new Paragraph("Usuario: " + usuario.getUsuario(), fuenteTexto));
            celdaDetallesReserva.addElement(new Paragraph("Fila: " + reserva.getnFila(), fuenteTexto));
            celdaDetallesReserva.addElement(new Paragraph("Asiento: " + reserva.getnAsiento(), fuenteTexto));
            tablaSuperior.addCell(celdaDetallesReserva);

            PdfPCell celdaImagen = new PdfPCell();
            celdaImagen.setBorderWidth(1);
            celdaImagen.addElement(new Paragraph("Película Reservada", fuenteSubtitulo));
            celdaImagen.addElement(new Paragraph("Título: " + pelicula.getTitulo(), fuenteTexto));

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
            documento.add(tablaSuperior);

            documento.add(new Paragraph("\n"));

            PdfPTable tablaDetalles = new PdfPTable(2);
            tablaDetalles.setWidthPercentage(100);
            tablaDetalles.setWidths(new float[]{1, 1});

            PdfPCell celdaDuracion = new PdfPCell(new Paragraph("⏳ Duración: " + pelicula.getDuracion() + " minutos", fuenteTexto));
            celdaDuracion.setBorderWidth(1);
            celdaDuracion.setPadding(10);
            celdaDuracion.setHorizontalAlignment(Element.ALIGN_CENTER);
            tablaDetalles.addCell(celdaDuracion);

            PdfPCell celdaIdioma = new PdfPCell(new Paragraph("🌍 Idioma: " + pelicula.getIdioma(), fuenteTexto));
            celdaIdioma.setBorderWidth(1);
            celdaIdioma.setPadding(10);
            celdaIdioma.setHorizontalAlignment(Element.ALIGN_CENTER);
            tablaDetalles.addCell(celdaIdioma);

            documento.add(tablaDetalles);

            documento.add(new Paragraph("\n"));

            Paragraph mensajeFinal = new Paragraph("Gracias por reservar con nosotros. ¡Disfruta la película! 🍿", fuenteSubtitulo);
            mensajeFinal.setAlignment(Element.ALIGN_CENTER);
            documento.add(mensajeFinal);

            documento.close();

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("error.jsp");
        } finally {
            em.close();
        }
    }


    private void BorroReserva(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String idReservaStr = request.getParameter("idReserva");

        HttpSession session = request.getSession(false);
        if (idReservaStr == null || session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect("error.jsp");
            return;
        }

        Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");
        EntityManager em = entityManagerFactory.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            int idReserva = Integer.parseInt(idReservaStr);
            tx.begin();

            Reserva reserva = em.find(Reserva.class, idReserva);

            if (reserva == null || !reserva.getUsuario().getUsuario().equals(usuarioSesion.getUsuario())) {
                tx.rollback();
                response.sendRedirect("error.jsp");
                return;
            }

            // Cambiar estado a "CANCELADA"
            reserva.setEstado("CANCELADA");
            em.merge(reserva);

            tx.commit();

            // Actualizar reservas en sesión
            List<Reserva> reservasActualizadas = em.createQuery(
                            "SELECT r FROM Reserva r WHERE r.usuario = :usuario", Reserva.class)
                    .setParameter("usuario", usuarioSesion)
                    .getResultList();

            session.setAttribute("reservas", reservasActualizadas);
            response.sendRedirect("verMisReservas.jsp");
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            response.sendRedirect("error.jsp");
        } finally {
            em.close();
        }
    }




    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("cancelarReserva".equals(action)) {
            BorroReserva(request, response);
        } else {
            response.setContentType("text/plain; charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // Código HTTP 400
            response.getWriter().println("Acción no válida: " + action);
        }

        // Verificación de sesión y usuario logueado
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect("index.jsp");
            return;
        }

        // Obtener el objeto Usuario de la sesión
        Object usuarioObj = session.getAttribute("usuario");
        if (!(usuarioObj instanceof Usuario)) {
            response.sendRedirect("error.jsp");
            return;
        }

        Usuario usuario = (Usuario) usuarioObj;

        // Obtener reservas del usuario desde la base de datos
        EntityManager em = entityManagerFactory.createEntityManager();

        try {
            List<Reserva> reservas = em.createQuery(
                            "SELECT r FROM Reserva r WHERE r.usuario = :usuario", Reserva.class)
                    .setParameter("usuario", usuario)
                    .getResultList();

            // Guardar la lista en sesión para que el JSP la use
            session.setAttribute("reservas", reservas);

            // Mostrar la página de reservas
            request.getRequestDispatcher("/verMisReservas.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("error.jsp");
        } finally {
            em.close();
        }
    }


    @Override
    public void destroy() {
        if (entityManagerFactory != null) {
            entityManagerFactory.close();
        }
    }
}
