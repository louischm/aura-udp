# aura-udp
Udp module for Aura

## Prerequisites
In order to run this UDP module you'll need:
* Jdk 1.8
* Maven 4
* Protobuf 3
* The DB used by Aura's API setup (see aura-api repository for further information)

## Build
You can package the whole project via this command: `mvn clean package`
It will also compile the protobuf classes.

## Run
You can now run the application via the jar file that has been produces via this commad line: `java -jar target/server-1.0.0.jar`
