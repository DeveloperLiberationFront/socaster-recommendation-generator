package ca.ubc.cs.commandrecommender.db;

import ca.ubc.cs.commandrecommender.Exception.DBConnectionException;
import ca.ubc.cs.commandrecommender.model.IndexMap;
import ca.ubc.cs.commandrecommender.model.ToolUse;
import ca.ubc.cs.commandrecommender.model.ToolUseCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * Created by Spencer on 6/24/2014.
 */
public class MongoCommandDBTest {
    private List<ToolUseCollection> savedToolUseCollections;
    private MongoClient client;
    private String DB_URL = "localhost";
    private int DB_PORT = 27017;
    private DBCollection commandCollection;
    private String DB_NAME = "commands-test";
    private IndexMap toolIndexMap, userIndexMap;
    private String USER_ID = "user_id";
    private String DESCRIPTION = "description";
    private MongoCommandDB commandDB;
    private EclipseCommandToolConverter toolConverter;

    @Before
    public void setUp() throws UnknownHostException, DBConnectionException {

        client = new MongoClient(DB_URL, DB_PORT);
        commandCollection = client.getDB(DB_NAME).getCollection(MongoCommandDB.COMMANDS_COLLECTION);
        toolIndexMap = new IndexMap();
        userIndexMap = new IndexMap();
        toolConverter = new EclipseCommandToolConverter(toolIndexMap);

        ConnectionParameters connectionParameters = new ConnectionParameters(DB_URL, DB_PORT, DB_NAME, "", "");
        commandDB = new MongoCommandDB(connectionParameters,toolConverter, userIndexMap);
        savedToolUseCollections = generateToolUses();
    }

    @After
    public void tearDown(){
        commandCollection.remove(new BasicDBObject());
    }

    private List<ToolUseCollection> generateToolUses() {
        List<ToolUseCollection> toolUseCollections = new ArrayList<ToolUseCollection>();
        Integer userIndex1 = userIndexMap.addItem(USER_ID + 1);
        Integer userIndex2 = userIndexMap.addItem(USER_ID + 2);
        ToolUseCollection toolUseCollectionUser1 = new ToolUseCollection(userIndex1);
        ToolUseCollection toolUseCollectionUser2 = new ToolUseCollection(userIndex2);
        for(int i = 0; i < 10; i++){
            String description = DESCRIPTION + i;
            String userId = USER_ID + (i%2);
            Long time = System.currentTimeMillis();
            BasicDBObject command = new BasicDBObject()
                    .append(EclipseCommandToolConverter.DESCRIPTION, description)
                    .append(EclipseCommandToolConverter.TIME, time)
                    .append(EclipseCommandToolConverter.BINDING_USED, true)
                    .append(MongoCommandDB.KIND, MongoCommandDB.COMMAND)
                    .append(EclipseCommandToolConverter.USER_ID, userId);
            Integer toolIndex = toolIndexMap.addItem(description);

            commandCollection.insert(command);
            if(i%2 != 0)
                toolUseCollectionUser1.add(new ToolUse(new Timestamp(time), toolIndex, true));
            else
                toolUseCollectionUser2.add(new ToolUse(new Timestamp(time), toolIndex, true));

        }
        toolUseCollections.add(toolUseCollectionUser1);
        toolUseCollections.add(toolUseCollectionUser2);
        return toolUseCollections;
    }

    @Test
    public void testGetAllUsageData(){
        List<ToolUseCollection> retrievedToolUseCollections = commandDB.getAllUsageData();
        assertEquals(retrievedToolUseCollections.size(), savedToolUseCollections.size());
        for(ToolUseCollection retrievedToolUses : retrievedToolUseCollections){
            for(ToolUseCollection savedToolUses : savedToolUseCollections){
                if(savedToolUses.userId == retrievedToolUses.userId){
                    assertToolUseCollectionsAreSame(savedToolUses, retrievedToolUses);
                }
            }

        }
    }

    private void assertToolUseCollectionsAreSame(ToolUseCollection savedToolUses, ToolUseCollection retrievedToolUses) {
        for(ToolUse savedToolUse : savedToolUses){
            assertTrue(retrievedToolUses.contains(savedToolUse));
        }
    }

    @Test
    public void testGetUserUsageData(){
        for(int i = 0; i < 2; i ++){
            ToolUseCollection retrievedToolUses = commandDB.getUsersUsageData(USER_ID + i);
            for(ToolUseCollection savedToolUses : savedToolUseCollections){
                if(savedToolUses.userId == retrievedToolUses.userId){
                    assertToolUseCollectionsAreSame(savedToolUses, retrievedToolUses);
                    break;
                }
            }
        }
    }
}
