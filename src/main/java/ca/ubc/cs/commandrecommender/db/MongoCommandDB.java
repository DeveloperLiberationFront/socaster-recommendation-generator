package ca.ubc.cs.commandrecommender.db;

import ca.ubc.cs.commandrecommender.RecommenderOptions;
import ca.ubc.cs.commandrecommender.Exception.DBConnectionException;
import ca.ubc.cs.commandrecommender.model.IndexMap;
import ca.ubc.cs.commandrecommender.model.ToolUse;
import ca.ubc.cs.commandrecommender.model.ToolUseCollection;

import com.mongodb.*;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Spencer on 6/20/2014.
 */
public class MongoCommandDB extends AbstractCommandDB {
    private MongoClient client;
    private boolean useCache;
    private HashMap<Integer, ToolUseCollection> userToolUsesMap;
    private DBCollection commandCollection;

    public static final String KIND = "kind";
    public static final String COMMAND = "command";

    public MongoCommandDB(RecommenderOptions options, AbstractCommandToolConverter toolConverter, 
    		IndexMap userIndexMap, boolean useCache) throws DBConnectionException{
        super(toolConverter, userIndexMap);
        this.useCache = useCache;
        try {
        	ConnectionParameters connectionParameters = options.getCommandConnectionParameters();
        	ServerAddress serverAddress = new ServerAddress(connectionParameters.getDbUrl(), connectionParameters.getDbPort());
        	if(!connectionParameters.getDbUser().equals("")){
        		List<MongoCredential> credentialList = createCredentialList(connectionParameters);        		
        		client = new MongoClient(serverAddress, credentialList);
        	}else{
        		client = new MongoClient(serverAddress);
        	}
            commandCollection = client.getDB(connectionParameters.getdBName()).getCollection(options.getCommandTable());
            ensureIndex();
        }catch(Exception ex){
            throw new DBConnectionException(ex);
        }/*catch(CommandFailureException ex){
        	throw new DBConnectionException(ex);
        }*/
    }
    
    private List<MongoCredential> createCredentialList(
			ConnectionParameters connectionParameters) {
		MongoCredential userCredential = MongoCredential.createMongoCRCredential(connectionParameters.getDbUser(), connectionParameters.getdBName(), connectionParameters.getDbPassword().toCharArray());
		return Collections.singletonList(userCredential);
	}

  

	private void ensureIndex() {
        if(commandCollection != null) {
            //Note that the order of the compound index is really important
            commandCollection.createIndex(new BasicDBObject(KIND, 1).append(toolConverter.getUserIdField(), 1));
        }
    }

    @Override
    public List<ToolUseCollection> getAllUsageData() {
        HashMap<Integer, ToolUseCollection> toolUses = new HashMap<Integer, ToolUseCollection>();

        //We might want to adjust this later to recommend bundles
        DBObject query = new BasicDBObject(KIND, COMMAND);
        DBObject fieldsToReturn = new BasicDBObject();
        fieldsToReturn.put(toolConverter.getHotkeyField(), 1);
        fieldsToReturn.put(toolConverter.getTimeField(), 1);
        fieldsToReturn.put(toolConverter.getCommandIdField(), 1);
        fieldsToReturn.put(toolConverter.getUserIdField(), 1);
        DBCursor cursor = commandCollection.find(query);

        while(cursor.hasNext()){
            DBObject row = cursor.next();
            String userIdString = (String) row.get(toolConverter.getUserIdField());
            Integer userId = userIndexMap.getItemByItemId(userIdString);
            ToolUse toolUse = toolConverter.convertToToolUse(row.toMap());
            if(!toolUses.containsKey(userId)){
                ToolUseCollection newUserToolUse = new ToolUseCollection(userId);
                newUserToolUse.add(toolUse);
                toolUses.put(userId, newUserToolUse);
            }else{
                toolUses.get(userId).add(toolUse);
            }
        }

        if (useCache)
            this.userToolUsesMap = toolUses;
        return new ArrayList<ToolUseCollection>(toolUses.values());
    }

    @Override
    public ToolUseCollection getUsersUsageData(String userId) {
        Integer userIdIndex = userIndexMap.getItemByItemId(userId);
        if (userToolUsesMap != null) { // cache exists
            ToolUseCollection toolUses = userToolUsesMap.get(userIdIndex);
            return (toolUses == null) ? new ToolUseCollection(userIdIndex) : toolUses;
        }
        DBObject query = new BasicDBObject(toolConverter.getUserIdField(), userId)
                .append(KIND, COMMAND);
        DBObject fieldsToReturn = new BasicDBObject();
        fieldsToReturn.put(toolConverter.getCommandIdField(), 1);
        fieldsToReturn.put(toolConverter.getHotkeyField(), 1);
        fieldsToReturn.put(toolConverter.getTimeField(), 1);
        DBCursor userCommandCursor = commandCollection.find(query);
        ToolUseCollection toolUses = new ToolUseCollection(userIdIndex);
        while(userCommandCursor.hasNext()){
            ToolUse toolUse = toolConverter.convertToToolUse(userCommandCursor.next().toMap());
            toolUses.add(toolUse);
        }
        return toolUses;
    }

}
