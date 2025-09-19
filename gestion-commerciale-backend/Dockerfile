# 
# @path gestion-commerciale-backend/Dockerfile
# @description Dockerfile corrigé pour copier le JAR construit, quel que soit son nom exact

# Phase de build avec Maven Wrapper
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# Copier le Maven Wrapper et le projet
COPY mvnw mvnw.cmd ./
COPY .mvn .mvn
COPY pom.xml .
COPY src ./src

# Rendre le wrapper exécutable et lancer le build
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# Phase runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copier le jar compilé depuis le builder (nom générique)
COPY --from=builder /app/target/*.jar app.jar

# Lancement de l’application
ENTRYPOINT ["java", "-jar", "app.jar"]
