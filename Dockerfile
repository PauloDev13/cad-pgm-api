# Estágio 1: Build
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
# Copia o wrapper e o pom.xml (ajuste para Gradle se necessário)
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Estágio 2: Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
# O Spring Boot expõe a porta 8080 por padrão
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]