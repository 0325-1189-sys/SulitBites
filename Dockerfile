# Build stage: compile the Java source
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY src ./src
RUN javac -encoding UTF-8 -d out $(find src -name '*.java')

# Run stage: small image with only the Java runtime
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/out ./out
ENV PORT=3000
EXPOSE 3000
CMD ["java", "-cp", "out", "com.sulitbites.Main"]
