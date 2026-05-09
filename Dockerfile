FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .
COPY src src

RUN chmod +x mvnw && ./mvnw -q -DskipTests package

EXPOSE 8080

CMD ["java", "-jar", "target/reservationapi-0.0.1-SNAPSHOT.jar"]
