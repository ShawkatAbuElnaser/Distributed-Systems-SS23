# Distributed Systems - SS23

## Introduction

This project simulates a banking system using Maven and Docker Compose. The current system consists of 4 banks and a stock market, but these elements are scalable using docker-compose. This Java Simulation project aims to demonstrate the effective utilization of UDP, TCP, RPC with Thrift, and MQTT with Mosquitto for building a realistic banking system that ensures real-time data exchange and secure user interactions.
All the related measurements and protocols exist in the folder "Protocols"

## Installation

To install and run the project, follow these steps:

- Ensure that you have Docker and Docker Compose installed on your system.

- Clone the project repository to your local machine using Git or download the project source code as a ZIP file and extract it.

- Open a terminal or command prompt and navigate to the project's directory.

- Build the project using the following command: 'docker-compose up --build'

This command will build the necessary Docker images, download the required dependencies, and configure the project environment.

- Once the build process is complete, you can start the project by running the following command: 'docker-compose up'

This command will start the containers defined in the Docker Compose file and launch the project. You will see the logs and output of the running containers in the terminal.

- Access the project's HTTP Interface by opening a web browser and entering the URL: 'http://localhost:4321/'

## Connection-Protocols

### UDP-Sockets

The stock market communicates with the banks using UDP P (User Datagram Protocol). UDP is a lightweight and connectionless protocol that provides fast communication between applications. In this simulation, UDP is utilized for real-time exchange of stocks' information between the banks and stock markets, enabling efficient and quick updates on stock prices. However, despite those advantages, UDP is prone to packet loss, especially when the system is under heavy traffic.

### TCP-Sockets and HTTP

Furthermore, the banks communicate with the users through a TCP (Transmission Control Protocol) connection. TCP is a more reliable and connection-oriented protocol that usually ensures the delivery of data packets in the correct order. In this simulation, TCP is employed to build a HTTP-Interface, to ensure secure and consistent communication between the server (banks) and the clients (users).
To test the HTTP-Interface, search for http://localhost:4321/ on your browser while the system is running!

### Remote Procedure Calls (RPC)

Our project incorporates RPC using the Thrift framework. Thrift allows the creation of bank-rescue functions, enabling banks to request assistance from other banks in case of financial emergencies. These functions are later used in the MQTT-Connection

### Message-oriented Middleware (MoM)

Additionally, the simulation implements a bank rescue system using MQTT (Message Queuing Telemetry Transport) with Mosquitto as the broker. MQTT is a lightweight MoM protocol that supports efficient communication between multiple elements in the system. By leveraging MQTT and Mosquitto as the broker, the simulation establishes a reliable and scalable communication among the different banks to implement the bank-rescue-System, mentioned in the last paragraph.
The rescue system uses 2PC (2 Phase Commit), which employs one of the banks as the communication coordinator (Sparkasse in our System. The coordinator manages the different phases of the system, analyzing the votes of the other participants on the matter of assisting banks which reach bankruptcy.
