FROM java:8
VOLUME /tmp
ADD *.jar message_service.jar
EXPOSE 2222
ENTRYPOINT [ "java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/message_service.jar" ]