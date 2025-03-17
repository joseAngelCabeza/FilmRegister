# Usar la imagen oficial de Tomcat con JDK 21
FROM tomcat:10.1.30-jdk21

# Autor del contenedor (opcional)
LABEL authors="Jose Angel"

# Establecer el directorio de trabajo en Tomcat
WORKDIR /usr/local/tomcat

# Copiar el archivo .war generado a la carpeta webapps de Tomcat
COPY target/FilmRegister-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/FilmRegister.war

# Exponer el puerto 8080 en el contenedor
EXPOSE 8080

# Iniciar Tomcat
CMD ["catalina.sh", "run"]
