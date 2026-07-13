# =========================================================================
# ETAPA 1: COMPILACIÓN (Build Stage) - Maven + Java 17
# =========================================================================
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copiar archivos de configuración de dependencias
COPY pom.xml .

# Copiar el código fuente
COPY src ./src

# Compilar el archivo .jar de Spring Boot sin ejecutar tests para mayor velocidad
RUN mvn clean package -DskipTests

# =========================================================================
# ETAPA 2: EJECUCIÓN LIGERA (Run Stage) - Java 17 JRE Alpine
# =========================================================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Crear directorio de subida de archivos
RUN mkdir -p /opt/render/project/src/uploads && chmod 777 /opt/render/project/src/uploads

# Copiar únicamente el .jar generado desde la etapa de construcción
COPY --from=build /app/target/*.jar app.jar

# Exponer el puerto por donde escucha Spring Boot
EXPOSE 8080

# Ejecutar la aplicación con límites optimizados para el plan gratuito de Render (512MB RAM)
ENTRYPOINT ["java", "-Xms128m", "-Xmx350m", "-jar", "app.jar"]
