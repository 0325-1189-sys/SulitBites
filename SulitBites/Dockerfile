FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY src /app/src
RUN mkdir -p /app/out && javac -d /app/out $(find /app/src -name '*.java')
EXPOSE 3000
ENV PORT=3000
CMD ["java", "-cp", "/app/out", "com.sulitbites.Main"]
