package ca.ubc.cs.commandrecommender.db;

import ca.ubc.cs.commandrecommender.RecommenderOptions;
import ca.ubc.cs.commandrecommender.Exception.DBConnectionException;
import ca.ubc.cs.commandrecommender.model.IndexMap;
import ca.ubc.cs.commandrecommender.model.Rationale;
import ca.ubc.cs.commandrecommender.model.User;

import com.google.common.primitives.Ints;
import com.mongodb.*;

import org.bson.types.ObjectId;

import java.net.UnknownHostException;
import java.util.*;

/**
 * Adapter for the recommendation, user, and command details collections in mongoDB
 *
 * Created by Spencer on 6/23/2014.
 */
public class MongoRecommendationDB extends AbstractRecommendationDB{
    public static final String USER_ID_FIELD = "user_id";
    public static final String COMMAND_DETAIL_ID_FIELD = "command_detail_id";
    public static final String COMMAND_DETAIL_OBJECT_ID_FIELD = "_id";
    public static final String COMMAND_ID_FIELD = "command_id";
    public static final String LAST_UPLOADED_DATE_FIELD = "last_upload_date";
    public static final String LAST_RECOMMENDATION_DATE_FIELD = "last_recommendation_date";
    public static final String LAST_RECOMMENDATION_ALGORITHM_FIELD = "last_recommendation_algorithm";
    public static final String CREATED_ON = "created_on";
    public static final String REASON_FIELD = "reason";
    public static final String ALGORITHM_TYPE_FIELD = "algorithm_type";
    public static final String ALGORITHM_VALUE_FIELD = "algorithm_value";
    public static final String REASON_VALUE_FIELD = "reason_value";
    public static final String RANK_FIELD = "rank";
    
    private MongoClient recommendationClient;
    private DBCollection commandDetailsCollection;
    private ConnectionParameters connectionParameters;

    protected DBCollection userCollection;
    protected DBCollection recommendationCollection;
    protected Map<String, ObjectId> commandDetailsMap;

    public MongoRecommendationDB(RecommenderOptions options, IndexMap userIndexMap)
            throws DBConnectionException{
        super(userIndexMap);
        try {
        	this.connectionParameters = options.getRecommendationConnectionParamters();
        	ServerAddress serverAddress = new ServerAddress(connectionParameters.getDbUrl(), connectionParameters.getDbPort());
        	if(!connectionParameters.getDbUser().equals("")){
        		List<MongoCredential> credentialList = createCredentialList(connectionParameters);        		
        		recommendationClient = new MongoClient(serverAddress, credentialList);
        	}else{
        		recommendationClient = new MongoClient(serverAddress);
        	}           
            userCollection = getCollection(options.getUserTable());
            recommendationCollection = getCollection(options.getRecommendationTable());
            commandDetailsCollection = getCollection(options.getCommandDetailTable());
            initCommandDetailsMap();
            ensureIndex();
        }catch(Exception ex){
            throw new DBConnectionException(ex);
        }
    }
    
    private List<MongoCredential> createCredentialList(
			ConnectionParameters connectionParameters) {
		MongoCredential userCredential = MongoCredential.createMongoCRCredential(connectionParameters.getDbUser(), connectionParameters.getdBName(), connectionParameters.getDbPassword().toCharArray());
		return Collections.singletonList(userCredential);
	}

    private void ensureIndex() {
        if(recommendationCollection != null) {
        	DBObject compoundIndex = new BasicDBObject();
        	compoundIndex.put(USER_ID_FIELD, 1);
        	compoundIndex.put(ALGORITHM_TYPE_FIELD, 1);
        	compoundIndex.put(COMMAND_ID_FIELD, 1);
            recommendationCollection.createIndex(compoundIndex);
        }
    }

