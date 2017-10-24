package pt.fcup;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;
import javax.ws.rs.core.Application;

/**
*   Creates a Grizzly HTTP server and exposes a
*   bunch of resources for the client to use
**/
class ClientManager extends ResourceConfig{

    private DBManager db;

    // Base URI the Grizzly server will listen on
    public static final String BASE_URI = "http://localhost:8080/trabblex/";

    public static void main(String[] args)
    {
        ClientManager cm = new ClientManager();

    }

    /**
     * Start Grizzly HTTP server exposing JAX-RS resources
     */
    public static HttpServer startServer() {
        // create a resource config that scans for JAX-RS resources and providers
        final ResourceConfig rc = new ResourceConfig().packages("pt.fcup");

        // create and start a new instance of grizzly http server
        // expose the Jersey application at BASE_URI
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);

    }

    /**
    * Create server and everything else necessary
    */
    public ClientManager ()
    {

        // start server
        try
        {
            final HttpServer server = startServer();
            System.out.println("Created server");
            System.in.read();
            server.stop();

        }
        catch(Exception e)
        {
            System.out.println("Error: " + e);

        }

    }

}
