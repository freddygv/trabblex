package pt.fcup;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * Contains a list of peers and servers the contain a specific video chunk
 */
public class Chunk {
    private final String PROTOCOL = "TCP";

    private final int chunkNumber;

    private Map<String, Owner> owners = new HashMap<>();

    public Chunk(JSONObject obj) {
        chunkNumber = obj.getInt("chunk_id");
        addSource(obj);

    }

    public int getChunkNumber() {
        return chunkNumber;

    }

    /**
     * Adds ip/port/seeder info for a given owner to the list of owners for this chunk
     */
    public void addSource(JSONObject obj) {

        String ownerIP = obj.getString("owner_ip");
        int ownerPort = obj.getInt("owner_port");
        boolean isSeeder = obj.getString("is_seeder").equalsIgnoreCase("t");
        String chunkHash = obj.getString("chunk_hash");

        Owner ow = new Owner(ownerIP,
                             ownerPort,
                             PROTOCOL,
                             isSeeder,
                             chunkHash);

        owners.put(ownerIP + ":" + ownerPort, ow);

    }

    /**
     * If a source doesn't have the chunk we asked for,
     * or the hash is bad, then remove that source
     */
    public void removeOwner(String ip, int port) {
        owners.remove(ip + ":" + port);

    }


    /**
     * Peers preferred as sources.
     * If no peers are available, download from the seeder servers.
     */
    public Owner getSource() {
        if (owners.isEmpty()) {
            System.out.println("No source available.");
            return null;

        }

        Owner seeder = null;

        for (Map.Entry<String, Owner> chunkPair : owners.entrySet()) {
            Owner currentOwner = chunkPair.getValue();

            if (currentOwner.is_seeder) {
                seeder = currentOwner;

            } else {
                System.out.println("Got source from peer for chunk " + chunkNumber);
                return currentOwner;

            }
        }

        System.out.println("Got source from seeder for chunk " + chunkNumber);
        return seeder;

    }

    public int getNumberOfSources() {
        return owners.size();

    }
}