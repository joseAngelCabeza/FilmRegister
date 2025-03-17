<%--
  Created by IntelliJ IDEA.
  User: cronc
  Date: 10/02/2025
  Time: 11:58
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Cancelar Reserva</title>
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
    <h2 class="text-center">Cancelar Reserva</h2>
    <form action="borrarReserva" method="post" class="card p-4 shadow-sm">
        <div class="mb-3">
            <label class="form-label">ID de Reserva</label>
            <input type="text" name="idReserva" class="form-control" required>
        </div>
        <div class="mb-3">
            <label class="form-label">Usuario</label>
            <input type="text" name="usuario" class="form-control" required>
        </div>
        <button type="submit" class="btn btn-danger w-100">Cancelar Reserva</button>
    </form>
</div>
</body>
</html>

