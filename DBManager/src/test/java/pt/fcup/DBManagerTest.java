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

    // TODO restore once working
  /*  @BeforeEach
    void setUp() throws Exception {
        testDB = new DBManager();
    }

    @Test
    void queryTable() throws Exception {
        String sampleQuery = "SELECT schemaname, tablename FROM pg_catalog.pg_tables pg " +
                "WHERE tablename IN ('chunk_owners','videos') ORDER BY tablename;";

        expectedString = "[{\"schemaname\":\"public\",\"tablename\":\"chunk_owners\"}," +
                "{\"schemaname\":\"public\",\"tablename\":\"videos\"}]";
        resultString = testDB.queryTable(sampleQuery).toString();

        assertEquals(expectedString, resultString);
    }

    @Test
    void singleInsertIntoDB() throws Exception {
        String insertQuery = "INSERT INTO chunk_owners(file_hash, chunk_hash, chunk_id, owner_ip, owner_port, is_seeder) " +
                "VALUES('file-hash-1', 'chunk-hash-1', '1', 'localhost', '12345', '1');";
        testDB.singleUpdate(insertQuery);

        String selectQuery = "SELECT file_hash, chunk_hash, chunk_id, owner_ip, owner_port FROM chunk_owners " +
                "WHERE file_hash = 'file-hash-1'";
        JSONArray resultArray =  testDB.queryTable(selectQuery);
        JSONObject resultObject = resultArray.getJSONObject(0);

        String fileHash = resultObject.getString("file_hash");
        String chunkHash = resultObject.getString("chunk_hash");
        String chunkID = resultObject.getString("chunk_id");
        String ownerIP = resultObject.getString("owner_ip");
        String ownerPort = resultObject.getString("owner_port");

        resultString = "[{\"file_hash\":\"" + fileHash + "\",\"chunk_hash\":\"" + chunkHash +  "\"," +
                "\"chunk_id\":\"" + chunkID +  "\",\"owner_ip\":\"" + ownerIP + "\",\"owner_port\":\"" + ownerPort + "\"}]";

        expectedString = "[{\"file_hash\":\"file-hash-1\",\"chunk_hash\":\"chunk-hash-1\",\"chunk_id\":\"1\"," +
                "\"owner_ip\":\"localhost\",\"owner_port\":\"12345\"}]";

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
*/
}