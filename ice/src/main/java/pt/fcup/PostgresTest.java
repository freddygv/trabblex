package pt.fcup;

import java.io.IOException;
import java.sql.*;
import java.io.FileInputStream;
import java.util.Properties;

public class PostgresTest {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://127.0.0.1:5432/prod";
        Properties dbProps = getProperties("db.properties");
        Connection db = getConnection(url, dbProps);

        String sampleQuery =  "SELECT pg.tablename FROM pg_catalog.pg_tables pg WHERE pg.tablename = 'seeders';";
        printQuery(db, sampleQuery);

    }

    private static void printQuery(Connection conn, String query) {
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                System.out.println(rs.getString(1));
            }

            rs.close();
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static Connection getConnection(String host, Properties props) {
        try {
            Connection conn = DriverManager.getConnection(host, props);
            System.out.println("Connected to prod db");
            return conn;
        } catch(SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Properties getProperties(String filename) {
        Properties props = new Properties();

        try {
            Class.forName("org.postgresql.Driver");
            FileInputStream input = new FileInputStream(filename);
            props.load(input);
        } catch(IOException e) {
            e.printStackTrace();
        } catch(ClassNotFoundException ex) {
            ex.printStackTrace();
        }

        return props;
    }

}
