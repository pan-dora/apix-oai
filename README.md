APIX-OAI
===================================

An API-X extension for OAI-PMH provider dissemination.

Building
--------
Create OSGI bundles

    gradle install

Copy bundles from local Maven repository to Docker Build directory
     
     gradle copyTask

Build Docker image

     gradle docker

Execute Docker Compose
     
     docker-compose up

Check OAI Endpoint
-----------------

    curl http:localhost:9104/oai
