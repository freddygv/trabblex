package pt.fcup;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import pt.fcup.generated.*;

public class Seeder {
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
        String regString = "Registering seeder with file: " + fileName;

        try(com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize()) {
            RegistrableIPrx register = RegistrableIPrx.checkedCast(communicator.stringToProxy("SeederRegistration:default -h localhost -p 8081"));
            register.registerSeeder(regString);
            return true;

        }

        // TODO: Pull query out into Portal class, Seeder won't have DB so this method should call an ICE message
        // TODO: Edit neighbor list as well
//        String insertionQuery = "INSERT INTO seeders(seeder_ip, file_hash, file_name, file_size, protocol, " +
//                                                     "port, video_size_x, video_size_y, bitrate) " +
//                                 "VALUES('" + ip + "', '" + fileHash + "', '" + fileName + "', '" + fileSize + "'" +
//                                        ",'" + PROTOCOL + "', '" + port + "', '" + video_size_x + "', '" + video_size_y + "'" +
//                                        ",'" + bitrate + "');";
//
//        try {
//            dbUpdate(insertionQuery);
//            return true;
//
//        } catch (ClassNotFoundException | IOException | SQLException ec) {
//            System.err.println("Seeder registration: DB insert failed.");
//            return false;
//
//        }
    }

    public boolean deregisterSeeder() {
        String deregString = "De-registering seeder with file: " + fileName;

        try(com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize()) {
            RegistrableIPrx register = RegistrableIPrx.checkedCast(communicator.stringToProxy("SeederRegistration:default -h localhost -p 8081"));
            register.deregisterSeeder(deregString);
            return true;

        }

        // TODO: Pull query out into Portal class, Seeder won't have DB so this method should call an ICE message
        // TODO: Edit neighbor list as well
//        String deletionQuery = "DELETE FROM seeders WHERE file_hash = '" + fileHash + "';";
//
//        try {
//            dbUpdate(deletionQuery);
//            return true;
//
//        } catch (ClassNotFoundException | IOException | SQLException ec) {
//            System.err.println("Seeder de-registration: DB delete failed.");
//            return false;
//
//        }

    }

    private void dbUpdate(String query) throws ClassNotFoundException, IOException, SQLException {
        DBManager db = new DBManager();
        db.singleUpdate(query);

    }

    public void setHost() {
        ip = "localhost";
        port = "8080";

    }
}
