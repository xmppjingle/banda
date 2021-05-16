FROM openjdk:8-jdk-alpine
VOLUME /tmp
COPY build/libs/*.jar bjomeliga-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","./bjomeliga-0.0.1-SNAPSHOT.jar"]
CMD ["java","-jar","./bjomeliga-0.0.1-SNAPSHOT.jar"]