# Usar la imagen oficial de Tomcat con JDK 21
FROM tomcat:10.1.30-jdk21

# Autor del contenedor (opcional)
LABEL authors="Jose Angel"

# Establecer el directorio de trabajo en Tomcat
WORKDIR /usr/local/tomcat

# Definir la variable de entorno para el puerto (Render asigna un puerto aleatorio)
ENV PORT=10000
ENV CATALINA_OPTS="-Djava.awt.headless=true -Djava.security.egd=file:/dev/./urandom"

# Modificar server.xml para usar el puerto dinámico
RUN sed -i "s/port=\"8080\"/port=\"${PORT}\"/g" /usr/local/tomcat/conf/server.xml

# Copiar el archivo .war generado y renombrarlo a ROOT.war
COPY target/FilmRegister-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war

# Exponer el puerto dinámico en el contenedor
EXPOSE 10000

# Iniciar Tomcat
CMD ["catalina.sh", "run"]