<%@ page import="logica.Usuario" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Registro de Usuario</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <style>
        body {
            display: flex;
            align-items: center;
            justify-content: center;
            min-height: 100vh;
            background-color: #e3f2fd;
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
<body>
<a href="index.jsp" class="enlace-volver">&larr; Volver</a>
<div class="container-form">
    <h2>Registro de Usuarios</h2>
    <form id="RegistroUsuario" action="servletUsuarios" method="post">
        <input type="hidden" name="action" value="RegistroUsuario">

        <div class="mb-3">
            <label for="dni" class="form-label">DNI:</label>
            <input type="text" id="dni" name="dni" class="form-control" required>
        </div>

        <div class="mb-3">
            <label for="nombre" class="form-label">Nombre:</label>
            <input type="text" id="nombre" name="nombre" class="form-control" required>
        </div>

        <div class="mb-3">
            <label for="apellidos" class="form-label">Apellidos:</label>
            <input type="text" id="apellidos" name="apellidos" class="form-control" required>
        </div>

        <div class="mb-3">
            <label for="email" class="form-label">Email:</label>
            <input type="email" id="email" name="email" class="form-control" required>
        </div>

        <div class="mb-3">
            <label for="usuario" class="form-label">Usuario:</label>
            <input type="text" id="usuario" name="usuario" class="form-control" required>
        </div>

        <div class="mb-3">
            <label for="contraseña">Contraseña:</label>
            <input type="password" id="contraseña" name="contraseña" class="form-control" required minlength="6">
        </div>

        <button type="submit" class="btn btn-primary w-100">Registrarse</button>
    </form>

    <!-- Mensaje de respuesta -->
    <div id="message" class="alert" style="display: none;"></div>
</div>

<script>
    document.getElementById('RegistroUsuario').addEventListener('submit', function(e) {
        e.preventDefault();

        var formData = new FormData(this);
        var urlEncodedData = new URLSearchParams(formData);

        fetch('servletUsuarios', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: urlEncodedData
        })
            .then(response => response.json())
            .then(data => {
                const messageDiv = document.getElementById('message');
                messageDiv.style.display = 'block';
                messageDiv.className = data.success ? 'alert alert-success' : 'alert alert-danger';
                messageDiv.textContent = data.message;

                if (data.success) {
                    setTimeout(() => {
                        window.location.href = data.redirect;
                    }, 2000);
                }

                setTimeout(() => {
                    messageDiv.style.display = 'none';
                }, 5000);
            })
            .catch(error => {
                console.error('Error:', error);
                const messageDiv = document.getElementById('message');
                messageDiv.style.display = 'block';
                messageDiv.className = 'alert alert-danger';
                messageDiv.textContent = 'Hubo un error al crear el usuario';

                setTimeout(() => {
                    messageDiv.style.display = 'none';
                }, 5000);
            });
    });
</script>
</body>
</html>
