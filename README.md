APIX-OAI
===================================

An API-X extension for OAI-PMH provider dissemination.

Building
--------
Create gradle.properties
```sh 
    $ echo "version=0.0.1" > gradle.properties
```

Create OSGI bundles
```sh 
    $ gradle install
```
Copy bundles from local Maven repository to Docker Build directory
```sh      
    $ gradle copyTask
```
Build Docker image
```sh 
    $ gradle docker
```
Execute Docker Compose
 ```sh     
    $ docker-compose up
  ```   

Creating OAI Test Data
----------------- 
See [rdfxml-ingest](https://github.com/pan-dora/rdfxml-ingest)    

Check OAI Endpoint
-----------------

```sh    
    $ curl -sS 'http://localhost:9104/oai?verb=ListRecords&set=gmd'
```