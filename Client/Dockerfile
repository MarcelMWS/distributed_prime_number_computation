FROM openjdk:7
COPY . /usr/src/client
WORKDIR /usr/src/client
EXPOSE 33333
RUN javac -classpath . Client.java && \
    java Client 172.28.1.2:33333
#CMD ["java", "Main"]