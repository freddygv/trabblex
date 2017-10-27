package pt.fcup;

import org.json.JSONObject;
import pt.fcup.generated.*;
import pt.fcup.exception.FileHashException;
import pt.fcup.generated.RegistrableIPrx;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

// TCP imports
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
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

    private String fileHash;
    private int numberOfChunks;
    private int maxChunkSizeInBytes;

    private List<String> chunkHashes = new ArrayList<>();

    public Seeder(String fileName, JSONObject fileMetadata, int chunkSize) {
        this.fileName = fileName;

        filepath = BASE_PATH + fileMetadata.get("filepath").toString();
        fileSize = fileMetadata.get("fileSize").toString();
        video_size_x = fileMetadata.get("video_size_x").toString();
        video_size_y = fileMetadata.get("video_size_y").toString();
        bitrate = fileMetadata.get("bitrate").toString();
        maxChunkSizeInBytes = chunkSize; // 10 Mb


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

    /**
     * Registers Seeder/file with the portal and sends chunk hashes to update swarm/neighborhood
     *
     * TODO: Send arguments instead, and build query in the portal
     * @return true if file and neighborhood registrations are successful
     */
    public boolean registerSeeder() {
        // extract file size 
        String[] parts = fileSize.split(" |\\.");
        int fileSizeInt = Integer.parseInt(parts[0]);

        // extract bitrate
        parts = fileSize.split(" |\\.");
        int bitrateInt = Integer.parseInt(parts[0]);

        String insertionQuery = "INSERT INTO seeders(seeder_ip, file_hash, file_name, file_size, protocol, " +
                "port, video_size_x, video_size_y, bitrate) " +
                "VALUES('" + ip + "', '" + fileHash + "', '" + fileName + "', '" + fileSizeInt + "'" +
                ",'" + PROTOCOL + "', '" + port + "', '" + video_size_x + "', '" + video_size_y + "'" +
                ",'" + bitrateInt + "');";

        System.out.println(insertionQuery);

        boolean regResult = false;
        boolean neighborhoodResult = false;

        // Retry policy
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

    /*
        Will open a TCP connection and stream a chunk only one time
        Then, close the connection
        @param seedNumber the relative number of the seeder
            eg, seeder x out of 20
    */
    public boolean transferTCP(int seedNumber, String chunkHash)
    {
        try
        {

            // TODO update Database to indicate that
            // a new chunk_owner has been created

            // TODO once client has finished downloading,
            // update chunk_owners

            // NOTE: how to parallelize this ? Thread ?

            ServerSocket ssock = new ServerSocket(Integer.parseInt(port + seedNumber));
            Socket socket = ssock.accept();
            
            InetAddress IA = InetAddress.getByName(ip); 
            
            // TODO atm - send entire file
            // next, send only a chunk
            File file = new File(filepath);

            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis); 
              
            //Get socket's output stream
            OutputStream os = socket.getOutputStream();
                    
            //Read File Contents into contents array 
            byte[] contents;
            long fileLength = file.length(); 
            long current = 0;
             
            while(current!=fileLength){ 
                int size = 10000;
                if(fileLength - current >= size)
                    current += size;    
                else{ 
                    size = (int)(fileLength - current); 
                    current = fileLength;
                } 
                contents = new byte[size]; 
                bis.read(contents, 0, size); 
                os.write(contents);

                // update every 20%
                if((current*100)/fileLength % 20 == 0)
                    System.out.print("Sending file ... "+(current*100)/fileLength+"% complete!");
            }   
            
            os.flush(); 
            //File transfer done. Close the socket connection!
            socket.close();
            ssock.close();
            System.out.println("File sent succesfully!");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return true;
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

            int bytesRead = 0;

            while((bytesRead = bi.read(chunkBuffer)) > 0) {
                chunkName = filepath + "-" +  Integer.toString(chunkIndex++);
                File currentChunk = new File(chunkName);

                try (FileOutputStream fo = new FileOutputStream(currentChunk)) {
                    fo.write(chunkBuffer, 0, bytesRead);
                    fo.close();
                    chunkHash = hashFile(currentChunk);
                    chunkHashes.add(chunkHash);
                }
            }

            numberOfChunks = chunkIndex; // Number of chunks

        }

    }


    /**
     * Stores Seeder address and port
     * @param port first port in 20-port range
     */
    public void setHost(int port) {
        // TODO: Get IP from environment variable, will be the same for all seeders
        ip = "localhost";

        this.port = Integer.toString(port);

        System.out.println("Seeder IP:PORT for " + fileName + " is " + ip + ":" + port);

    }
}
