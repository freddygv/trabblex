package pt.fcup;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;

public class Portal
{
  // database manager
  // storage manager
  // seeders list

  private ClientManager cm;

  public void Portal()
  {
      System.out.println("Creating client manager");
      cm = new ClientManager();
  }


    public static void main(String[] args) {
        System.out.println("Creating portal …");
        Portal pt = new Portal();
    }
}
