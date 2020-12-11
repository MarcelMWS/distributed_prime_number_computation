FROM openjdk:7
COPY . /usr/src/server
WORKDIR /usr/src/server
EXPOSE 33333
RUN javac -classpath . Server.java && \
    java Server 999999999999
#CMD ["java", "Main"]