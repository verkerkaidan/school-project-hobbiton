FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN chmod +x mvnw

RUN ./mvnw dependency:go-offline

COPY src src

RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

RUN apt-get update && apt-get install -y curl

COPY --from=build /app/target/*.jar app.jar
COPY entrypoint.sh /entrypoint.sh

RUN chmod +x /entrypoint.sh

RUN mkdir -p /app/models

ENTRYPOINT ["/entrypoint.sh"]
