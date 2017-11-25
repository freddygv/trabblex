package pt.fcup;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;
import javax.ws.rs.core.Application;

/**
 *   Creates a Grizzly HTTP server and exposes JAX-RS resources for the client
 **/
class ClientManager extends ResourceConfig {
    private final String BASE_URI = "http://0.0.0.0:8080/trabblex/";

    public static void main(String[] args) {
        ClientManager cm = new ClientManager();
        cm.run();

    }

    private void run() {
        // create a resource config that scans for JAX-RS resources and providers
        ResourceConfig rc = new ResourceConfig().packages("pt.fcup");

        // create and start a new instance of grizzly http server
        // expose the Jersey application at BASE_URI
        GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

}
