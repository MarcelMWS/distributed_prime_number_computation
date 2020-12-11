distributed_prime_number_computation
====================================

A program to computer prime numbers across multiple computers and threads. The server is robust enough to handle client dropouts. The clients will shutdown if the server shutsdown.



This program can easily be run through the bash script that's included. This will however only work on Linux and Mac.

----------------------------------
            SERVER

  ./prime_number server start_number
  
This starts a server at the given starting number.

-----------------------------------
            CLIENT
            
  ./prime_number client server_ip_address

This starts a client that connects to the given server.


------------------------------------------------------------------------------

For any version of java.

  javac -classpath . *.java
  
To run the server

  java Server start_number
  
To run the client

  java Client server_ip_address server_port


## docker 

build 

docker build . -t prime:server
#server
docker-compose -f docker-compose-S.yml up --build

#client
docker-compose -f docker-compose.yml up --build