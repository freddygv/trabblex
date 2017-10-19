# Project description
This project is a streaming facility, that makes use of edge computing.
All the code must be written in Java or cxx11.

This streaming service is a mesh between Bittorrent and Netflix. The idea is to decentralized the
streaming service, so instead of only using the cloud servers, the clients must communicate between
them and share data blocks for a specific file.

# Documentation

| Software | Link |
| -- | -- |
| Ice 3.7 | https://doc.zeroc.com/display/Ice37/Home |
| Maven 3.5.0 | http://maven.apache.org |
| Bittorrent java implementation | https://bitbucket.org/frazboyz/bencoder |
| Bittorrent specification | https://wiki.theory.org/index.php/BitTorrentSpecification |
| Docker | |

## Components

### Database manager
Maybe later will be implemented as singleton -
to have only one entry point to the database for all the other components

### Client Manager
* Creates a Grizzly HTTP Server (at first view, provides the best performance)
* Creates resources used by the client
* Offers REST interface (maybe ICE later)

#### Resource: Client Manager Resource
* Implements *client* interface
* Enables the client to get back the list of seeders , and create a seed

## Simple Client
Connects to the client manager using the JAX-RS Client API

## Database
Using CLOUD SQL --> integrated into google cloud

https://yurisubach.com/2016/07/14/jersey-dockerize/ - How to Dockerize Java RESTful API Application

## Hash System
* The hash is calculated by the seeder. It is then sent with the file to the client, as metadata.
* The client calculates the file's hash and compares it to the hash provided by the seeder.
* That is also done at the end of each chunk before the download of the next chunk

## Chunk management
* Each time a client finishes downloading a chunk, he indicates it to the server
* The server can then redirect the client that wants to download a chunk to other clients that already have it
* If the portal asks the client for a chunk it doesn't have --> update it in the database

### Chunk structure
* hash
* pointer to next chunk (hash of next chunk)

## Client Handling
Idea: for each request, a new thread gets created
Once maximum number of optimal threads (ex. 6 threads on a 4 core machine),
the other requests get queued

# Project team
Freddy - code the **Seeder manager**

Quentin - code the **Client manager**

# TODO
* Create a simple server that manages the client and seeder apps
* Create a simple client manager (Jetty)
* Create a simple ICE RPC client
* Create the seeder (ICE)
* Create test cases using Junit based on the spec - one for each method of the elements
* Manage exceptions in client manager resources
