<%@ page import="jakarta.servlet.http.HttpSession" %>
<%@ page import="java.util.List" %>
<%@ page import="logica.Pelicula" %>
<%@ page import="java.util.Base64" %>
<%@ page contentType="text/html; charset=UTF-8" %>


<%
    HttpSession sessionObj = request.getSession();
    boolean autenticado = (sessionObj.getAttribute("usuario") != null); //Compruebo que se ha realizado el login
    String loginError = (String) sessionObj.getAttribute("error");
    String nombreUsuario = autenticado ? (String) sessionObj.getAttribute("usuario") : "Invitado";

    // Limpio el mensaje de error del login
    if (loginError != null) {
        sessionObj.removeAttribute("error");
    }

    // Obtengo la lista de peliculas
    List<Pelicula> peliculas = (sessionObj.getAttribute("peliculas") instanceof List<?>)
            ? (List<Pelicula>) sessionObj.getAttribute("peliculas")
            : null;

    // Variables de mi Paginacion
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
    <style>
        /*Ajusto algunos parametros cuando se reduce el tamño de la pantalla*/
        @media (max-width: 1400px) {
            table, thead, tbody, th, td, tr {
                display: block;
            }

            thead {
                display: none; /* Ocultamos cabecera */
            }

            tr {
                margin-bottom: 15px;
                border: 1px solid #ccc;
                border-radius: 10px;
                padding: 10px;
                background-color: #f7f7f7;
            }

            td {
                display: flex;
                justify-content: space-between;
                align-items: center;
                padding: 10px;
                border-bottom: 1px solid #ddd;
                white-space: normal;
                overflow-wrap: break-word;
            }

            td::before {
                content: attr(data-label);
                font-weight: bold;
                color: #004085;
                margin-right: 10px;
                flex: 0 0 45%;
                text-align: left;
            }

            td img {
                max-width: 100%;
                height: auto;
                display: block;
                margin-left: auto;
            }

            td:last-child {
                border-bottom: none;
            }
        }



    </style>
</head>
<body>
<!--Pongo una barra superior para el nombre de la web y para mostrar el usuario actual-->
<nav class="navbar navbar-expand-lg navbar-dark bg-dark">
    <div class="container-fluid">
        <a class="navbar-brand d-flex align-items-center" href="#">
            🎬 <span class="ms-2">FilmRegister</span>
        </a>
        <div class="ms-auto text-white">
            Bienvenido, <strong><%= nombreUsuario %></strong>
        </div>
    </div>
</nav>

<div class="container-fluid">
    <div class="row vh-100">
        <!--Pongo una barra lateral a modo de Menú-->
        <div class="col-md-2 bg-light p-2">
            <h4>Menú</h4>
            <ul class="nav flex-column">
                <li class="nav-item"><a class="nav-link" href="verMisReservas.jsp">Ver Mis Reservas</a></li>
                <li class="nav-item"><a class="nav-link" href="peliculas.jsp">Películas en Cartelera</a></li>
            </ul>

            <% if (autenticado) { %>
            <a href="logout.jsp" class="btn btn-danger mt-3 w-50">Cerrar sesión</a>
            <% } %>
        </div>

        <!--Tabla con la Cartelera de Peliculas-->
        <div class="col-md-9 p-3">
            <h2>Películas en Cartelera</h2>

            <div class="table-responsive">
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
                    <tr>
                        <td data-label="ID"><%= pelicula.getId() %></td>
                        <td data-label="Título"><%= pelicula.getTitulo() %></td>
                        <td data-label="Duración"><%= pelicula.getDuracion() %> min</td>
                        <td data-label="Género"><%= pelicula.getGenero() %></td>
                        <td data-label="Director"><%= pelicula.getDirector() %></td>
                        <td data-label="Clasificación"><%= pelicula.getClasificacion() %></td>
                        <td data-label="Fecha de Estreno"><%= pelicula.getFechaEstreno() %></td>
                        <td data-label="País"><%= pelicula.getPais() %></td>
                        <td data-label="Idioma"><%= pelicula.getIdioma() %></td>
                        <td data-label="Disponible"><%= pelicula.getDisponible() ? "Sí" : "No" %></td>
                        <td data-label="Precio Reserva">$<%= pelicula.getPrecioReserva() %></td>
                        <td data-label="Imagen">
                            <% if (pelicula.getImagen() != null) { %>
                            <img src="data:image/jpeg;base64,<%= Base64.getEncoder().encodeToString(pelicula.getImagen()) %>" class="img-thumbnail" width="100">
                            <% } else { %>
                            No disponible
                            <% } %>
                        </td>
                        <td data-label="Reservar">
                            <a href="reservarPelicula.jsp?id=<%= pelicula.getId() %>" class="btn btn-primary btn-sm">Reservar</a>
                        </td>
                    </tr>

                    </tr>
                    <% }
                    } else { %>
                    <tr>
                        <td colspan="13" class="text-center">No hay películas disponibles</td>
                    </tr>
                    <% } %>
                    </tbody>
                </table>
            </div>

            <!-- Enlaces de la Paginación -->
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

<!-- Modal de inicio de sesión Obligatorio-->
<div class="modal " id="loginModal" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog " id="modalDialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Iniciar Sesión</h5>
                <a href="registro.jsp">Ir a Registro</a>
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
    const modal = new bootstrap.Modal(document.getElementById('loginModal'));
    modal.show();

    // Evito que el usuario se salte el modal evitando que haga click fuera y resaltando que debe logearse
    document.getElementById('loginModal').addEventListener('click', function(event) {
        if (event.target === this) {
            document.getElementById('modalDialog').classList.add('modal-lg'); // Agrandar el modal
        }
    });
    <% } %>
</script>

</body>
</html>
