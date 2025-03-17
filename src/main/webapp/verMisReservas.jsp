<%--
  Created by IntelliJ IDEA.
  User: cronc
  Date: 10/02/2025
  Time: 11:56
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, logica.Reserva" %>
<%@ page import="jakarta.servlet.http.HttpSession" %>

<%
    HttpSession sessionObj = request.getSession();
    List<Reserva> reservas = (List<Reserva>) sessionObj.getAttribute("reservas");
%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Mis Reservas</title>
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
    <h2 class="text-center">Mis Reservas</h2>
    <table class="table table-striped">
        <thead>
        <tr>
            <th>ID Reserva</th>
            <th>Película</th>
            <th>Fecha de Reserva</th>
            <th>Estado</th>
            <th>Acción</th>
        </tr>
        </thead>
        <tbody>
        <% if (reservas != null) { %>
        <% for (Reserva reserva : reservas) { %>
        <tr>
            <td><%= reserva.getId() %></td>
            <td><%= reserva.getPelicula().getTitulo() %></td>
            <td><%= reserva.getFechaReserva() %></td>
            <td><%= reserva.getEstado() %></td>
            <td>
                <% if ("ACTIVA".equals(reserva.getEstado())) { %>
                <form action="realizarReserva" method="get">
                    <input type="hidden" name="idReserva" value="<%= reserva.getId() %>">
                    <input type="hidden" name="usuario" value="<%= reserva.getUsuario().getUsuario() %>">
                    <button type="submit" class="btn btn-danger btn-sm">Cancelar</button>
                </form>
                <% } %>
            </td>
        </tr>
        <% } %>
        <% } else { %>
        <tr><td colspan="5" class="text-center">No tienes reservas registradas.</td></tr>
        <% } %>
        </tbody>
    </table>

</div>
</body>
</html>
