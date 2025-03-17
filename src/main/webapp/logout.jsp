<%--
  Created by IntelliJ IDEA.
  User: cronc
  Date: 10/02/2025
  Time: 11:45
  To change this template use File | Settings | File Templates.
--%>
<%@ page import="jakarta.servlet.http.HttpSession" %>
<%
    // Cierro la sesion
    HttpSession sessionObj = request.getSession(false);
    if (sessionObj != null) {
        sessionObj.invalidate();
    }

    // Elimino la cookie de sesión
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("JSESSIONID")) {
                cookie.setMaxAge(0);
                cookie.setPath("/");
                response.addCookie(cookie);
            }
        }
    }

    // Regreso al jsp
    response.sendRedirect("index.jsp");
%>
