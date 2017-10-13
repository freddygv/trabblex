package pt.fcup;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;

class ClientManager{


    // Base URI the Grizzly server will listen on
    public static final String BASE_URI = "http://localhost:8080/myapp/";

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
        final HttpServer server = startServer();
        System.out.println(String.format("Jersey app started with WADL available at "
               + "%sapplication.wadl\nHit enter to stop it...", BASE_URI));
        try{
            System.in.read();
        }
        catch(IOException e){
            // Do something !
        }
        server.stop();

    }
}
