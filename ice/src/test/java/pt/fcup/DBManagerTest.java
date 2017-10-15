package pt.fcup;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DBManagerTest {
    private DBManager testDB;
    private String resultString;
    private String expectedString;

    @BeforeEach
    void setUp() throws Exception {
        testDB = new DBManager();
    }

    @Test
    void queryTable() throws Exception {
        String sampleQuery = "SELECT schemaname, tablename FROM pg_catalog.pg_tables pg " +
                "WHERE tablename IN ('chunk_owners','seeders') ORDER BY tablename;";

        expectedString = "[{\"schemaname\":\"public\",\"tablename\":\"chunk_owners\"}," +
                "{\"schemaname\":\"public\",\"tablename\":\"seeders\"}]";
        resultString = testDB.queryTable(sampleQuery).toString();

        assertEquals(expectedString, resultString);
    }

    @Test
    void singleInsertIntoDB() throws Exception {
        String insertQuery = "INSERT INTO chunk_owners(file_hash, chunk_hash, owner_ip, is_active) " +
                "VALUES('file-hash-1', 'chunk-hash-1', 'localhost', '1');";
        testDB.singleUpdate(insertQuery);

        String selectQuery = "SELECT file_hash, chunk_hash, owner_ip, is_active FROM chunk_owners " +
                "WHERE file_hash = 'file-hash-1'";
        JSONArray resultArray =  testDB.queryTable(selectQuery);
        JSONObject resultObject = resultArray.getJSONObject(0);

        String fileHash = resultObject.getString("file_hash");
        String chunkHash = resultObject.getString("chunk_hash");
        String ownerIP = resultObject.getString("owner_ip");
        String isActive = resultObject.getString("is_active");

        resultString = "[{\"file_hash\":\"" + fileHash + "\",\"chunk_hash\":\"" + chunkHash +  "\"," +
                "\"owner_ip\":\"" + ownerIP + "\",\"is_active\":\"" + isActive + "\"}]";

        expectedString = "[{\"file_hash\":\"file-hash-1\",\"chunk_hash\":\"chunk-hash-1\"," +
                "\"owner_ip\":\"localhost\",\"is_active\":\"t\"}]";

        assertEquals(expectedString, resultString);

    }

    @Test
    void singleDeletionFromDB() throws Exception {
        String deletionQuery = "DELETE FROM chunk_owners WHERE file_hash = 'file-hash-1'";
        testDB.singleUpdate(deletionQuery);

        String selectQuery = "SELECT * FROM chunk_owners WHERE file_hash = 'file-hash-1'";

        resultString = testDB.queryTable(selectQuery).toString();
        expectedString = "[]";

        assertEquals(expectedString, resultString);

    }

}