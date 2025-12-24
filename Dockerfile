FROM openjdk:8-jdk-alpine
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} ROOT.jar
ENTRYPOINT ["java","-jar","/ROOT.jar"]