package pt.fcup;

import java.util.*;
import javax.ws.rs.*;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;

/**
 * Makes requests to the ClientManager
 */

public class JerseyClient {

    protected String host;
    protected String url;
    private Client client;

    public JerseyClient() {
        client = ClientBuilder.newClient();
        setTarget();

    }

    private void setTarget() {
        String path = "/trabblex/clientmanager/";

        host = (SimpleClient.MODE == "local") ? "http://127.0.0.1:8080"
                                              : "http://35.195.218.215:8080";

        url = host + path;
    }

    public String query(String path) {
        return query(path, null, null);

    }

    public String query(String path, String param) {
        return query(path, param, null);

    }

    public String query(String path, String param, Map<String,String> queryParams)
            throws ProcessingException, NotFoundException, InternalServerErrorException {

        if (param != null) {
            path = path + "/";

        } else {
            param = "";

        }

        WebTarget resourceWebTarget = client.target(url).path(path + param);

        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            resourceWebTarget = resourceWebTarget.queryParam(entry.getKey(), entry.getValue());

        }

        String result = resourceWebTarget.request(MediaType.TEXT_PLAIN).get(String.class);

        return result;
    }

}