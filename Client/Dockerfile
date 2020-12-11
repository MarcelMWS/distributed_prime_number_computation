FROM openjdk:7
COPY . /usr/src/client
WORKDIR /usr/src/client
EXPOSE 33333
RUN javac -classpath . Client.java
RUN java Client 192.168.10.2 33333
#CMD ["java", "Main"]