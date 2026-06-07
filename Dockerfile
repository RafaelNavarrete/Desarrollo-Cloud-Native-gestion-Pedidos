FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY target/gestion-pedidos-0.0.1-SNAPSHOT.jar app.jar
RUN mkdir -p /app/efs
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]