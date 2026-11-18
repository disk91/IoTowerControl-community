FROM eclipse-temurin:25-jre
COPY ./build/libs/iot-0.0.1-SNAPSHOT.jar itc.jar
ENTRYPOINT ["java","-jar","/itc.jar"]
