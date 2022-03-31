FROM openjdk:8-alpine

COPY target/uberjar/r99c.jar /app/r99c.jar

EXPOSE 3000

CMD ["java", "-jar", "/app/r99c.jar"]
