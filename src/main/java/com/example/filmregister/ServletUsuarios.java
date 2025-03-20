package com.example.filmregister;

import jakarta.persistence.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import logica.Usuario;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import io.github.cdimascio.dotenv.Dotenv;
import org.mindrot.jbcrypt.BCrypt;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

@WebServlet("/servletUsuarios")
public class ServletUsuarios extends HttpServlet {

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

            try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
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
            case "login":
                login(request, response);
                break;
            case "RegistroUsuario":
                registro(request, response);
                break;
            default:
                response.sendRedirect("index.jsp");
                break;
        }
    }

    private void login(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // Obtener la sesión (sin crear una nueva todavía)
        HttpSession session = request.getSession();

        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            session.setAttribute("error", "Por favor, completa todos los campos.");
            response.sendRedirect("index.jsp");
            return;
        }

        EntityManager em = entityManagerFactory.createEntityManager();

        try {
            TypedQuery<Usuario> query = em.createQuery("SELECT u FROM Usuario u WHERE u.usuario = :username", Usuario.class);
            query.setParameter("username", username);
            List<Usuario> result = query.getResultList();

            if (result.isEmpty()) {
                session.setAttribute("error", "Usuario o contraseña incorrectos.");
                response.sendRedirect("index.jsp");
                return;
            }

            Usuario usuario = result.get(0);
            System.out.println("Usuario encontrado: " + usuario.getNombre());

            // Comparar la contraseña directamente sin BCrypt
            if (!password.equals(usuario.getPassword())) {
                session.setAttribute("error", "Usuario o contraseña incorrectos.");
                response.sendRedirect("index.jsp");
                return;
            }

            // Cerrar sesión anterior (si existe) antes de crear una nueva
            session.invalidate();
            session = request.getSession(true); // Crear nueva sesión

            session.setAttribute("usuario", usuario.getUsuario());
            session.setMaxInactiveInterval(86400); // 24 horas

            // Crear cookie de sesión
            Cookie sessionCookie = new Cookie("JSESSIONID", session.getId());
            sessionCookie.setMaxAge(86400); // 24 horas
            sessionCookie.setHttpOnly(true);
            sessionCookie.setPath("/");
            response.addCookie(sessionCookie);

            response.sendRedirect("servletPeliculas");
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("error", "Se ha producido un error al procesar la solicitud.");
            response.sendRedirect("index.jsp");
        } finally {
            em.close();
        }
    }

    private void registro(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        EntityManager em = entityManagerFactory.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            tx.begin();

            String dni = request.getParameter("dni");

            List<Usuario> usuarios = em.createQuery("SELECT u FROM Usuario u WHERE u.dni = :dni", Usuario.class)
                    .setParameter("dni", dni)
                    .getResultList();

            if (!usuarios.isEmpty()) {
                response.getWriter().write("{\"success\": false, \"message\": \"El usuario ya existe.\"}");
                return;
            }

            // Validar los campos de entrada
            String nombre = request.getParameter("nombre");
            String apellidos = request.getParameter("apellidos");
            String email = request.getParameter("email");
            String usuario = request.getParameter("usuario");
            String contraseña = request.getParameter("contraseña");

            if (nombre == null || apellidos == null || email == null || usuario == null || contraseña == null ||
                    nombre.isEmpty() || apellidos.isEmpty() || email.isEmpty() || usuario.isEmpty() || contraseña.isEmpty()) {
                response.getWriter().write("{\"success\": false, \"message\": \"Todos los campos son obligatorios.\"}");
                return;
            }

            // Verificar si el correo electrónico ya está registrado
            List<Usuario> usuarioConEmail = em.createQuery("SELECT u FROM Usuario u WHERE u.email = :email", Usuario.class)
                    .setParameter("email", email)
                    .getResultList();
            if (!usuarioConEmail.isEmpty()) {
                response.getWriter().write("{\"success\": false, \"message\": \"El correo electrónico ya está registrado.\"}");
                return;
            }

            Usuario usuarioNuevo = new Usuario();
            usuarioNuevo.setDni(dni);
            usuarioNuevo.setNombre(nombre);
            usuarioNuevo.setApellidos(apellidos);
            usuarioNuevo.setEmail(email);
            usuarioNuevo.setUsuario(usuario);
            usuarioNuevo.setPassword(contraseña); // 🔥 Se guarda la contraseña sin cifrar

            em.persist(usuarioNuevo);
            tx.commit();

            // Enviar el correo antes de dar el mensaje de éxito
            Correo(request, response);

            response.getWriter().write("{\"success\": true, \"message\": \"Usuario registrado con éxito\", \"redirect\": \"index.jsp\"}");

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            response.getWriter().write("{\"success\": false, \"message\": \"Error al registrar usuario.\"}");
            throw new ServletException("Error al registrar usuario", e);
        } finally {
            em.close();
        }
    }

    private void Correo(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String emailDestino = request.getParameter("email");

        final String remitente = "joseangelcabezafp@gmail.com";
        final String clave = "uaod xkrq fmgp ydqr";

        // Configuración del servidor SMTP
        Properties propiedades = new Properties();
        propiedades.put("mail.smtp.host", "smtp.gmail.com");
        propiedades.put("mail.smtp.port", "587");
        propiedades.put("mail.smtp.auth", "true");
        propiedades.put("mail.smtp.starttls.enable", "true");

        // Autenticación
        Session sesion = Session.getInstance(propiedades, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(remitente, clave);
            }
        });

        try {
            // Crear el mensaje con HTML
            Message mensaje = new MimeMessage(sesion);
            mensaje.setFrom(new InternetAddress(remitente));
            mensaje.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailDestino));
            mensaje.setSubject("🎉 Registro Exitoso en FilmRegister");

            String htmlContent = "<html>" +
                    "<body style='font-family: Arial, sans-serif;'>" +
                    "<h2 style='color: #007bff;'>¡Bienvenido a FilmRegister! 🎬</h2>" +
                    "<p>Hola <b>" + request.getParameter("nombre") + "</b>,</p>" +
                    "<p>Tu registro se ha completado con éxito. Ahora puedes acceder a tu cuenta y disfrutar de nuestra plataforma.</p>" +
                    "<br>" +
                    "<p style='color: #555;'>Si no solicitaste este registro, ignora este mensaje.</p>" +
                    "<hr>" +
                    "<p style='font-size: 12px; color: #777;'>Este es un mensaje automático, por favor no respondas a este correo.</p>" +
                    "</body>" +
                    "</html>";

            mensaje.setContent(htmlContent, "text/html; charset=utf-8");

            // Enviar el correo
            Transport.send(mensaje);
            System.out.println("Correo enviado correctamente.");

        } catch (MessagingException e) {
            e.printStackTrace();
            response.getWriter().write("{\"success\": false, \"message\": \"Error al enviar el correo.\"}");
        }
    }



    @Override
    public void destroy() {
        if (entityManagerFactory != null) {
            entityManagerFactory.close();
        }
    }
}

