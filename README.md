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
     
Creating OAI Test Data
----------------- 
See [rdfxml-ingest](https://github.com/pan-dora/rdfxml-ingest)    

Check OAI Endpoint
-----------------

```sh    
    $ curl -sS 'http://localhost:9104/oai?verb=ListRecords&set=gmd'
```