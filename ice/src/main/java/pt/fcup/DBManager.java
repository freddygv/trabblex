package pt.fcup;

import org.json.JSONObject;
import org.json.JSONArray;
import java.io.IOException;
import java.sql.*;
import java.io.FileInputStream;
import java.util.Properties;

public class DBManager {

    private String HOST = "127.0.0.1:5432";
    private String DB_NAME = "prod";
    private String DB_URL = "jdbc:postgresql://" + HOST + "/" + DB_NAME;
    private Properties DB_PROPS;

    private Connection conn;
    private Statement statement;
    private ResultSet resultSet;
    private ResultSetMetaData metaData;
    private int numColumns;

    private JSONObject row = new JSONObject();
    private JSONArray table = new JSONArray();


    public DBManager() throws IOException, ClassNotFoundException{
        try {
            Class.forName("org.postgresql.Driver");

        } catch (ClassNotFoundException e) {
            System.err.println("Postgres Driver not found.");
            throw e;

        }

        try {
            DB_PROPS = getDBProperties("db.properties");

        } catch (IOException e) {
            System.err.println("Error loading properties file.");
            throw e;

        }
    }

    /**
     * @param query
     * @return JSONArray as string, [{row1-col1: value}, {row2-col1: value}]
     * @throws SQLException
     */
    public String queryTable(String query) throws SQLException {
        try {
            conn = getConnection();

        } catch (SQLException e) {
            System.err.println("Connection to DB Failed.");
            throw e;
        }

        try {
            statement = conn.createStatement();
            resultSet = statement.executeQuery(query);
            metaData = resultSet.getMetaData();
            numColumns = metaData.getColumnCount();

            while (resultSet.next()) {
                for (int i = 1; i <= numColumns; i++) {
                    row.put(metaData.getColumnName(i), resultSet.getString(i));
                }
                table.put(row);
            }

            resultSet.close();
            statement.close();
            conn.close();

            return table.toString();

        } catch (SQLException e) {
            System.err.println("Query execution failed.");
            throw e;

        }


    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_PROPS);

    }

    private Properties getDBProperties(String filename) throws IOException {
        Properties props = new Properties();

        FileInputStream input = new FileInputStream(filename);
        props.load(input);
        return props;

    }

}