    private void initCommandDetailsMap() {
        commandDetailsMap = new HashMap<String, ObjectId>();
        DBCursor details = commandDetailsCollection.find();
        for (DBObject detail : details) {
            String commandId = (String) detail.get(COMMAND_ID_FIELD);
            ObjectId objectId = (ObjectId) detail.get(COMMAND_DETAIL_OBJECT_ID_FIELD);
            commandDetailsMap.put(commandId, objectId);
        }
    }

    protected DBCollection getCollection(String collection) {
        return recommendationClient.getDB(connectionParameters.getdBName()).getCollection(collection);
    }
	
	public void closeConnection() {
		recommendationClient.close();
	}
    
    @Override
    public int getNumberOfKnownCommands() {
    	return Ints.saturatedCast(commandDetailsCollection.count());
    }

    @Override
    public void saveRecommendation(String commandId,
                                   String userId,
                                   String reason,
                                   String algorithmType,
                                   Rationale rationale) {
        if(commandId == null || commandId.equals("") || userId == null || userId.equals(""))
            return;
        DBObject queryForOldRecommendation = new BasicDBObject(USER_ID_FIELD, userId)
        .append(ALGORITHM_TYPE_FIELD, algorithmType)
        .append(COMMAND_ID_FIELD, commandId);
        DBObject recommendation = recommendationCollection.findOne(queryForOldRecommendation);
        
        if (recommendation == null) {
            ObjectId commandDetail = commandDetailsMap.get(commandId);
            // If the command detail is not know, we would not make the recommendation for the user
            // This situation should not occur for the production version as all the tools we know of must be in the 
            // command detail table
	    //            if(commandDetail == null)
	    //            	return;
            recommendation =  new BasicDBObject(USER_ID_FIELD, userId)
            .append(ALGORITHM_TYPE_FIELD, algorithmType)
		.append(COMMAND_ID_FIELD, new ObjectId(commandId))
            .append(COMMAND_DETAIL_ID_FIELD, commandDetail);
        }
        
        recommendation.put(RANK_FIELD, rationale.getRank());
        recommendation.put(ALGORITHM_VALUE_FIELD, rationale.getDecisionPointValue());
        recommendation.put(CREATED_ON, new Date(System.currentTimeMillis())); //TODO: should we keep this?
        recommendation.put(REASON_VALUE_FIELD, rationale.getValueForTypeSpecificReason()); //TODO: avoid duplication
        recommendation.put(REASON_FIELD, reason); //TODO: avoid duplication
        recommendationCollection.save(recommendation);
    }

    @Override
    public List<User> getAllUsers() {
        DBCursor userCursor = userCollection.find();
        List<User> users = new ArrayList<User>();
        while(userCursor.hasNext()){
            DBObject userDbObject = userCursor.next();
            String userId = (String) userDbObject.get(USER_ID_FIELD);
            Date lastUpdate = (Date) userDbObject.get(LAST_UPLOADED_DATE_FIELD);
            Date lastRecommendationDate = (Date) userDbObject.get(LAST_RECOMMENDATION_DATE_FIELD);
            users.add(new User(userId, lastUpdate, lastRecommendationDate, this));
        }
        return users;
    }

    @Override
    public void updateRecommendationStatus(String userId, String algoType) {
        DBObject query = new BasicDBObject(USER_ID_FIELD, userId);
        DBObject updatedValues = new BasicDBObject(LAST_RECOMMENDATION_DATE_FIELD, new Date())
        .append(LAST_RECOMMENDATION_ALGORITHM_FIELD, algoType);
        userCollection.update(query, new BasicDBObject("$set", updatedValues), true, false);
    }

	@Override
	public void clearInfoAndRankings(String userId, String algoType) {
        DBObject query = new BasicDBObject(USER_ID_FIELD, userId).append(ALGORITHM_TYPE_FIELD, algoType);
        DBObject fieldsToClear = new BasicDBObject(ALGORITHM_VALUE_FIELD, null)
        .append(RANK_FIELD, null)
        .append(REASON_VALUE_FIELD, null);
		recommendationCollection.update(query, new BasicDBObject("$unset", fieldsToClear), false, true);
	}
    
}
