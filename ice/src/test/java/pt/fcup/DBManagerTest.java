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
        String sampleQuery = "SELECT pg.tablename FROM pg_catalog.pg_tables pg WHERE pg.tablename = 'seeders';";

        expectedString = "[{\"tablename\":\"seeders\"}]";
        resultString = testDB.queryTable(sampleQuery).toString();

        assertEquals(expectedString, resultString);
    }

}