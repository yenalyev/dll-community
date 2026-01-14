FROM eclipse-temurin:17-jre-alpine
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} ROOT.jar
ENTRYPOINT ["java","-jar","/ROOT.jar"]