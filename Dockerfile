FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY demo /app/demo
RUN cd demo && ./mvnw clean package -DskipTests
ENTRYPOINT ["java", "-jar", "/app/demo/target/demo-1.0.0.jar"]