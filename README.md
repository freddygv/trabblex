# Project description
This project is a streaming facility, that makes use of edge computing.
All the code must be written in Java or cxx11.

This streaming service is a mesh between Bittorrent and Netflix. The idea is to decentralized the
streaming service, so instead of only using the cloud servers, the clients must communicate between
them and share data blocks for a specific file.

# Log - quentin
* Taking care of the client manager, and the client.
* Created a Downloader class, which connects to the seeder and downloads a chunk (TODO finish)
    * give it a buffer, an ip + port + protocol and wait for it to finish
    * meanwhile, can start other downloads
    * Note: by convenience, format of the file stored in the name (eg file.mp4)
* If no seeder found (server or client), contact ClientManager and ask him to create a seeder
* Added a method to the seeder that creates a tcp connection, for the client to connect to
* TODO client to client connection
* TODO have the seeder seed a single chunk and not the entire file

* How does freddy manage file metadata ? + needs better exception management if file not found
* When Ice portal can't connect to database manager, needs better exception management to indicate problem
* Changed file size from string to int in registerSeeder, needs to take into account MB, GB....
    * better exception management
    * better file size extraction
    * etc

* The SeederServer manually creates and starts a seeder
    * Implement via RPC call

* TODO in downloader - give him the download folder

* If no chunk seeder available for a chunk, create one:
    * (((Create the seeder - scan the whole file))) -- done at start
    * The seeder then creates a chunk seeder based on which chunk was asked for by the client
    * Needs a table mapping the chunks to the file

* Chunk management: the client calls the seedbox to know how many chunks a file has

* Managing info on the download:
    * class File
    * has arraylist of seeders <hash, ip, port, protocol>
    * manages download
    * has file name, hash, number of chunks
    * is passed to downloadFile when called recursively

* First, download a chunk (the rarest one)
    * Handshake
    * Indicates total number of chunks in file
    * If needed, request a seeder

====== ALGORITHM ========
* Download the first chunk from the first seeder available
    * If no seeder available, create one and recursive call
* From metadata, get number of chunks
* Assess if all chunks are available
* If no, request seeder for missing chunks (NOTE how to know them ?? From file parts ?)
* Start pool of downloaders
    * If downloader fails, give him next chunk source
    * If no more chunk sources, request new seeder

* TODO later : in startup phase, only download metadata and not entire chunk ?
* NOTE: now we also get the name of the file part in the metadata 
    * TODO client side, extract file name --> chunk number

* TODO client open tcp connection to other clients
* TODO priority management

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
