package com.example.filmregister;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mariadb.jdbc.Connection;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import logica.Pelicula;

@WebServlet("/servletPeliculas")
public class ServletPeliculas extends HttpServlet {

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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        // Compruebp que hay una sesion
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect("index.jsp");
            return;
        }

        // Obtengo la lista de peliculas de la base de datos
        EntityManager em = entityManagerFactory.createEntityManager();
        List<Pelicula> peliculas = null;

        try {
            peliculas = em.createQuery("SELECT p FROM Pelicula p", Pelicula.class).getResultList();
            System.out.println("Número de películas recuperadas: " + peliculas.size());

            // Guardo en la sesion la respuesta de la consulta
            session.setAttribute("peliculas", peliculas);

        } catch (Exception e) {
            System.err.println("Error al recuperar las películas: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("error", "Se ha producido un error al recuperar las películas.");
            request.getRequestDispatcher("/index.jsp").forward(request, response);
            return;
        } finally {
            em.close();
        }

        // Envio la respuesta a mi index.jsp para listar las peliculas
        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }


    @Override
    public void destroy() {
        if (entityManagerFactory != null) {
            entityManagerFactory.close();
        }
    }
}
