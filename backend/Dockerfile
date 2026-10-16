# syntax=docker/dockerfile:1
# Imagen del backend de GEEX. Se construye desde la raíz del repositorio:
#   docker build -f backend/Dockerfile -t geex-backend .

# ── 1. Compilación ──────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jdk-noble AS construccion
WORKDIR /app

COPY backend/.mvn .mvn
COPY backend/mvnw backend/pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -ntp -q dependency:resolve

COPY backend/src src
RUN ./mvnw -B -ntp -q package -DskipTests \
    && java -Djarmode=tools -jar target/geex-backend-*.jar extract --layers --launcher --destination extraido

# ── 2. Ejecución ────────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jre-noble
RUN groupadd --system geex && useradd --system --gid geex --uid 10001 geex
WORKDIR /app

# Capas de Spring Boot: las dependencias cambian poco y se cachean aparte del código
COPY --from=construccion /app/extraido/dependencies/ ./
COPY --from=construccion /app/extraido/spring-boot-loader/ ./
COPY --from=construccion /app/extraido/snapshot-dependencies/ ./
COPY --from=construccion /app/extraido/application/ ./

USER geex
EXPOSE 8080
# La configuración (base de datos, clave JWT...) llega por variables de entorno; nunca se incluye en la imagen.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "org.springframework.boot.loader.launch.JarLauncher"]
