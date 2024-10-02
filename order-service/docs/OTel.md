# Open Telemetry Documentation

Following the official documentation [here](https://opentelemetry.io/docs/collector/quick-start/#:~:text=Launch%20the%20Collector%3A%20docker%20run%20%20-p%20127.0.0.1%3A4317%3A4317,%23%20Optionally%20tee%20output%20for%20easier%20search%20later) and the youtube video [here](https://www.youtube.com/watch?v=H9bAMRmaaxk&list=LL&index=1&t=390s).

Steps to reproduce: 

1. Copy the code to run a simple Spring Boot application (1st video up to 4th minute). Check that application is running properly. Installed with gradle, using dependencies:
   - implementation 'org.springframework.boot:spring-boot-starter-web'
   - compileOnly 'org.projectlombok:lombok'
   - annotationProcessor 'org.projectlombok:lombok'
   - testImplementation 'org.springframework.boot:spring-boot-starter-test'
   - testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
2. From the OTel documentation page (Collector -> Quickstart):
    - Install GO, add to environment path, check with `go version`
    - Add env variable with `export GOBIN=${GOBIN:-$(go env GOPATH)/bin}`
    - Run: `go install github.com/open-telemetry/opentelemetry-collector-contrib/cmd/telemetrygen@latest`
    - Launch the collector: 
   ```
   docker run \
    -p 127.0.0.1:4317:4317 \
    -p 127.0.0.1:55679:55679 \
    otel/opentelemetry-collector-contrib:0.110.0 \
    2>&1 | tee collector-output.txt # Optionally tee output for easier search later
    ```
   I changed the port to be 4318 as java server will be listening to that port for the collector.
3. From OTel docs, under Launguage APIs & SDKs -> Java -> Getting Started -> Instrumentation: 
    - Download the opentelemetry-javaagent.jar with: 
   ```
   curl -L -O https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar
   ```
   - Use gradle feature to `clean` and `build`. .jar file will be created under build -> libs -> ...SNAPSHOT.jar
   - Metrics, Traces and Logs need to be exported to logging. Run the following to run the application:
   ```
   java -javaagent:opentelemetry-javaagent.jar -Dotel.metrics.exporter=logging -Dotel.logs.exporter=logging -Dotel.traces.exporter=logging -jar build/libs/order-service-0.0.1-SNAPSHOT.jar
    ```
   - Server will start, automatically instrument the application.
4. When visiting http://localhost:8080/orders/4, metrics will appear on CLI.
5. In build.gradle, rename SNAPSHOT.jar by adding:
   ```
   bootJar {
       archiveFileName = "app.jar"
     }
   ```
   Then run gradle -> clean -> build again. SNAPSHOT.jar was renamed app.jar
6. Create Dockerfile:
   ```
   FROM eclipse-temurin:17-jre
   
   ADD build/libs/app.jar /app.jar
   ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar /opentelemetry-javaagent.jar
   
   ENTRYPOINT java -javaagent:/opentelemetry-javaagent.jar \
   -Dotel.traces.exporter=logging \
   -Dotel.metrics.exporter=logging \
   -Dotel.logs.exporter=logging \
   -jar /app.jar
   ```