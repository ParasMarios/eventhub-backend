# Χρησιμοποιούμε Java 17
FROM eclipse-temurin:17-jdk-focal
WORKDIR /app
# Αντιγράφουμε το παραχθέν jar από τον φάκελο target
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]