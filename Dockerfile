# Multi-stage build — Allo Bank Backend Challenge
# ── Stage 1: Build ──────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy pom first (layer cache for dependencies)
COPY pom.xml ./
RUN mvn -B dependency:go-offline -q

# Copy source and build
COPY src/ src/
RUN mvn -B package -DskipTests -q

# ── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 4110

ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "app.jar"]
