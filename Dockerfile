# Etapa de construcción
FROM gradle:8.5-jdk21 AS build
COPY --chown=gradle:gradle . /home/gradle/project
WORKDIR /home/gradle/project
RUN gradle build -x test

# Etapa de producción
FROM eclipse-temurin:21-jdk
EXPOSE 8080
COPY --from=build /home/gradle/project/build/libs/demo-java-spring-api-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
