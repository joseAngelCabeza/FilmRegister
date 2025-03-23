<%@ page import="jakarta.servlet.http.HttpSession" %>
<%@ page import="java.util.List" %>
<%@ page import="logica.Pelicula" %>
<%@ page import="logica.Reserva" %>
<%@ page import="java.util.Base64" %>
<%@ page contentType="text/html; charset=UTF-8" %>

<html>
<head>
    <title>Detalle de la Película Reservada</title>
    <style>
        body {
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            margin: 0;
            background-color: #f4f4f4;
        }
        .container {
            background: white;
            padding: 20px;
            border-radius: 10px;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
            text-align: center;
        }
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
        img {
            max-width: 200px;
            border-radius: 5px;
            margin-top: 10px;
        }
        .boton-ticket {
            display: inline-block;
            margin-top: 15px;
            padding: 10px 20px;
            font-size: 16px;
            color: white;
            background-color: black;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            text-decoration: none;
            transition: background-color 0.3s ease, transform 0.2s ease, box-shadow 0.2s ease;
        }

        .boton-ticket:hover {
            background-color: black;
            transform: scale(1.05);
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
        }
    </style>
</head>
<body>
<a href="index.jsp" class="enlace-volver">&larr; Volver</a>

<div class="container">
    <h2>Detalle de la Película Reservada</h2>

    <%
        HttpSession sessionActual = request.getSession(false);
        List<Reserva> reservas = (sessionActual != null) ? (List<Reserva>) sessionActual.getAttribute("reservas") : null;
        String idReservaStr = request.getParameter("id");

        if (reservas != null && idReservaStr != null) {
            try {
                int idReserva = Integer.parseInt(idReservaStr);
                Reserva reservaEncontrada = null;

                for (Reserva reserva : reservas) {
                    if (reserva.getId() == idReserva) {
                        reservaEncontrada = reserva;
                        break;
                    }
                }

                if (reservaEncontrada != null) {
                    Pelicula peliculaReservada = reservaEncontrada.getPelicula();
                    byte[] imagenBytes = peliculaReservada.getImagen();
                    String imagenBase64 = (imagenBytes != null && imagenBytes.length > 0)
                            ? Base64.getEncoder().encodeToString(imagenBytes)
                            : "";
    %>
    <p><strong>Título:</strong> <%= peliculaReservada.getTitulo() %></p>
    <p><strong>Duración:</strong> <%= peliculaReservada.getDuracion() %> minutos</p>
    <p><strong>Fecha de Reserva:</strong> <%= reservaEncontrada.getFechaReserva() %></p>
    <p><strong>Estado:</strong> <%= reservaEncontrada.getEstado() %></p>

    <% if (!imagenBase64.isEmpty()) { %>
    <p><strong>Imagen:</strong></p>
    <img src="data:image/jpeg;base64,<%= imagenBase64 %>" alt="Imagen de la película">
    <% } else { %>
    <p><strong>Imagen:</strong> No disponible</p>
    <% } %>

    <!-- Botón para generar el ticket en PDF -->
    <form action="servletReservas?action=CreoticketPDF" method="post" target="_blank">
        <input type="hidden" name="action" value="CrearTicketPDF">
        <input type="hidden" name="idReserva" value="<%= reservaEncontrada.getId() %>">
        <button type="submit" class="boton-ticket">Generar Ticket PDF</button>
    </form>

    <%
    } else {
    %>
    <p>No se encontró la reserva con ese ID.</p>
    <%
        }
    } catch (NumberFormatException e) {
    %>
    <p>Error: ID de reserva inválido.</p>
    <%
        }
    } else {
    %>
    <p>Error: No se pudo obtener la información de la reserva o sesión expirada.</p>
    <%
        }
    %>

</div>
</body>
</html>
