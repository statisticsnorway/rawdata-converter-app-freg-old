FROM adoptopenjdk/openjdk11-openj9:jdk-11.0.1.13-alpine-slim
COPY target/rawdata-converter-*.jar rawdata-converter.jar
EXPOSE 8080
CMD java -Dcom.sun.management.jmxremote -noverify ${JAVA_OPTS} -Dmicronaut.config.files="/conf/application.properties" -jar rawdata-converter.jar