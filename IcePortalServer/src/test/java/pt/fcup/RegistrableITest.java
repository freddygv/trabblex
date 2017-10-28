package pt.fcup;

import com.zeroc.IceInternal.Ex;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RegistrableITest {
    private RegistrableI testRequestHandler;
    private DBManager testDB;
    private com.zeroc.Ice.Current current;

    private String resultString;
    private String expectedString;
    private boolean result;

    private final String fileHash = "my-test-sha256-hash";
    private final String fileName = "cool-video";
    private final int fileSize = 100;
    private final String protocol = "tcp";
    private final int port = 12345;
    private final int videoSizeX = 400;
    private final int videoSizeY = 300;
    private final int bitrate = 512;
    private String[] chunkHashes = new String[]{"a", "b"};
    private String[] chunkIDs = new String[]{"1", "2"};
    private String ip = "localhost";
    private String isSeeder = "t";

    @BeforeEach
    void setUp() throws Exception {
        testRequestHandler = new RegistrableI();
        testDB = new DBManager();
        current = new com.zeroc.Ice.Current();

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
        result = testRequestHandler.registerSeeder(fileHash, fileName, fileSize, protocol, port,
                videoSizeX, videoSizeY, bitrate, current);

        JSONArray resultArray =  testDB.queryTable("SELECT * FROM seeders WHERE file_hash = '" + fileHash + "'");
        JSONObject resultObject = resultArray.getJSONObject(0);

        String hash = resultObject.getString("file_hash");
        String name = resultObject.getString("file_name");
        String size = resultObject.getString("file_size");
        String proto = resultObject.getString("protocol");
        String seedPort = resultObject.getString("port");
        String xSize = resultObject.getString("video_size_x");
        String ySize = resultObject.getString("video_size_y");
        String rate = resultObject.getString("bitrate");

        String unformattedString = "[{\"file_hash\":\"%s\"," +
                                    "\"file_name\":\"%s\"," +
                                    "\"file_size\":\"%s\"," +
                                    "\"protocol\":\"%s\"," +
                                    "\"port\":\"%s\"," +
                                    "\"videoSizeX\":\"%s\"," +
                                    "\"videoSizeY\":\"%s\"," +
                                    "\"bitrate\":\"%s\"}]";

        resultString = String.format(unformattedString, hash, name, size, proto, seedPort, xSize, ySize, rate);

        expectedString = "[{\"file_hash\":\"" + fileHash + "\"," +
                            "\"file_name\":\"" + fileName + "\"," +
                            "\"file_size\":\"" + fileSize + "\"," +
                            "\"protocol\":\"" + protocol + "\"," +
                            "\"port\":\"" + port + "\"," +
                            "\"videoSizeX\":\"" + videoSizeX + "\"," +
                            "\"videoSizeY\":\"" + videoSizeY + "\"," +
                            "\"bitrate\":\"" + bitrate + "\"}]";

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
        result = testRequestHandler.registerSeeder(fileHash, fileName, fileSize, protocol, port,
                videoSizeX, videoSizeY, bitrate, current);

        testRequestHandler.deregisterSeeder(fileHash, current);

        resultString = testDB.queryTable("SELECT * FROM seeders WHERE file_hash = '" + fileHash + "'").toString();
        expectedString = "[]";

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
        testDB.singleUpdate("DELETE FROM seeders WHERE file_hash = '" + fileHash + "';");
        testDB.singleUpdate("DELETE FROM chunk_owners WHERE file_hash = '" + fileHash + "';");
    }
}
