FROM --platform=linux/amd64 amazoncorretto:23 as build
WORKDIR application
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM --platform=linux/amd64 amazoncorretto:23
WORKDIR application
COPY --from=build application/dependencies/ ./
COPY --from=build application/spring-boot-loader/ ./
COPY --from=build application/snapshot-dependencies/ ./
COPY --from=build application/application/ ./
ENTRYPOINT ["java", "-Duser.timezone = Vietname/Hanoi", "-Dspring.profiles.active=${SPRING_PROFILE}", "org.springframework.boot.loader.launch.JarLauncher"]
