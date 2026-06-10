# syntax=docker/dockerfile:1.7
# ============================================================================
# Dockerfile — SIFAP 2.0 Backend
# Multi-stage: dependency cache → build → lean runtime
# Target JRE: Eclipse Temurin 21 (Alpine) — ~85 MB final image
# ============================================================================

# ─── Stage 1: resolve dependencies (cached unless pom.xml changes) ──────────
FROM eclipse-temurin:21-jdk-alpine AS deps
WORKDIR /build

# Copy only the POM first so Docker cache layer is reused on source-only changes
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Download all dependencies (offline-ready layer)
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B dependency:resolve dependency:resolve-plugins -q

# ─── Stage 2: build + test (skipped on release builds via --build-arg) ───────
FROM deps AS build
ARG SKIP_TESTS=false

COPY src src

RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B package \
        -DskipTests=${SKIP_TESTS} \
        -Djacoco.skip=${SKIP_TESTS}

# Explode the fat jar for faster layer diffing on redeploys
RUN java -Djarmode=layertools -jar target/*.jar extract --destination target/extracted

# ─── Stage 3: lean runtime image ─────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

# Non-root user (OWASP A05 — Security Misconfiguration)
RUN addgroup -S sifap && adduser -S sifap -G sifap
USER sifap

WORKDIR /app

# Spring Boot layered jar: dependencies change least → copied first (best cache hit)
COPY --from=build --chown=sifap:sifap /build/target/extracted/dependencies/          ./
COPY --from=build --chown=sifap:sifap /build/target/extracted/spring-boot-loader/    ./
COPY --from=build --chown=sifap:sifap /build/target/extracted/snapshot-dependencies/ ./
COPY --from=build --chown=sifap:sifap /build/target/extracted/application/           ./

# Actuator health check — used by Docker Compose + Kubernetes probes
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

EXPOSE 8080

# Virtual threads (Java 21) + structured logging (JSON via logback)
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} org.springframework.boot.loader.launch.JarLauncher"]
