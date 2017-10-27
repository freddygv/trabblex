package pt.fcup;

import pt.fcup.exception.FileHashException;
import java.io.IOException;

public class RequestableI implements pt.fcup.generated.RequestableI {
    public int requestSeeder(String fileName, com.zeroc.Ice.Current current) {
        int numChunks = 0;

        try {
            Seedbox mySB = Seedbox.getSeedbox();
            Seeder newSeed = mySB.seederHashMap.get(fileName);

            if(newSeed == null) {
                System.out.println("No seeder present, creating new seeder.");
                newSeed = mySB.createSingleSeeder(fileName);

            } else {
                System.out.println("Seeder already exists.");

            }

            numChunks = newSeed.getNumberOfChunks();

//            System.in.read(); // TODO: Should this come back? Why is the seeder reading?

        } catch (IOException | FileHashException e) {
            // TODO: Handle seeder creation failure in ClientManager, if 0 is returned, consider failure
            System.err.println("Error generating seeder.");


        } finally {
            return numChunks;

        }

    }

    public boolean disconnectClient(com.zeroc.Ice.Current current) {
        boolean successfulDisconnect = false;

        System.err.println("Client disconnect not implemented, returning false");
        return successfulDisconnect;
    }
}
