# Usa una imagen con JDK 21 para compilar el proyecto
FROM gradle:8.5-jdk21 AS build
COPY --chown=gradle:gradle . /home/gradle/project
WORKDIR /home/gradle/project

# Construye el proyecto sin tests
RUN gradle build -x test

# Imagen ligera para producción (también con JDK 21)
FROM eclipse-temurin:21-jdk
EXPOSE 8080
COPY --from=build /home/gradle/project/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
