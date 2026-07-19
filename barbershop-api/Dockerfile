
# ---------- Etapa 1: build ----------
# Imagem com Maven + JDK 21, só usada pra compilar. Não vai pra imagem final.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copia só o pom.xml primeiro e baixa as dependências.
# Isso cria uma camada de cache: se você não mexer no pom.xml,
# o Docker não baixa tudo de novo nos próximos builds (fica muito mais rápido).
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Agora copia o código fonte e compila, gerando o .jar
COPY src ./src
RUN mvn clean package -DskipTests

# ---------- Etapa 2: runtime ----------
# Imagem final, bem mais leve (só JRE, sem Maven/JDK completo)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copia só o .jar gerado na etapa de build
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]


