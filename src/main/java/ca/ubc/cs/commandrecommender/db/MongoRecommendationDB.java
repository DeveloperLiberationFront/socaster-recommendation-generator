package ca.ubc.cs.commandrecommender.db;

import ca.ubc.cs.commandrecommender.Exception.DBConnectionException;
import ca.ubc.cs.commandrecommender.model.IndexMap;
import ca.ubc.cs.commandrecommender.model.ToolUseCollection;
import ca.ubc.cs.commandrecommender.model.User;
import com.mongodb.*;

import java.net.UnknownHostException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for the recommendation, user, and command details collections in mongoDB
 *
 * Created by Spencer on 6/23/2014.
 */
public class MongoRecommendationDB extends AbstractRecommendationDB{
    public static final String USER_ID_FIELD = "user_id";
    public static final String COMMAND_DETAIL_ID_FIELD = "command_detail_id";
    public static final String COMMAND_DETAIL_OBJECT_ID_FIELD = "_id";
    public static final String NEW_RECOMMENDATION_FIELD = "new_recommendation";
    public static final String USER_COLLECTION = "users";
    public static final String USER_RECOMMENDATION_COLLECTION = "recommendations";
    public static final String COMMAND_DETAILS_COLLECTION = "command_details";
    public static final String COMMAND_ID_FIELD = "command_id";
    public static final String LAST_UPLOADED_DATE_FIELD = "last_upload_date";
    public static final String CREATED_ON = "created_on";
    public static final String REASON_FIELD = "reason";

    private MongoClient recommendationClient;
    private DBCollection userCollection;
    private DBCollection recommendationCollection;
    private DBCollection commandDetailsCollection;
    private AbstractCommandToolConverter toolConverter;


    public MongoRecommendationDB(ConnectionParameters recommendationConnectionParameters,
                                 AbstractCommandToolConverter toolConverter, IndexMap userIndexMap) throws DBConnectionException{
        super(userIndexMap);
        this.toolConverter = toolConverter;
        try {
            recommendationClient = new MongoClient(recommendationConnectionParameters.getDbUrl(), recommendationConnectionParameters.getDbPort());
            this.userCollection = getCollection(recommendationConnectionParameters, USER_COLLECTION);
            this.recommendationCollection = getCollection(recommendationConnectionParameters, USER_RECOMMENDATION_COLLECTION);
            this.commandDetailsCollection = getCollection(recommendationConnectionParameters, COMMAND_DETAILS_COLLECTION);
            ensureIndex();
        }catch(UnknownHostException ex){
            throw new DBConnectionException(ex);
        }
    }

    private void ensureIndex() {
        if(recommendationCollection != null) {
            recommendationCollection.createIndex(new BasicDBObject(USER_ID_FIELD, 1));
        }
    }

    private DBCollection getCollection(ConnectionParameters connectionParameters, String collection) {
        return recommendationClient.getDB(connectionParameters.getdBName()).getCollection(collection);
    }

    @Override
    public void saveRecommendation(String commandId, String userId, String reason) {
        if(commandId == null || commandId.equals("") || userId == null || userId.equals(""))
            return;
        DBObject query = new BasicDBObject(COMMAND_ID_FIELD, commandId);
        DBObject commandDetail = commandDetailsCollection.findOne(query);
        // If the command detail is not know, we would not make the recommendation for the user
        if(commandDetail == null)
            return;
        BasicDBObject recommendationToSave = new BasicDBObject(USER_ID_FIELD, userId)
                .append(COMMAND_DETAIL_ID_FIELD, commandDetail.get(COMMAND_DETAIL_OBJECT_ID_FIELD))
                .append(NEW_RECOMMENDATION_FIELD, true)
                .append(REASON_FIELD, reason)
                .append(CREATED_ON, new Date(System.currentTimeMillis()));
        recommendationCollection.insert(recommendationToSave);
    }

    @Override
    public void markRecommendationsAsOld(String userId) {
        BasicDBObject query = new BasicDBObject(USER_ID_FIELD, userId);
        BasicDBObject update = new BasicDBObject().append("$set", new BasicDBObject(NEW_RECOMMENDATION_FIELD, false));
        recommendationCollection.update(query, update, false, true);
    }

    @Override
    public List<User> getAllUsers() {
        DBCursor userCursor = userCollection.find();
        List<User> users = new ArrayList<User>();
        while(userCursor.hasNext()){
            DBObject userDbObject = userCursor.next();
            String userId = (String) userDbObject.get(USER_ID_FIELD);
            java.util.Date lastUpdate = (java.util.Date) userDbObject.get(LAST_UPLOADED_DATE_FIELD);
            ToolUseCollection userPastRecommendations = getToolUses(userId);
            users.add(new User(userId, new Date(lastUpdate.getTime()), userPastRecommendations, this));
        }
        return users;
    }

    private ToolUseCollection getToolUses(String userId) {
        DBObject query = new BasicDBObject(USER_ID_FIELD, userId);
        DBCursor cursor = recommendationCollection.find(query);
        ToolUseCollection toolUses = new ToolUseCollection(userIndexMap.getItemByItemId(userId));
        while(cursor.hasNext()){
            DBObject recommendation = cursor.next();
            query = new BasicDBObject(COMMAND_DETAIL_OBJECT_ID_FIELD, recommendation.get(COMMAND_DETAIL_ID_FIELD));
            DBObject toolUse = commandDetailsCollection.findOne(query);
            toolUses.add(toolConverter.convertRecommendationToToolUse(toolUse.toMap()));
        }
        return toolUses;
    }
}
