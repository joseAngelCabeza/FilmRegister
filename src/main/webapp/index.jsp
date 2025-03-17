<%@ page import="jakarta.servlet.http.HttpSession" %>
<%@ page import="java.util.List" %>
<%@ page import="logica.Pelicula" %>
<%@ page import="java.util.Base64" %>
<%@ page contentType="text/html; charset=UTF-8" %>


<%
    HttpSession sessionObj = request.getSession();
    boolean autenticado = (sessionObj.getAttribute("usuario") != null);
    String loginError = (String) sessionObj.getAttribute("error");

    // Limpiar el mensaje de error después de mostrarlo
    if (loginError != null) {
        sessionObj.removeAttribute("error");
    }

    // Obtener lista de películas
    List<Pelicula> peliculas = (sessionObj.getAttribute("peliculas") instanceof List<?>)
            ? (List<Pelicula>) sessionObj.getAttribute("peliculas")
            : null;

    // Paginación
    int peliculasPorPagina = 5;
    int paginaActual = request.getParameter("pagina") != null ? Integer.parseInt(request.getParameter("pagina")) : 1;
    int totalPeliculas = (peliculas != null) ? peliculas.size() : 0;
    int totalPaginas = (int) Math.ceil((double) totalPeliculas / peliculasPorPagina);
    int inicio = (paginaActual - 1) * peliculasPorPagina;
    int fin = Math.min(inicio + peliculasPorPagina, totalPeliculas);
%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Listado de Películas</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

</head>
<body>
<div class="container-fluid">
    <div class="row vh-100">
        <!-- Sidebar -->
        <div class="col-md-2 bg-light p-2">
            <h4>Menú</h4>
            <ul class="nav flex-column">
                <li class="nav-item"><a class="nav-link" href="reservarPelicula.jsp">Reservar Película</a></li>
                <li class="nav-item"><a class="nav-link" href="verMisReservas.jsp">Ver Mis Reservas</a></li>
                <li class="nav-item"><a class="nav-link" href="peliculas.jsp">Películas en Cartelera</a></li>
                <li class="nav-item"><a class="nav-link" href="cancelarReserva.jsp">Cancelar Reserva</a></li>
            </ul>

            <% if (autenticado) { %>
            <a href="logout.jsp" class="btn btn-danger mt-3 w-50">Cerrar sesión</a>
            <% } %>
        </div>

        <!-- Contenido principal -->
        <div class="col-md-9 p-3">
            <h2>Películas Disponibles</h2>

            <!-- Tabla de películas -->
            <table class="table table-bordered table-striped">
                <thead class="bg-white">
                <tr>
                    <th>ID</th>
                    <th>Título</th>
                    <th>Duración</th>
                    <th>Género</th>
                    <th>Director</th>
                    <th>Clasificación</th>
                    <th>Fecha de Estreno</th>
                    <th>País</th>
                    <th>Idioma</th>
                    <th>Disponible</th>
                    <th>Precio Reserva</th>
                    <th>Imagen</th>
                    <th>Reservar</th>
                </tr>
                </thead>
                <tbody>
                <% if (peliculas != null && !peliculas.isEmpty()) {
                    for (int i = inicio; i < fin; i++) {
                        Pelicula pelicula = peliculas.get(i);
                %>
                <tr>
                    <td><%= pelicula.getId() %></td>
                    <td><%= pelicula.getTitulo() %></td>
                    <td><%= pelicula.getDuracion() %> min</td>
                    <td><%= pelicula.getGenero() %></td>
                    <td><%= pelicula.getDirector() %></td>
                    <td><%= pelicula.getClasificacion() %></td>
                    <td><%= pelicula.getFechaEstreno() %></td>
                    <td><%= pelicula.getPais() %></td>
                    <td><%= pelicula.getIdioma() %></td>
                    <td><%= pelicula.getDisponible() ? "Sí" : "No" %></td>
                    <td>$<%= pelicula.getPrecioReserva() %></td>
                    <td>
                        <% if (pelicula.getImagen() != null) { %>
                        <img src="data:image/jpeg;base64,<%= Base64.getEncoder().encodeToString(pelicula.getImagen()) %>" class="img-thumbnail" width="100">
                        <% } else { %>
                        No disponible
                        <% } %>
                    </td>
                    <td>
                        <a href="reservarPelicula.jsp?id=<%= pelicula.getId() %>" class="btn btn-primary btn-sm">Reservar</a>
                    </td>
                </tr>
                <% }
                } else { %>
                <tr>
                    <td colspan="13" class="text-center">No hay películas disponibles</td>
                </tr>
                <% } %>
                </tbody>
            </table>

            <!-- Paginación -->
            <nav aria-label="Paginación">
                <ul class="pagination justify-content-center">
                    <% if (paginaActual > 1) { %>
                    <li class="page-item">
                        <a class="page-link" href="?pagina=<%= paginaActual - 1 %>">Anterior</a>
                    </li>
                    <% } %>

                    <% for (int i = 1; i <= totalPaginas; i++) { %>
                    <li class="page-item <%= (i == paginaActual) ? "active" : "" %>">
                        <a class="page-link" href="?pagina=<%= i %>"><%= i %></a>
                    </li>
                    <% } %>

                    <% if (paginaActual < totalPaginas) { %>
                    <li class="page-item">
                        <a class="page-link" href="?pagina=<%= paginaActual + 1 %>">Siguiente</a>
                    </li>
                    <% } %>
                </ul>
            </nav>
        </div>
    </div>
</div>

<!-- Modal de inicio de sesión -->
<div class="modal fade" id="loginModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Iniciar Sesión</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <% if (loginError != null) { %>
                <div class="alert alert-danger"><%= loginError %></div>
                <% } %>
                <form method="post" action="servletUsuarios?action=login">
                <div class="mb-3">
                        <label for="username" class="form-label">Usuario</label>
                        <input type="text" class="form-control" id="username" name="username" required>
                    </div>
                    <div class="mb-3">
                        <label for="password" class="form-label">Contraseña</label>
                        <input type="password" class="form-control" id="password" name="password" required>
                    </div>
                    <button type="submit" class="btn btn-primary w-100">Ingresar</button>
                </form>
            </div>
        </div>
    </div>
</div>

<script>
    <% if (!autenticado) { %>
    new bootstrap.Modal(document.getElementById('loginModal')).show();
    <% } %>
</script>

</body>
</html>
