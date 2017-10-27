package pt.fcup;

import pt.fcup.exception.FileHashException;
import java.io.IOException;

public class RequestableI implements pt.fcup.generated.RequestableI {
    /**
     * Implementation of RPC requests for Seeders from the ClientManager.
     * If a client requests a seeder, provide existing, else create and register one.
     *
     * @param fileName of video
     * @param current Ice object
     * @return number of chunks that the video was split into
     */
    public int requestSeeder(String fileName, com.zeroc.Ice.Current current) {
        int numChunks = 0;

        try {

            // Try to get an existing instance of a Seeder for a given file
            Seedbox mySB = Seedbox.getSeedbox();
            Seeder newSeed = mySB.seederHashMap.get(fileName);

            // If there is no instance, create one
            if(newSeed == null) {
                System.out.println("No seeder present, creating new seeder.");
                newSeed = mySB.createSingleSeeder(fileName);

            } else {
                System.out.println("Seeder already exists.");

            }

            numChunks = newSeed.getNumberOfChunks();

//            System.in.read(); // TODO: Should this line come back? Why is the seeder reading?

        } catch (IOException | FileHashException e) {
            // TODO: Handle seeder creation failure in ClientManager, if 0 is returned, consider failure
            System.err.println("Error generating seeder.");


        } finally {
            return numChunks;

        }

    }

    /**
     * TODO: Implement
     * @param current
     * @return
     */
    public boolean disconnectClient(com.zeroc.Ice.Current current) {
        boolean successfulDisconnect = false;

        System.err.println("Client disconnect not implemented, returning false");
        return successfulDisconnect;
    }
}
