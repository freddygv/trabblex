package pt.fcup;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

public class Seeder {
    private final String PROTOCOL = "TCP";
    private String ip;
    private String port;

    private final String filepath;
    private final String fileHash;
    private final String filename;
    private final String video_size_x;
    private final String video_size_y;

    public Seeder(HashMap<String, String> fileMetadata) {
        filepath = fileMetadata.get("filepath");
        fileHash = fileMetadata.get("fileHash");
        filename = fileMetadata.get("filename");
        video_size_x = fileMetadata.get("video_size_x");
        video_size_y = fileMetadata.get("video_size_y");

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

    public String getFilename() {
        return filename;
    }

    public String getVideo_size_x() {
        return video_size_x;
    }

    public String getVideo_size_y() {
        return video_size_y;
    }

    public boolean registerInPortal() {
        System.out.println("Registering seeder with file: " + filename);

//        String insertionQuery = "";
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

        // TODO: Remove when ready
        return true;
    }

    public boolean deregisterInPortal() {
        System.out.println("De-registering seeder with file: " + filename);

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

        // TODO: Remove when ready
        return true;
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
