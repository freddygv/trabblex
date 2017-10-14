package pt.fcup;

import org.json.JSONObject;
import org.json.JSONArray;
import java.io.IOException;
import java.sql.*;
import java.io.FileInputStream;
import java.util.Properties;

public class DBManager {

    protected final String HOST = "127.0.0.1:5432";
    protected final String DB_NAME = "prod";
    protected final String DB_URL = "jdbc:postgresql://" + HOST + "/" + DB_NAME;
    protected final String DB_PROPS_LOCATION = "db.properties";
    protected Properties DB_PROPS;

    protected Connection conn;
    protected Statement statement;
    protected ResultSet resultSet;
    protected ResultSetMetaData metaData;
    protected int numColumns;

    protected JSONObject row;
    protected JSONArray table;


    public DBManager() throws IOException, ClassNotFoundException{
        try {
            Class.forName("org.postgresql.Driver");

        } catch (ClassNotFoundException e) {
            System.err.println("Postgres Driver not found.");
            throw e;

        }

        try {
            loadDBProperties(DB_PROPS_LOCATION);

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
    public JSONArray queryTable(String query) throws SQLException {
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

            table = new JSONArray();

            while (resultSet.next()) {
                row = new JSONObject();

                for (int i = 1; i <= numColumns; i++) {
                    row.put(metaData.getColumnName(i), resultSet.getString(i));
                }

                table.put(row);
            }

            resultSet.close();
            statement.close();
            conn.close();

            return table;

        } catch (SQLException e) {
            System.err.println("Query execution failed.");
            throw e;

        }


    }

    protected Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_PROPS);

    }

    protected void loadDBProperties(String filename) throws IOException {
        FileInputStream input = new FileInputStream(filename);
        DB_PROPS = new Properties();
        DB_PROPS.load(input);
        input.close();

    }

}
