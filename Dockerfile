# ---------- BUILD STAGE ----------
FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

# Copy Maven wrapper & config
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Fix permissions + Windows line endings
RUN chmod +x mvnw && sed -i 's/\r$//' mvnw

# Download dependencies
RUN ./mvnw -B dependency:resolve

# Copy source
COPY src src

# Build jar
RUN ./mvnw clean package -DskipTests


# ---------- RUN STAGE ----------
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy built jar
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
