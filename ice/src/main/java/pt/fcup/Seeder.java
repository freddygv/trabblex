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

    public void registerInPortal() {
        System.out.println("Registering seeder with file: " + filename);

//        try {
//            DBManager db = new DBManager();
//            String sampleQuery = "SELECT pg.tablename FROM pg_catalog.pg_tables pg WHERE pg.tablename = 'seeders';";
//            System.out.println(db.queryTable(sampleQuery).toString());
//
//        } catch (ClassNotFoundException | IOException | SQLException ec) {
//            ec.printStackTrace();
//
//        }
    }

    public void deregisterInPortal() {
        System.out.println("De-registering seeder with file: " + filename);
    }

    public void setHost() {
        ip = "localhost";
        port = "8080";

    }
}
