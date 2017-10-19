package pt.fcup;

import java.util.HashMap;
import pt.fcup.generated.*;

public class Seeder {
    private final int MAX_RETRIES = 4;

    private final String PROTOCOL = "TCP";
    private String ip;
    private String port;

    private final String filepath;
    private final String fileHash;
    private final String fileName;
    private final String fileSize;
    private final String video_size_x;
    private final String video_size_y;
    private final String bitrate;

    public Seeder(HashMap<String, String> fileMetadata) {
        filepath = fileMetadata.get("filepath");
        fileHash = fileMetadata.get("fileHash");
        fileName = fileMetadata.get("fileName");
        fileSize = fileMetadata.get("fileSize");
        video_size_x = fileMetadata.get("video_size_x");
        video_size_y = fileMetadata.get("video_size_y");
        bitrate = fileMetadata.get("bitrate");

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

    public void setHost(int port) {
        // TODO: Get IP from environment variable, will be the same for all seeders
        ip = "localhost";

        this.port = Integer.toString(port);

        System.out.println("Seeder IP:PORT for " + fileName + " is " + ip + ":" + port);

    }
}
