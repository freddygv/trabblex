package pt.fcup;

import java.io.FileInputStream;
import java.io.IOException;
import org.json.JSONObject;
import org.json.JSONArray;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.*;
import java.util.Properties;

public class DBManager {

    private final String DB_PROPS_LOCATION = "db.properties";
    private final String DB_NAME = "prod";
    private String DB_URL;

    private Properties DB_PROPS;

    public DBManager() throws IOException, ClassNotFoundException {
        setPostgresURL();
        loadDriver();
        loadDBProperties(DB_PROPS_LOCATION);

    }

    /**
     * Attempts to resolve DB address with k8s DNS.
     * If it fails, the DB is running locally.
     */
    private void setPostgresURL() {
        String host;

        try {
            host = InetAddress.getByName("postgres-server").getHostAddress();

        } catch (UnknownHostException e) {
            host = "127.0.0.1:5432";

        }

        DB_URL = "jdbc:postgresql://" + host + "/" + DB_NAME;

    }

    /**
     * Load Postgres JDBC driver
     */
    private void loadDriver() throws ClassNotFoundException {
        try {
            Class.forName("org.postgresql.Driver");

        } catch (ClassNotFoundException e) {
            System.err.println("Postgres Driver not found.");
            throw e;

        }
    }

    /**
     * Loads username and password from local properties file
     */
    protected void loadDBProperties(String filename) throws IOException {
        try (FileInputStream input = new FileInputStream(filename)) {
            DB_PROPS = new Properties();
            DB_PROPS.load(input);

        } catch (IOException e) {
            System.err.println("Error loading properties file.");
            throw e;

        }

    }

    /**
     * @return JSONArray: [{col1: row1-value, col2: row1-value},
     *                     {col1: row2-value, col2: row2-value}]
     */
    public JSONArray queryTable(String query) throws SQLException {
        try (Connection conn = getConnection()) {

            try (Statement statement = conn.createStatement();
                 ResultSet resultSet = statement.executeQuery(query)) {

                ResultSetMetaData metaData = resultSet.getMetaData();
                int numColumns = metaData.getColumnCount();

                JSONArray table = new JSONArray();

                while (resultSet.next()) {
                    JSONObject row = new JSONObject();

                    for (int i = 1; i <= numColumns; i++) {
                        row.put(metaData.getColumnName(i), resultSet.getString(i));
                    }

                    table.put(row);
                }

                return table;

            } catch (SQLException e) {
                System.err.println("Query execution failed for: " + query);
                throw e;

            }

        } catch (SQLException e) {
            System.err.println("Connection to DB Failed.");
            throw e;

        }
    }

    /**
     * Executes a generic query that does not return data.
     * Used for update/insert/delete.
     */
    public void singleUpdate(String updateQuery) throws SQLException {
        try (Connection conn = getConnection()){
            try (PreparedStatement statement = conn.prepareStatement(updateQuery)) {
                statement.executeUpdate();

            } catch (SQLException e) {
                System.err.println("Query execution failed for: " + updateQuery);
                throw e;

            }

        } catch (SQLException e) {
            System.err.println("Connection to DB Failed.");
            throw e;

        }
    }


    protected Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_PROPS);

    }

}
