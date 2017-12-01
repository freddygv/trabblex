package pt.fcup;

import pt.fcup.exception.FileHashException;

import java.io.*;
import org.json.JSONObject;

import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


class Seeder {
    private final int CHUNK_SIZE_BYTES = 10 * 1024 * 1024; // 10 MB
    private final String HASHING_ALGORITHM = "SHA-256";
    private final String BASE_PATH = "videos/";

    private final String filename;
    private final String videoName;
    private final int port;
    private String ip;

    private String fileHash;

    private List<String> chunkHashes;
    private List<String> chunkIDs;

    Seeder(String filename, int port, JSONObject fileMetadata) throws UnknownHostException {

        this.filename = filename;

        ip = System.getenv("SEEDBOX_IP");
        if (ip == null) {ip = "localhost";}

        this.port = port;

        videoName = fileMetadata.get("videoName").toString();

    }

    String getVideoName() {
        return videoName;
    }

    /**
     * Registers Seeder/file with the portal and sends chunk hashes to update swarm/neighborhood
     *
     * @return true if file and neighborhood registrations are successful
     */
    void registerSeeder() throws ClassNotFoundException, IOException, SQLException {
     try {
            System.out.println("Registering seeder for " + videoName + " at port " + port);
            registerInVideos();
            registerInChunkOwners();

        } catch (Exception e) {
            deregisterSeeder();
            throw e;

        }

    }

    /**
     * Update the videos table to indicate there's an active seeder
     * @return true if registration was completed without exceptions
     */
    private void registerInVideos() throws ClassNotFoundException, IOException, SQLException {
        try {
            System.out.println(String.format("Seeding Seeder for: %s as active", fileHash));

            String updateQuery = "UPDATE videos SET seeder_is_active = 't' WHERE file_hash = '%s';";
            dbUpdate(String.format(updateQuery, fileHash));

        } catch (ClassNotFoundException | IOException | SQLException ec) {
            System.err.println("Seeder registration: DB update failed.");
            ec.printStackTrace();
            throw ec;

        }

    }

    /**
     * Update the chunk_owners table to populate seeder IP for each chunk
     * @return true if registration was completed without exceptions
     */
    private void registerInChunkOwners() throws ClassNotFoundException, IOException, SQLException {
        String baseUpdateQuery = "INSERT INTO chunk_owners(file_hash, chunk_hash, chunk_id, "
                                                        + "owner_ip, owner_port, is_seeder) ";

        try {
            for (int i = 0; i < chunkHashes.size(); i++) {
                String chunkHash = chunkHashes.get(i);
                String chunkID = chunkIDs.get(i);

                String updateQuery = baseUpdateQuery
                                     + String.format("VALUES('%s', '%s', '%s', '%s', '%d', 't');",
                                                     fileHash,
                                                     chunkHash,
                                                     chunkID,
                                                     ip,
                                                     port);

                System.out.println(String.format("Seeding chunkHash #%s for: %s as active", chunkHash,
                                                                                            chunkID));
                dbUpdate(updateQuery);

            }

        } catch (ClassNotFoundException | IOException | SQLException ec) {
            System.err.println("Neighborhood update: DB insert failed.");
            ec.printStackTrace();
            throw ec;

        }
    }

    /**
     * Deletes from videos table, removing de-registered seeder
     * @return true if records successfully deleted
     */
    private void deregisterSeeder() throws ClassNotFoundException, IOException, SQLException {
        String seederDeletionQry = "UPDATE videos SET seeder_is_active = 'f' WHERE file_hash = '%s';";
        String neighborDeletionQry = "DELETE FROM chunk_owners WHERE file_hash = '%s';";

        try {
            dbUpdate(String.format(seederDeletionQry, fileHash));
            dbUpdate(String.format(neighborDeletionQry, fileHash));

        } catch (ClassNotFoundException | IOException | SQLException ec) {
            System.err.println("Seeder de-registration: DB delete failed.");
            ec.printStackTrace();
            throw ec;

        }
    }

    /**
     * Generate SHA-256 hash for file, chunk file, then hash chunks.
     * Hashes used for file verification on the client-side.
     * @return true if video processed successfully
     */
    void processVideo() throws FileHashException, IOException {
        try {
            fileHash = hashFile(new File(BASE_PATH + filename));
            System.out.println("SHA-256 Hash: " + fileHash);
            chunkAndHash();

        } catch (IOException | FileHashException e) {
            System.err.println("Error chunking file and hashing chunks.");
            throw e;

        }
    }

    /**
     * Converts byte array to hex string
     * https://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
     */
    private final char[] hexArray = "0123456789ABCDEF".toCharArray();
    String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Read file to byte array with buffer, hash, then convert to hex string
     * http://www.codejava.net/coding/how-to-calculate-md5-and-sha-hash-values-in-java
     * @param file
     * @return hex string hash
     * @throws FileHashException
     */
    //
    private String hashFile(File file) throws FileHashException {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance(HASHING_ALGORITHM);

            byte[] bytesBuffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(bytesBuffer)) > 0) {
                digest.update(bytesBuffer, 0, bytesRead);
            }

            byte[] hashedBytes = digest.digest();

            return bytesToHex(hashedBytes);

        } catch (NoSuchAlgorithmException | IOException e) {
            throw new FileHashException("Could not generate hash from file", e);

        }
    }

    /**
     * Reads files with a buffer set to the max chunk size
     * and writes them out to the original video directory
     * https://stackoverflow.com/questions/10864317/how-to-break-a-file-into-pieces-using-java
     * @throws IOException
     * @throws FileHashException
     */
    private void chunkAndHash() throws IOException, FileHashException {
        byte[] chunkBuffer = new byte[CHUNK_SIZE_BYTES];

        try (FileInputStream fi = new FileInputStream(new File(BASE_PATH + filename));
             BufferedInputStream bi = new BufferedInputStream(fi)) {

            chunkHashes = new ArrayList<>();
            chunkIDs = new ArrayList<>();

            // Filename without extension
            String strippedFile = filename.substring(0, filename.lastIndexOf("."));

            int chunkIndex = 0;

            int bytesRead;
            while ((bytesRead = bi.read(chunkBuffer)) > 0) {
                String chunkName = "chunks/" + strippedFile + "/" + filename +  "-" +  chunkIndex;
                File currentChunk = new File(chunkName);

                try (FileOutputStream fo = new FileOutputStream(currentChunk)) {
                    fo.write(chunkBuffer, 0, bytesRead);
                    fo.close();
                    String chunkHash = hashFile(currentChunk);

                    chunkHashes.add(chunkHash);
                    chunkIDs.add(Integer.toString(chunkIndex));

                    chunkIndex++;
                }
            }
        }
    }

    /**
     * @param query DB insert/update/delete to execute
     */
    private void dbUpdate(String query) throws ClassNotFoundException, IOException, SQLException {
        Seedbox.db.singleUpdate(query);

    }
}
