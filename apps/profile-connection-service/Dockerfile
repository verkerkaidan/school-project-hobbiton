FROM java:21
FROM maven:sapmachine

WORKDIR /app

COPY . /app

ADD pom.xml /app
RUN mvn verify clean --fail-never

RUN mvn clean package -DskipTests

ENTRYPOINT ["java", "-jar", "/app/target/profile-connection-service-1.0.0.jar"]
