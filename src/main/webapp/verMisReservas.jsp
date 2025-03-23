<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, logica.Reserva" %>
<%@ page import="jakarta.servlet.http.HttpSession" %>

<%
    HttpSession sessionObj = request.getSession();
    List<Reserva> reservas = (List<Reserva>) sessionObj.getAttribute("reservas");

    // Verifico si no hay reservas para mostrar
    boolean noReservas = (reservas == null || reservas.isEmpty());
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

    <% if (noReservas) { %>
    <div class="alert alert-info text-center" role="alert">
        No tienes reservas registradas.
    </div>
    <% } else { %>
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
        </tbody>
    </table>
    <% } %>

</div>
</body>
</html>

