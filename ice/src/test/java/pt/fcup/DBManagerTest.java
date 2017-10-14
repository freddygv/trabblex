package pt.fcup;

import com.zeroc.IceInternal.Ex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

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

}