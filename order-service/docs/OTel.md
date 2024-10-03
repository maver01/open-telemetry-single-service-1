# Open Telemetry Documentation

Following the official documentation [here](https://opentelemetry.io/docs/collector/quick-start/#:~:text=Launch%20the%20Collector%3A%20docker%20run%20%20-p%20127.0.0.1%3A4317%3A4317,%23%20Optionally%20tee%20output%20for%20easier%20search%20later) and the youtube videos made by
Linh Vu [here](https://www.youtube.com/watch?v=H9bAMRmaaxk&list=LL&index=1&t=390s).

## Part 1

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
7. Create docker-compose.yml as:
   ```
   version: '1'
   services:
     order-service:
     build: ./
     environment:
       OTEL_TRACES_EXPORTER: "logging"
       OTEL_METRICS_EXPORTER: "logging"
       OTEL_LOGS_EXPORTER: "logging"
       ports:
       - "8080:8080"
   ```
   By adding the environment variables, it is not necessary to specify them in the Dockerfile.
8. By adding the following agent to the gradle.build file, the Dockerfile will not need to pull the har file from the internet anymore, but it will load it from build directory instead:
   ```
   configurations {
     compileOnly {
         extendsFrom annotationProcessor
     }
     agent
   } 
   ```
   ```
   dependencies {
     agent "io.opentelemetry.javaagent:opentelemetry-javaagent:1.32.0"
     ...
   ```
   ```
   tasks.register('copyAgent', Copy) {
     from configurations.agent {
       rename "opentelemetry-javaagent-.*\\.jar", "opentelemetry-javaagent.jar"
     }
     into layout.buildDirectory.dir("agent")
   }
   ```
   ```
   bootJar {
     dependsOn copyAgent 
     archiveFileName = "app.jar"
   }
   ```
   The Dockerfile can therefore become: 
   ```
   ADD /build/agent/opentelemetry-javaagent.jar /opentelemetry-javaagent.jar
   ```
   
## Part 2

Following the second video of the library.

1. Added dependencies: 
   - implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
   - runtimeOnly 'org.postgresql:postgresql'
2. Run postgres using:
   ```docker compose up postgres -d```
3. Test the creation of the table, then in application.yml, change the url from localhost to postgres.
4. Add jaeger-all-in-one container, use the UI on `localhost:16686`. It will have a service called `app`, which is the name used for the jar file. Service name can be defined in docker-compose.yml.

## Part 3

Following [this](https://youtu.be/rcAYuHCpcUk?list=PLLMxXO6kMiNg6EcNCx6C6pydmgUlDDcZY) video tutorial.

1. Add prometheus to the docker-compose.yml. It will run with a configuration file, that is mounted in the container from the file in a folder we create called docker.
2. Add grafana to the the docker-compose.yml. It will run with a configuration file, that is mounted in the container from the file in a folder we create called docker.

## Part 4

Following [this](https://youtu.be/KayZj8Ga4NI?list=PLLMxXO6kMiNg6EcNCx6C6pydmgUlDDcZY) video tutorial.

Send logs to loki, through the OTel collector. 

1. Add collector configuration, that receives logs, and export them to Loki.
2. Change configuration for logs exporter in docker-compose.yml:
   ```
   OTEL_LOGS_EXPORTER: "otlp"
   OTEL_EXPORTER_OTLP_LOGS_ENDPOINT: "http://collector:4317"
   ````
   Port 4317 is the same as the one defined in the OTLP collector.
3. Added loki local-config.yml file, even though it is not in tutorial.
4. Added datasource to grafana-datasources.yml so that it does not have to be specified in grafana UI, but appears automatically. 