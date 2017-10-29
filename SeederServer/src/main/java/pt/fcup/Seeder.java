package pt.fcup;

import pt.fcup.exception.FileHashException;
import pt.fcup.generated.RegistrableIPrx;

import java.io.*;
import org.json.JSONObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;


public class Seeder {
    private final int MAX_RETRIES = 4;
    private final String HASHING_ALGORITHM = "SHA-256";
    private final String BASE_PATH = "videos/";

    private final String filepath;
    private final String fileName;
    private final int port;
    private final String ip;

    private String fileHash;
    private int numberOfChunks;
    private int maxChunkSizeInBytes;

    private List<String> chunkHashes;
    private List<String> chunkIDs;

    public Seeder(String fileName, int port, JSONObject fileMetadata, int chunkSize) {
        this.fileName = fileName;

        // TODO: Get IP from environment variable, will be the same for all seeders
        ip = "localhost";
        this.port = port;
        System.out.println("Seeder IP:PORT for " + fileName + " is " + ip + ":" + port);

        filepath = BASE_PATH + fileMetadata.get("filepath").toString();
        maxChunkSizeInBytes = chunkSize; // 10 Mb


    }


    public String getFilepath() {
        return filepath;
    }

    public int getPort() {
        return port;
    }

    public String getIp() {
        return ip;
    }

    public int getNumberOfChunks() {
        return numberOfChunks;
    }

    public String getFileName() {
        return fileName;
    }

    /**
     * Registers Seeder/file with the portal and sends chunk hashes to update swarm/neighborhood
     *
     * TODO: Send arguments instead, and build query in the portal
     * @return true if file and neighborhood registrations are successful
     */
    public boolean registerSeeder() {

        boolean regResult = false;
        boolean neighborhoodResult = false;

        String[] hashStringArray = chunkHashes.toArray(new String[chunkHashes.size()]);
        String[] idStringArray = chunkIDs.toArray(new String[chunkIDs.size()]);

        // Retry policy
        for (int retries = 0; retries < MAX_RETRIES; retries++) {
            try (com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize()) {
                RegistrableIPrx register = RegistrableIPrx.checkedCast(communicator.stringToProxy("SeederRegistration:default -h localhost -p 8081"));

                regResult = register.registerSeeder(fileHash);

                neighborhoodResult = register.sendHashes(hashStringArray, idStringArray, fileHash, ip, port);

            }

            if (regResult && neighborhoodResult) {
                break;
            }
        }

        return regResult && neighborhoodResult;

    }

    // TODO: Should this also close the socket if the seeder has no client connections?
    public boolean deregisterSeeder() {
        boolean regResult = false;

        for (int retries = 0; retries < MAX_RETRIES; retries++) {
            try (com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize()) {
                RegistrableIPrx deregister = RegistrableIPrx.checkedCast(communicator.stringToProxy("SeederRegistration:default -h localhost -p 8081"));
                regResult = deregister.deregisterSeeder(fileHash);
            }

            if (regResult) {
                break;
            }
        }

        return regResult;
    }

    /**
     * Generate hash for file, chunk file, then hash chunks
     * @return true if video processed successfully
     */
    public boolean processVideo() throws FileHashException, IOException {
        try {
            fileHash = hashFile(new File(filepath));
            System.out.println("SHA-256 Hash: " + fileHash);

        } catch (FileHashException e) {
            System.err.println("Error generating file hash.");
            throw e;

        }

        try {
            chunkAndHash();
            System.out.println("Number of chunks: " + numberOfChunks);

        } catch (IOException | FileHashException e) {
            System.err.println("Error chunking file and hashing chunks.");
            throw e;

        }

        return true;
    }

    /**
     * Converts byte array to hex string
     * https://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
     */
    private final char[] hexArray = "0123456789ABCDEF".toCharArray();
    public String bytesToHex(byte[] bytes) {
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
     * Reads files with a buffer set to the max chunk size and writes them out to the original video directory
     * https://stackoverflow.com/questions/10864317/how-to-break-a-file-into-pieces-using-java
     * @throws IOException
     * @throws FileHashException
     */
    private void chunkAndHash() throws IOException, FileHashException {
        byte[] chunkBuffer = new byte[maxChunkSizeInBytes];
        String chunkName;
        String chunkHash;
        int chunkIndex = 0;

        try (FileInputStream fi = new FileInputStream(new File(filepath));
             BufferedInputStream bi = new BufferedInputStream(fi)) {

            chunkHashes = new ArrayList<>();
            chunkIDs = new ArrayList<>();

            int bytesRead = 0;

            while((bytesRead = bi.read(chunkBuffer)) > 0) {
                chunkName = filepath + "-" +  Integer.toString(chunkIndex);
                File currentChunk = new File(chunkName);

                try (FileOutputStream fo = new FileOutputStream(currentChunk)) {
                    fo.write(chunkBuffer, 0, bytesRead);
                    fo.close();
                    chunkHash = hashFile(currentChunk);

                    chunkHashes.add(chunkHash);
                    chunkIDs.add(Integer.toString(chunkIndex));

                    System.out.println(String.format("Chunk index: %d, Chunk hash: %s", chunkIndex, chunkHash));

                    chunkIndex++;
                }
            }

            numberOfChunks = chunkIndex; // Number of chunks

        }

    }

}
