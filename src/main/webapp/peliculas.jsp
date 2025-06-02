<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, logica.Pelicula" %>
<%@ page import="jakarta.servlet.http.HttpSession" %>

<%
    HttpSession sessionObj = request.getSession();
    List<Pelicula> peliculas = (List<Pelicula>) sessionObj.getAttribute("peliculas");
%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Películas Disponibles</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        .enlace-volver {
            position: absolute;
            top: 15px;
            left: 15px;
            font-size: 16px;
            text-decoration: none;
            color: #1565c0;
        }
        .enlace-volver:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body class="bg-light">
<a href="index.jsp" class="enlace-volver">&larr; Volver</a>
<!--En esta vista muestro la crtelera de algunas pelicula marcadas como disponibles con valor 1-->
<div class="container mt-5">
    <h2 class="text-center">Películas en Cartelera</h2>
    <div class="row d-flex align-items-stretch">
        <% if (peliculas != null) { %>
        <% for (Pelicula pelicula : peliculas) { %>
        <% if (pelicula.getDisponible()) { %>
        <div class="col-md-4 mb-4 d-flex">
            <div class="card h-100 w-100">
                <!--Cargo la imagen de la pelicula-->
                <img src="data:image/jpeg;base64,<%= java.util.Base64.getEncoder().encodeToString(pelicula.getImagen()) %>" class="card-img-top" alt="<%= pelicula.getTitulo() %>">
                <div class="card-body d-flex flex-column">
                    <h5 class="card-title"><%= pelicula.getTitulo() %></h5>
                    <p class="card-text flex-grow-1"><%= pelicula.getSinopsis() %></p>
                    <a href="reservarPelicula.jsp?id=<%= pelicula.getId() %>" class="btn btn-primary mt-auto">Reservar</a>
                </div>
            </div>
        </div>
        <% } %>
        <% } %>
        <% } else { %>
        <p class="text-center">No hay películas disponibles en este momento.</p>
        <% } %>
    </div>
</div>
</body>
</html>