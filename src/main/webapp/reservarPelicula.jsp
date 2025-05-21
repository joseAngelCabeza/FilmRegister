<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%
    String idPelicula = request.getParameter("id");
    String error = (String) session.getAttribute("errorReserva");
    session.removeAttribute("errorReserva");

    List<String> asientosOcupados = (List<String>) request.getAttribute("asientosOcupados");
    if (asientosOcupados == null) asientosOcupados = new java.util.ArrayList<>();
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Seleccionar Asiento</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet" />
    <style>
        body {
            background-color: white;
            min-height: 100vh;
            margin: 0;
            font-family: Arial, sans-serif;
        }
        .volver-link {
            position: fixed;
            top: 15px;
            left: 15px;
            font-size: 16px;
            color: #1565c0;
            text-decoration: none;
            font-weight: 600;
            z-index: 1000;
        }
        .volver-link:hover {
            text-decoration: underline;
        }
        .container-form {
            background-color: white;
            max-width: 600px;
            margin: 100px auto 50px auto;
            padding: 30px 40px;
            border-radius: 8px;
            box-shadow: 0 4px 15px rgba(0,0,0,0.1);
            display: flex;
            flex-direction: column;
            align-items: center;
        }
        .screen {
            background-color: #ccc;
            padding: 10px;
            margin-bottom: 20px;
            width: 100%;
            text-align: center;
            font-weight: bold;
            border-radius: 5px;
            box-shadow: inset 0 0 5px #999;
        }
        form {
            width: 100%;
        }
        .seats {
            display: grid;
            grid-template-columns: 40px repeat(8, 50px);
            gap: 10px;
            margin-bottom: 20px;
            align-items: center;
        }
        .fila-num {
            font-weight: bold;
            font-size: 20px;
            color: #1565c0;
            text-align: center;
            user-select: none;
        }
        button.seat {
            border: 2px solid #1565c0;
            background-color: white;
            border-radius: 5px;
            font-size: 16px;
            cursor: pointer;
            color: #1565c0;
            font-weight: bold;
            transition: background-color 0.3s, color 0.3s;
            position: relative;
            height: 40px;
            width: 50px;
            line-height: 38px;
            text-align: center;
            padding: 0;
        }
        button.seat.selected {
            background-color: #2e7d32;
            color: white;
            border-color: #2e7d32;
        }
        button.seat.occupied {
            background-color: #b71c1c;
            color: white;
            border-color: #b71c1c;
            cursor: not-allowed;
        }
        button.seat::after {
            content: "\f5d1"; /* fa-chair icon Unicode from FontAwesome */
            font-family: "Font Awesome 6 Free";
            font-weight: 900;
            position: absolute;
            top: 3px;
            right: 3px;
            font-size: 14px;
            color: inherit;
            opacity: 0.3;
        }
        button.seat.occupied::after {
            content: "\f271"; /* fa-ban icon */
            opacity: 0.6;
        }
        #error-message {
            margin-bottom: 20px;
            text-align: center;
            width: 100%;
        }
        button#confirm-btn {
            width: auto;
            padding: 8px 25px;
            font-size: 16px;
            font-weight: 600;
        }
    </style>
    <link
            rel="stylesheet"
            href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css"
            crossorigin="anonymous"
            referrerpolicy="no-referrer"
    />
</head>
<body>
<a href="index.jsp" class="volver-link">&larr; Volver</a>

<div class="container-form">

    <% if (error != null) { %>
    <div id="error-message" class="alert alert-danger">
        <%= error %>
    </div>
    <% } else { %>
    <div id="error-message"></div>
    <% } %>

    <div class="screen">Pantalla</div>

    <form action="servletReservas?action=realizarReserva" method="post" onsubmit="return validarSeleccion()">
        <input type="hidden" name="idPelicula" value="<%= idPelicula %>" />
        <input type="hidden" name="nFila" id="nFila" />
        <input type="hidden" name="nAsiento" id="nAsiento" />

        <% for (int fila = 1; fila <= 5; fila++) { %>
        <div class="seats">
            <div class="fila-num"><%= fila %></div>
            <% for (int asiento = 1; asiento <= 8; asiento++) {
                String seatId = fila + "-" + asiento;
                boolean ocupado = asientosOcupados.contains(seatId);
            %>
            <button type="button"
                    class="seat <%= ocupado ? "occupied" : "" %>"
                    data-fila="<%= fila %>"
                    data-asiento="<%= asiento %>"
                    <%= ocupado ? "disabled" : "" %>
                    title="Fila <%= fila %> - Asiento <%= asiento %>">
                <%= asiento %>
            </button>
            <% } %>
        </div>
        <% } %>

        <button type="submit" id="confirm-btn" class="btn btn-primary mt-3">Confirmar Reserva</button>
    </form>
</div>

<script>
    const seats = document.querySelectorAll('button.seat:not(.occupied)');
    let selectedSeat = null;

    seats.forEach(seat => {
        seat.addEventListener('click', () => {
            if (selectedSeat) {
                selectedSeat.classList.remove('selected');
            }
            seat.classList.add('selected');
            selectedSeat = seat;

            document.getElementById('nFila').value = seat.dataset.fila;
            document.getElementById('nAsiento').value = seat.dataset.asiento;

            // Limpiar el mensaje de error si hay
            document.getElementById('error-message').innerText = "";
        });
    });

    function validarSeleccion() {
        if (!selectedSeat) {
            alert("Por favor selecciona un asiento.");
            return false;
        }
        return true;
    }
</script>
</body>
</html>
