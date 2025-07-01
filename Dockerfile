# Usa una imagen de Java 17 con Gradle preinstalado
FROM gradle:8.5-jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/project
WORKDIR /home/gradle/project

# Construye el proyecto
RUN gradle build -x test

# Usa JDK ligero para producción
FROM eclipse-temurin:17-jdk
EXPOSE 8080
COPY --from=build /home/gradle/project/build/libs/*.jar app.jar

# Ejecuta el jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
