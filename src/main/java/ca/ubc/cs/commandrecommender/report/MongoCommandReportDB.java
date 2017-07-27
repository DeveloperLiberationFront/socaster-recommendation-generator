package ca.ubc.cs.commandrecommender.report;

import ca.ubc.cs.commandrecommender.RecommenderOptions;
import ca.ubc.cs.commandrecommender.Exception.DBConnectionException;
import ca.ubc.cs.commandrecommender.db.ConnectionParameters;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import com.mongodb.*;

import org.bson.types.ObjectId;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MongoCommandReportDB {

    public static final String DESCRIPTION_FIELD = "description";
    public static final String COMMAND_ID_FIELD = "command_id";
    public static final String ID_FIELD = "_id";
    public static final String BINDING_USED_FIELD = "bindingUsed";
    public static final String USER_ID_FIELD = "user_id";
    public static final String TIME_FIELD = "time";
    public static final String USE_COUNT_FIELD = "useCount";
    public static final String HOTKEY_COUNT_FIELD = "hotkeyCount";
    public static final String KIND_FIELD = "kind";
    public static final String COMMAND_FIELD = "command";
    public static final String NEW_FIELD = "new";
    public static final String CURRENT_FIELD = "current";
    public static final String COMMAND_DETAIL_ID_FIELD = "command_detail_id";
    
    private DBCollection commandCollection;
    private MongoClient client;

    public MongoCommandReportDB(RecommenderOptions recommenderOptions) 
    		throws DBConnectionException {
        try {
        	ConnectionParameters connectionParameters = recommenderOptions.getCommandConnectionParameters();
            client = new MongoClient(connectionParameters.getDbUrl(), connectionParameters.getDbPort());
            commandCollection = client.getDB(connectionParameters.getdBName()).getCollection(recommenderOptions.getCommandTable());
        }catch(Exception ex){
            throw new DBConnectionException(ex);
        }
    }

    public void closeConnection() {
        client.close();
    }
    
    /**
     * 
     * @param startTime the report will be generated based on usage data collected from the startTime to now
     * @param userIds  the users to generate reports for
     * @param cmdLimit maximum number of commands to output for a user. If negative, no limit is set
     * @return
     */
    public List<DBObject> getUsageReports(long startTime,
                                          List<String> userIds,
                                          int cmdLimit,
                                          Map<String, ObjectId> commandDetailsMap) {
        AggregationOptions options = AggregationOptions.builder()
                .allowDiskUse(true)
                .outputMode(AggregationOptions.OutputMode.CURSOR)
                .build();
        Cursor rawStats = commandCollection.aggregate(getReportPipeline(startTime, userIds), options);
        PeekingIterator<DBObject> cursor = Iterators.peekingIterator(rawStats);
        List<DBObject> reports = new ArrayList<DBObject>();
        while (cursor.hasNext()) {
            // retrieve the report for a user
            String userId = (String) ((DBObject) cursor.peek().get(ID_FIELD)).get(USER_ID_FIELD);
            int totalCommandUsed = 0;
            int totalInvocation = 0;
            BasicDBList cmdStats = new BasicDBList();
            BasicDBList newCmds = new BasicDBList();
            while (cursor.hasNext() &&
                    userId.equals(((DBObject) cursor.peek().get(ID_FIELD)).get(USER_ID_FIELD))) {
                // retrieve the stats for a command for the user
                DBObject stat = cursor.next();
                int useCount = (Integer) stat.get(USE_COUNT_FIELD);
                if (useCount == 0)
                    continue;
                if (cmdLimit < 0 || totalCommandUsed < cmdLimit) {
                    int hotkeyCount = (Integer) stat.get(HOTKEY_COUNT_FIELD);
                    String cmdId = (String) ((DBObject) stat.get(ID_FIELD)).get(COMMAND_ID_FIELD);
                    ObjectId cmdDetailObjectId = commandDetailsMap.get(cmdId);
                    cmdStats.add(CommandStats.create(cmdDetailObjectId, useCount, hotkeyCount));
                    if ((Boolean) stat.get(NEW_FIELD))
                        newCmds.add(new BasicDBObject(COMMAND_DETAIL_ID_FIELD, cmdDetailObjectId));
                }
                totalInvocation += useCount;
                totalCommandUsed++;
            }
            reports.add(UsageReport.create(userId, cmdStats, totalInvocation, totalCommandUsed, newCmds));
        }
        return reports;
    }

    private List<DBObject> getReportPipeline(long startTime, List<String> userIds) {
        DBObject match = new BasicDBObject("$match",
                new BasicDBObject(KIND_FIELD, COMMAND_FIELD)
                        .append(USER_ID_FIELD, new BasicDBObject("$in", userIds)));
        DBObject afterStartTime = new BasicDBObject("$gt", new Object[]{"$" + TIME_FIELD, startTime});
        DBObject project = new BasicDBObject("$project",
                new BasicDBObject(DESCRIPTION_FIELD, 1)
                        .append(USER_ID_FIELD, 1)
                        .append(BINDING_USED_FIELD, 1)
                        .append(CURRENT_FIELD, afterStartTime));
        DBObject fieldsToGroupBy = new BasicDBObject(USER_ID_FIELD, "$" + USER_ID_FIELD)
                .append(COMMAND_ID_FIELD, "$" + DESCRIPTION_FIELD);
        DBObject useCountCond = new BasicDBObject("$cond", new Object[]{"$"+CURRENT_FIELD, 1, 0});
        DBObject hotkeyCountCond = new BasicDBObject("$cond",
                new Object[]{new BasicDBObject("$and",
                        new Object[]{"$" + CURRENT_FIELD, "$" + BINDING_USED_FIELD}), 1, 0});
        DBObject groupFields = new BasicDBObject(ID_FIELD, fieldsToGroupBy)
                .append(USE_COUNT_FIELD, new BasicDBObject("$sum", useCountCond))
                .append(HOTKEY_COUNT_FIELD, new BasicDBObject("$sum", hotkeyCountCond))
                .append(NEW_FIELD, new BasicDBObject("$min", "$" + CURRENT_FIELD));
        DBObject group = new BasicDBObject("$group", groupFields);
        DBObject sort = new BasicDBObject("$sort",
                new BasicDBObject(ID_FIELD + "." + USER_ID_FIELD, 1)
                        .append(USE_COUNT_FIELD, -1));
        return Arrays.asList(match, project, group, sort);
    }

    /* the query used above
    db.commands.aggregate(
    [
    {
        $match:
        {
            kind:"command",
            user_id: {
                $in: <userIds>
            }
        }
    },
    {
        $project:
        {
            description:1,
            user_id:1,
            bindingUsed:1,
            current:
            {
                $gt: ["$time", <startTime>]
            }
        }
    },
    {
        $group:
        {
            _id:
            {
                user_id:"$user_id",
                command_id:"$description"
            },
            useCount:
            {
                $sum:
                {
                    $cond: ["$current", 1, 0]
                }
            },
            hotkeyCount:
            {
                $sum:
                {
                    $cond: [{$and: ["$bindingUsed", "$current"]}, 1, 0]
                }
            },
            new:
            {
                $min: "$current"
            }
        }
    },
    {
        $sort:
        {
            "_id.user_id": 1,
            useCount: -1
        }
    }
    ],
    {
        allowDiskUse:true
    }
    )
    */

}
