<%--
  Created by IntelliJ IDEA.
  User: cronc
  Date: 10/02/2025
  Time: 11:57
  To change this template use File | Settings | File Templates.
--%>
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
<div class="container mt-5">
    <h2 class="text-center">Películas en Cartelera</h2>
    <div class="row">
        <% if (peliculas != null) { %>
        <% for (Pelicula pelicula : peliculas) { %>
        <% if (pelicula.getDisponible()) { %>
        <div class="col-md-4 mb-4">
            <div class="card">
                <img src="data:image/jpeg;base64,<%= java.util.Base64.getEncoder().encodeToString(pelicula.getImagen()) %>" class="card-img-top" alt="<%= pelicula.getTitulo() %>">
                <div class="card-body">
                    <h5 class="card-title"><%= pelicula.getTitulo() %></h5>
                    <p class="card-text"><%= pelicula.getSinopsis() %></p>
                    <a href="reservarPelicula.jsp?id=<%= pelicula.getId() %>" class="btn btn-primary">Reservar</a>
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
