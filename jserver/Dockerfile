# Dockerfile - jperf-monitors (unified Java server: UDP, TCP, HTTP, gRPC)
FROM openjdk:11
WORKDIR /jmonitor
COPY target/jmonitor-server.jar /jmonitor/
EXPOSE 2019
ENTRYPOINT [ "java", "-jar", "/jmonitor/jmonitor-server.jar", "-P", "udp", "-e" ]
