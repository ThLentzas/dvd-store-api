FROM maven:3.9.1 AS buildstage

WORKDIR /app

ADD pom.xml .

ADD src ./src

RUN mvn clean package -P docker -DskipTests

FROM openjdk:17-jdk-alpine

COPY --from=buildstage /app/target/DvdApi.jar .

ENTRYPOINT ["java", "-jar", "DvdApi.jar"]