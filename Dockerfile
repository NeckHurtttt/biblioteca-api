# Etapa 1: compilar
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src src
RUN mvn -q -B package -DskipTests

# Etapa 2: imagen liviana solo con el .jar
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd -r biblioteca
COPY --from=build /app/target/biblioteca-api-*.jar app.jar
USER biblioteca
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
