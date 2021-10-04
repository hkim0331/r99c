FROM openjdk:8-alpine

COPY target/uberjar/r99c.jar /r99c/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/r99c/app.jar"]
