package pt.fcup;

import com.zeroc.IceInternal.Ex;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class RegistrableITest {
    private RegistrableI testRequestHandler;
    private DBManager testDB;
    private com.zeroc.Ice.Current current;

    private String resultString;
    private String expectedString;
    private boolean result;

    private final int port = 12345;
    private final String ip = "localhost";
    private final String fileHash = "my-test-sha256-hash";
    private final String isSeeder = "t";
    private final String[] chunkHashes = new String[]{"a", "b"};
    private final String[] chunkIDs = new String[]{"1", "2"};

    @BeforeEach
    void setUp() throws Exception {
        testRequestHandler = new RegistrableI();
        testDB = new DBManager();
        current = new com.zeroc.Ice.Current();

        String insertionQuery = "INSERT INTO videos (file_hash, file_name, file_size, protocol, port, " +
                "video_size_x, video_size_y, bitrate, seeder_is_active) " +
                "VALUES('%s', 'fake-movie', '27546', 'TCP', '30880', '320', '240', '800', 'f')";

        testRequestHandler.dbUpdate(String.format(insertionQuery, fileHash));

    }

    @Test
    void testDBUpdate() throws Exception {
        String chunkHash = "random-test-hash";
        String chunkID = "1234";

        String insertionQuery = "INSERT INTO chunk_owners(file_hash, chunk_hash, chunk_id, owner_ip, owner_port, is_seeder) " +
                "VALUES('" + fileHash + "', '" + chunkHash + "', '" + chunkID + "', '" + ip + "', '" + port + "', '" + isSeeder + "');";

        testRequestHandler.dbUpdate(insertionQuery);

        JSONArray resultArray = testDB.queryTable("SELECT * FROM chunk_owners WHERE chunk_hash = '" + chunkHash + "';");
        JSONObject resultObject = resultArray.getJSONObject(0);

        String resultHash = resultObject.getString("file_hash");
        String resultChunkHash = resultObject.getString("chunk_hash");
        String resultChunkID = resultObject.getString("chunk_id");
        String resultIP = resultObject.getString("owner_ip");
        String resultPort = resultObject.getString("owner_port");
        String resultIsSeeder = resultObject.getString("is_seeder");

        resultString = "[{\"file_hash\":\"" + resultHash + "\"," +
                        "\"chunk_hash\":\"" + resultChunkHash + "\"," +
                        "\"chunk_id\":\"" + resultChunkID + "\"," +
                        "\"owner_ip\":\"" + resultIP + "\"," +
                        "\"owner_port\":\"" + resultPort + "\"," +
                        "\"is_seeder\":\"" + resultIsSeeder + "\"}]";

        expectedString = "[{\"file_hash\":\"" + fileHash + "\"," +
                            "\"chunk_hash\":\"" + chunkHash + "\"," +
                            "\"chunk_id\":\"" + chunkID + "\"," +
                            "\"owner_ip\":\"" + ip + "\"," +
                            "\"owner_port\":\"" + port + "\"," +
                            "\"is_seeder\":\"" + isSeeder + "\"}]";

        assertEquals(expectedString, resultString);
    }

    @Test
    public void registeredAfterRequest() throws Exception {

        result = testRequestHandler.registerSeeder(fileHash, current);

        String videosQuery = "SELECT file_hash, seeder_is_active FROM videos WHERE file_hash = '%s'";

        JSONArray resultArray = testDB.queryTable(String.format(videosQuery, fileHash));

        System.out.println(resultArray);

        JSONObject resultObject = resultArray.getJSONObject(0);

        String resultHash = resultObject.getString("file_hash");
        String resultIsActive = resultObject.getString("seeder_is_active");

        String unformattedString = "[{\"file_hash\":\"%s\"," +
                                    "\"seeder_is_active\":\"%s\"}]";

        resultString = String.format(unformattedString, resultHash, resultIsActive);

        expectedString = "[{\"file_hash\":\"" + fileHash + "\"," +
                            "\"seeder_is_active\":\"t\"}]";

        assertEquals(expectedString, resultString);
        assertTrue(result);

    }

    @Test
    public void neighborhoodUpdated() throws Exception {
        result = testRequestHandler.sendHashes(chunkHashes, chunkIDs, fileHash, ip, port, current);

        JSONArray resultArray =  testDB.queryTable("SELECT * " +
                                                   "FROM chunk_owners WHERE file_hash = '" + fileHash + "' " +
                                                   "ORDER BY chunk_hash");

        JSONObject resultObjectA = resultArray.getJSONObject(0);
        JSONObject resultObjectB = resultArray.getJSONObject(1);

        String resultHash = resultObjectA.getString("file_hash");
        String resultIP = resultObjectA.getString("owner_ip");
        String resultPort = resultObjectA.getString("owner_port");
        String resultIsSeeder = resultObjectA.getString("is_seeder");

        String chunkHashA = resultObjectA.getString("chunk_hash");
        String chunkIDA = resultObjectA.getString("chunk_id");

        String chunkHashB = resultObjectB.getString("chunk_hash");
        String chunkIDB = resultObjectB.getString("chunk_id");


        resultString = "[{\"file_hash\":\"" + resultHash + "\"," +
                        "\"chunk_hash\":\"" + chunkHashA + "\"," +
                        "\"chunk_id\":\"" + chunkIDA + "\"," +
                        "\"owner_ip\":\"" + resultIP + "\"," +
                        "\"owner_port\":\"" + resultPort + "\"," +
                        "\"is_seeder\":\"" + resultIsSeeder + "\"}," +

                        "{\"file_hash\":\"" + resultHash + "\"," +
                        "\"chunk_hash\":\"" + chunkHashB + "\"," +
                        "\"chunk_id\":\"" + chunkIDB + "\"," +
                        "\"owner_ip\":\"" + resultIP + "\"," +
                        "\"owner_port\":\"" + resultPort + "\"," +
                        "\"is_seeder\":\"" + resultIsSeeder + "\"}]";

        expectedString = "[{\"file_hash\":\"" + fileHash + "\"," +
                            "\"chunk_hash\":\"" + chunkHashes[0] + "\"," +
                            "\"chunk_id\":\"" + chunkIDs[0] + "\"," +
                            "\"owner_ip\":\"" + ip + "\"," +
                            "\"owner_port\":\"" + port + "\"," +
                            "\"is_seeder\":\"" + isSeeder + "\"}," +

                           "{\"file_hash\":\"" + fileHash + "\"," +
                            "\"chunk_hash\":\"" + chunkHashes[1] + "\"," +
                            "\"chunk_id\":\"" + chunkIDs[1] + "\"," +
                            "\"owner_ip\":\"" + ip + "\"," +
                            "\"owner_port\":\"" + port + "\"," +
                            "\"is_seeder\":\"" + isSeeder + "\"}]";

        assertEquals(expectedString, resultString);
        assertTrue(result);

    }

    @Test
    public void deregisteredSeederAfterRequest() throws Exception {
        result = testRequestHandler.registerSeeder(fileHash, current);

        testRequestHandler.deregisterSeeder(fileHash, current);

        String resultQuery = "SELECT seeder_is_active FROM videos WHERE file_hash = '%s';";
        resultString = testDB.queryTable(String.format(resultQuery, fileHash)).toString();

        expectedString = "[{\"seeder_is_active\":\"f\"}]";

        assertEquals(expectedString, resultString);
        assertTrue(result);

    }

    @Test
    public void deregisteredChunksAfterRequest() throws Exception {
        result = testRequestHandler.sendHashes(chunkHashes, chunkIDs, fileHash, ip, port, current);

        testRequestHandler.deregisterSeeder(fileHash, current);

        resultString = testDB.queryTable("SELECT * FROM chunk_owners WHERE file_hash = '" + fileHash + "'").toString();
        expectedString = "[]";

        assertEquals(expectedString, resultString);
        assertTrue(result);

    }

    @AfterEach
    void tearDown() throws Exception {
        testDB.singleUpdate("DELETE FROM videos WHERE file_hash = '" + fileHash + "';");
        testDB.singleUpdate("DELETE FROM chunk_owners WHERE file_hash = '" + fileHash + "';");
    }
}
