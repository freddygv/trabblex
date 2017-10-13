package pt.fcup;

import java.io.IOException;
import java.sql.*;
import java.io.FileInputStream;
import java.util.Properties;

public class DBManager {

    private String HOST = "127.0.0.1:5432";
    private String DB_NAME = "prod";
    private String DB_URL = "jdbc:postgresql://" + HOST + "/" + DB_NAME;
    private Properties DB_PROPS = getProperties("db.properties");

    private Connection conn;
    private Statement st;
    private ResultSet rs;


    public DBManager() {
        try {
            Class.forName("org.postgresql.Driver");

        } catch(ClassNotFoundException ex) {
            ex.printStackTrace();

        }
    }

    public void printQuery(String query) {
        try {
            st = getConnection().createStatement();
            rs = st.executeQuery(query);

            while (rs.next()) {
                // Just printing the first column of the returned set right now
                System.out.println(rs.getString(1));

            }

            rs.close();
            st.close();

        } catch (SQLException e) {
            e.printStackTrace();

        }
    }

    private Connection getConnection() {
        try {
            conn = DriverManager.getConnection(DB_URL, DB_PROPS);
            System.out.println("Connected to prod db");
            return conn;

        } catch(SQLException e) {
            e.printStackTrace();
            return null;

        }
    }

    private Properties getProperties(String filename) {
        Properties props = new Properties();

        try {
            FileInputStream input = new FileInputStream(filename);
            props.load(input);

        } catch(IOException e) {
            e.printStackTrace();

        }

        return props;
    }

}
