FROM openjdk:7
COPY . /usr/src/server
WORKDIR /usr/src/server
EXPOSE 33333
RUN javac -classpath . Server.java
CMD java Server 5