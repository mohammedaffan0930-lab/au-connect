
FROM maven:3.9-eclipse-temurin-21

WORKDIR /app

COPY . .

RUN mvn clean package -DskipTests

EXPOSE 8080

CMD ["sh", "-c", "java -jar $(find target -maxdepth 1 -type f -name '*.jar' ! -name '*.original' | head -n 1)"]

