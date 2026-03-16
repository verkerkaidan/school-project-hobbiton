FROM java:21
FROM maven:sapmachine

WORKDIR /app

COPY . /app

ADD pom.xml /app
RUN mvn verify clean --fail-never

RUN mvn clean package -DskipTests

ENTRYPOINT ["java", "-jar", "/app/target/recognition-service-0.0.1-SNAPSHOT.jar"]
