FROM maven:3.8.5-openjdk-11-slim AS builder

ARG MVN_PROFILE="default"
WORKDIR /app

COPY . .

RUN mvn clean package -P ${MVN_PROFILE}

FROM tomcat:10-jdk11

WORKDIR /app

COPY --from=builder /app/target/cs122b.war /usr/local/tomcat/webapps/cs122b.war

EXPOSE 8080

CMD ["catalina.sh", "run"]
