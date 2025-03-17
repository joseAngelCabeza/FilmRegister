<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.*" %>

<%
    // Obtener el ID de la película desde la URL
    String idPelicula = request.getParameter("id");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Reservar Película</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body {
            display: flex;
            align-items: center;
            justify-content: center;
            min-height: 100vh;
            background-color: #e3f2fd;
            position: relative;
        }
        .container-form {
            background-color: #ffffff;
            border-radius: 8px;
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
            padding: 30px;
            max-width: 500px;
            width: 100%;
        }
        h2 {
            color: #1565c0;
            text-align: center;
            margin-bottom: 20px;
        }
        label {
            color: #1565c0;
        }
        .btn-primary {
            background-color: #1565c0;
            border-color: #1565c0;
        }
        .btn-primary:hover {
            background-color: #0d47a1;
            border-color: #0d47a1;
        }
        #message {
            display: none;
            margin-top: 20px;
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
    </style>
</head>
<body class="bg-light">
<a href="index.jsp" class="enlace-volver">&larr; Volver</a>
<div class="container mt-5">
    <h2 class="text-center">Reservar Película</h2>
    <form action="servletReservas?action=realizarReserva" method="post" class="card p-4 shadow-sm">
        <div class="mb-3">
            <label class="form-label">ID de la Película</label>
            <input type="text" name="idPelicula" class="form-control" value="<%= idPelicula != null ? idPelicula : "" %>"  required>
        </div>
        <div class="mb-3">
            <label class="form-label">Nombre</label>
            <input type="text" name="nombre" class="form-control" required>
        </div>
        <div class="mb-3">
            <label class="form-label">Apellidos</label>
            <input type="text" name="apellidos" class="form-control" required>
        </div>
        <div class="mb-3">
            <label class="form-label">Usuario</label>
            <input type="text" name="usuario" class="form-control" required>
        </div>
        <button type="submit" class="btn btn-primary w-100">Reservar</button>
    </form>
</div>
</body>
</html>