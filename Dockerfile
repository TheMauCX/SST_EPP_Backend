# ==============================================================
# Etapa 1: Build — compila el JAR con Maven
# --platform asegura compatibilidad con Azure (linux/amd64)
# ==============================================================
FROM --platform=linux/amd64 maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copiar solo pom.xml primero → Docker cachea las dependencias
# si el código cambia pero el pom no, esta capa no se re-descarga
COPY pom.xml .
RUN mvn dependency:go-offline -B --no-transfer-progress

# Copiar código fuente y compilar (sin tests — los tests se corren en CI)
COPY src ./src
RUN mvn clean package -DskipTests -B --no-transfer-progress

# ==============================================================
# Etapa 2: Runtime — imagen mínima JRE Alpine
# ==============================================================
FROM --platform=linux/amd64 eclipse-temurin:17-jre-alpine
WORKDIR /app

# Instalar curl para el healthcheck (wget ya viene en alpine)
RUN apk add --no-cache curl

# Crear usuario no-root por seguridad
RUN addgroup -S spring && adduser -S spring -G spring

# Crear directorio para uploads temporales con permisos correctos
RUN mkdir -p /app/uploads && chown spring:spring /app/uploads

USER spring:spring

# Copiar JAR desde etapa de build
COPY --from=build /app/target/*.jar app.jar

# Puerto que expone la app
EXPOSE 8080

# Health check — Azure Container Apps lo usa para determinar si el contenedor está listo
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# JAVA_OPTS permite inyectar flags JVM en runtime desde Azure (ej: -Xms, -Xmx)
# Spring Boot 3.x usa SPRING_APPLICATION_JSON o variables de entorno directamente
ENV JAVA_OPTS="-Xms256m -Xmx512m -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]