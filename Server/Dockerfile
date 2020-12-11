FROM openjdk:7
COPY . /usr/src/server
WORKDIR /usr/src/server
EXPOSE 33333
RUN javac -classpath . Server.java
RUN java Server 33333
#MD ["java", "Server 33333"]