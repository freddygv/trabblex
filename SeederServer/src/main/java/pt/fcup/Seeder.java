package pt.fcup;

import org.json.JSONObject;
import pt.fcup.generated.*;
import pt.fcup.exception.FileHashException;
import pt.fcup.generated.RegistrableIPrx;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class Seeder {
    private final int MAX_RETRIES = 4;
    private final String HASHING_ALGORITHM = "SHA-256";
    private final String PROTOCOL = "TCP";
    private String ip;
    private String port;

    private final String BASE_PATH = "videos/";
    private final String filepath;
    private final String fileName;
    private final String fileSize;
    private final String video_size_x;
    private final String video_size_y;
    private final String bitrate;
    private final String fileHash;

    private int numberOfChunks;
    private List<String> chunkHashes = new ArrayList<>();

    public Seeder(String fileName, JSONObject fileMetadata) throws FileHashException, IOException {
        this.fileName = fileName;

        filepath = BASE_PATH + fileMetadata.get("filepath").toString();
        fileSize = fileMetadata.get("fileSize").toString();
        video_size_x = fileMetadata.get("video_size_x").toString();
        video_size_y = fileMetadata.get("video_size_y").toString();
        bitrate = fileMetadata.get("bitrate").toString();

        try {
            fileHash = hashFile(new File(filepath));
            System.out.println("SHA-256 Hash: " + fileHash);

        } catch (FileHashException e) {
            throw e;

        }

        try {
            chunkAndHash();
            System.out.println("Number of chunks: " + numberOfChunks);

        } catch (IOException | FileHashException e) {
            e.printStackTrace();

        }


    }

    public String getPROTOCOL() {
        return PROTOCOL;
    }

    public String getIp() {
        return ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getFilepath() {
        return filepath;
    }

    public String getFileHash() {
        return fileHash;
    }

    public int getNumberOfChunks() {
        return numberOfChunks;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileSize() {
        return fileSize;
    }

    public String getVideo_size_x() {
        return video_size_x;
    }

    public String getVideo_size_y() {
        return video_size_y;
    }

    public String getBitrate() {
        return bitrate;
    }

    public boolean registerSeeder() {
        String insertionQuery = "INSERT INTO seeders(seeder_ip, file_hash, file_name, file_size, protocol, " +
                "port, video_size_x, video_size_y, bitrate) " +
                "VALUES('" + ip + "', '" + fileHash + "', '" + fileName + "', '" + fileSize + "'" +
                ",'" + PROTOCOL + "', '" + port + "', '" + video_size_x + "', '" + video_size_y + "'" +
                ",'" + bitrate + "');";

        boolean regResult = false;
        boolean neighborhoodResult = false;

        String[] hashStringArray = chunkHashes.toArray(new String[chunkHashes.size()]);
        for (int retries = 0; retries < MAX_RETRIES; retries++) {
            try (com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize()) {
                RegistrableIPrx register = RegistrableIPrx.checkedCast(communicator.stringToProxy("SeederRegistration:default -h localhost -p 8081"));

                regResult = register.registerSeeder(insertionQuery);
                neighborhoodResult = register.sendHashes(hashStringArray, fileHash, ip, port);

            }

            if (regResult && neighborhoodResult) {
                break;
            }
        }

        return regResult && neighborhoodResult;

    }

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

    // https://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
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

    // TODO: Edit this function? Also dig into what it's doing more
    // Source: http://www.codejava.net/coding/how-to-calculate-md5-and-sha-hash-values-in-java
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

    // https://stackoverflow.com/questions/10864317/how-to-break-a-file-into-pieces-using-java
    private void chunkAndHash() throws IOException, FileHashException {
        int maxChunkSizeInBytes = 10 * 1024 * 1024; // 10 Mb
        byte[] chunkBuffer = new byte[maxChunkSizeInBytes];
        String chunkName;
        String chunkHash;
        int chunkIndex = 0;

        try (FileInputStream fi = new FileInputStream(new File(filepath));
             BufferedInputStream bi = new BufferedInputStream(fi)) {

            int bytesRead = 0;

            while((bytesRead = bi.read(chunkBuffer)) > 0) {
                chunkName = filepath + "-" +  Integer.toString(chunkIndex++);
                File currentChunk = new File(chunkName);

                try (FileOutputStream fo = new FileOutputStream(currentChunk)) {
                    fo.write(chunkBuffer, 0, bytesRead);
                    chunkHash = hashFile(currentChunk);
                    chunkHashes.add(chunkHash);
                }
            }

            numberOfChunks = chunkIndex; // Number of chunks

        }

    }


    public void setHost(int port) {
        // TODO: Get IP from environment variable, will be the same for all seeders
        ip = "localhost";

        this.port = Integer.toString(port);

        System.out.println("Seeder IP:PORT for " + fileName + " is " + ip + ":" + port);

    }
}
