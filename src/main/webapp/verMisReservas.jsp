<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, logica.Reserva" %>
<%@ page import="jakarta.servlet.http.HttpSession" %>

<%
    HttpSession sessionObj = request.getSession();
    List<Reserva> reservas = (List<Reserva>) sessionObj.getAttribute("reservas");
    boolean noReservas = (reservas == null || reservas.isEmpty());
    String errorCancelar = (String) sessionObj.getAttribute("errorCancelar");
    sessionObj.removeAttribute("errorCancelar");
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

        @media (max-width: 576px) {
            table thead {
                display: none;
            }

            table tbody tr {
                display: block;
                margin-bottom: 1rem;
                background: #fff;
                border-radius: 8px;
                box-shadow: 0 2px 5px rgba(0,0,0,0.1);
                padding: 10px;
            }

            table tbody td {
                display: flex;
                justify-content: space-between;
                padding: 6px 10px;
                border: none;
                border-bottom: 1px solid #eee;
            }

            table tbody td:last-child {
                border-bottom: none;
            }

            table tbody td::before {
                content: attr(data-label);
                font-weight: bold;
                color: #1565c0;
            }
        }
    </style>
</head>
<body class="bg-light">
<a href="index.jsp" class="enlace-volver">&larr; Volver</a>

<div class="container mt-5">
    <h2 class="text-center">Mis Reservas</h2>

    <% if (errorCancelar != null) { %>
    <div class="alert alert-danger text-center" role="alert">
        <%= errorCancelar %>
    </div>
    <% } %>

    <% if (noReservas) { %>
    <div class="alert alert-info text-center" role="alert">
        No tienes reservas registradas.
    </div>
    <% } else { %>
    <div class="table-responsive">
        <table class="table table-striped">
            <thead class="table-primary">
            <tr>
                <th>ID</th>
                <th>Película</th>
                <th>Fila</th>
                <th>Asiento</th>
                <th>Fecha</th>
                <th>Estado</th>
                <th>Acción</th>
            </tr>
            </thead>
            <tbody>
            <!--Cargo los datos de mis reservas-->
            <% for (Reserva reserva : reservas) { %>
            <tr>
                <td data-label="ID"><%= reserva.getId() %></td>
                <td data-label="Película"><%= reserva.getPelicula().getTitulo() %></td>
                <td data-label="Fila"><%= reserva.getnFila() %></td>
                <td data-label="Asiento"><%= reserva.getnAsiento() %></td>
                <td data-label="Fecha"><%= reserva.getFechaReserva() %></td>
                <td data-label="Estado"><%= reserva.getEstado() %></td>
                <td data-label="Acción">
                    <% if ("ACTIVA".equals(reserva.getEstado())) { %>
                    <form action="servletReservas?action=cancelarReserva" method="post">
                        <input type="hidden" name="idReserva" value="<%= reserva.getId() %>">
                        <button type="submit" class="btn btn-danger btn-sm">Cancelar</button>
                    </form>
                    <% } else { %>
                    <span class="text-muted">-</span>
                    <% } %>
                </td>
            </tr>
            <% } %>
            </tbody>
        </table>
    </div>
    <% } %>
</div>
</body>
</html>
