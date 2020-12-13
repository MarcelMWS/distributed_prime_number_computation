FROM openjdk:7
COPY . /usr/src/client
WORKDIR /usr/src/client
EXPOSE 33333
RUN javac -classpath . Client.java
CMD java Client 192.168.10.3 33333