package pt.fcup;

import org.json.JSONObject;
import pt.fcup.generated.*;
import pt.fcup.exception.FileHashException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
    private final File videoFile;

    public Seeder(String fileName, JSONObject fileMetadata) throws FileHashException {
        this.fileName = fileName;

        filepath = BASE_PATH + fileMetadata.get("filepath").toString();
        fileSize = fileMetadata.get("fileSize").toString();
        video_size_x = fileMetadata.get("video_size_x").toString();
        video_size_y = fileMetadata.get("video_size_y").toString();
        bitrate = fileMetadata.get("bitrate").toString();

//        fileHash = generateFileHash();

        videoFile = new File(filepath);
        try {
            fileHash = hashFile(videoFile);
            System.out.println(fileHash);

        } catch (FileHashException e) {
            throw e;

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

        for (int retries = 0; retries < MAX_RETRIES; retries++) {
            try (com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize()) {
                RegistrableIPrx register = RegistrableIPrx.checkedCast(communicator.stringToProxy("SeederRegistration:default -h localhost -p 8081"));
                regResult = register.registerSeeder(insertionQuery);
            }

            if (regResult) {
                break;
            }
        }

        return regResult;

    }

    // TODO: Should this also close the socket if the seeder has no client connections?
    public boolean deregisterSeeder() {
        String deletionQuery = "DELETE FROM seeders WHERE file_hash = '" + fileHash + "';";

        boolean regResult = false;

        for (int retries = 0; retries < MAX_RETRIES; retries++) {
            try (com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize()) {
                RegistrableIPrx deregister = RegistrableIPrx.checkedCast(communicator.stringToProxy("SeederRegistration:default -h localhost -p 8081"));
                regResult = deregister.deregisterSeeder(deletionQuery);
            }

            if (regResult) {
                break;
            }
        }

        return regResult;
    }

    // Source: http://www.codejava.net/coding/how-to-calculate-md5-and-sha-hash-values-in-java
    private static String convertByteArrayToHexString(byte[] arrayBytes) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < arrayBytes.length; i++) {
            stringBuffer.append(Integer.toString((arrayBytes[i] & 0xff) + 0x100, 16)
                    .substring(1));
        }
        return stringBuffer.toString();
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

            return convertByteArrayToHexString(hashedBytes);

        } catch (NoSuchAlgorithmException | IOException e) {
            throw new FileHashException("Could not generate hash from file", e);

        }
    }

    public void setHost(int port) {
        // TODO: Get IP from environment variable, will be the same for all seeders
        ip = "localhost";

        this.port = Integer.toString(port);

        System.out.println("Seeder IP:PORT for " + fileName + " is " + ip + ":" + port);

    }
}
