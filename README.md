Trabblex
=========
![architecture](https://github.com/freddygv/trabblex/raw/master/img_report/global.png)
### What?
Trabblex is a streaming service that is a blend between Bittorrent and traditional online video streaming. The idea was to decentralize the streaming service so that instead of only using the cloud servers, the clients also source blocks of videos from other clients. The behavior implemented is that clients default to downloading from their peers before downloading from our servers.

### Components
*   **[SeederServer](docker-containers/seedbox-container/SeederServer):** Server that holds all video files and transfers video blocks to clients as they are requested.
*   **[ClientManager](docker-containers/cmanager-container/ClientManager):** RESTful service that provides the client with information on the files available for download and the neighbors they can download from.
*   **[PostgresDB](docker-containers/database-container):** Maintains global state of files available and users who can seed them.
*   **[Client](client):** Client for users to access the service.

### Deployment
The SeederServer, ClientManager, and Postgres DB are all containerized services deployed to Google Container Clusters with Kubernetes. Deployment scripts and configuration files can be found: [here](docker-containers). 

### Contributors
* @lebastaq
* @freddygv
