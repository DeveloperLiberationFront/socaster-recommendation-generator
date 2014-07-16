package ca.ubc.cs.commandrecommender;


import ca.ubc.cs.commandrecommender.Exception.DBConnectionException;
import ca.ubc.cs.commandrecommender.db.*;
import ca.ubc.cs.commandrecommender.generator.AlgorithmType;
import ca.ubc.cs.commandrecommender.generator.IRecGen;
import ca.ubc.cs.commandrecommender.model.IndexMap;
import ca.ubc.cs.commandrecommender.model.RecommendationCollector;
import ca.ubc.cs.commandrecommender.model.ToolUseCollection;
import ca.ubc.cs.commandrecommender.model.User;
import ca.ubc.cs.commandrecommender.model.acceptance.AbstractLearningAcceptance;
import ca.ubc.cs.commandrecommender.model.acceptance.LearningAcceptanceType;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.UnknownHostException;
import java.util.List;

/**
 * Main class for running the command line recommendation generator
 */
public class App {
    public static final String COMMAND_HOST = "ch";
    public static final String COMMAND_PORT = "cp";
    public static final String COMMAND_DB_NAME = "cn";
    public static final String RECOMMENDATION_HOST = "rh";
    public static final String RECOMMENDATION_PORT = "rp";
    public static final String RECOMMENDATION_DB_NAME = "rn";
    public static final String COMMAND_USER = "cu";
    public static final String COMMAND_PASS = "cpass";
    public static final String RECOMMENDATION_USER = "ru";
    public static final String RECOMMENDATION_PASS = "rpass";
    public static final String AMOUNT = "a";
    public static final String ALGORITHM_TYPE = "t";
    public static final String ACCEPTANCE_TYPE = "c";
    public static final String USE_CACHE = "u";
    
    private static AbstractCommandToolConverter toolConverter;
    private static ConnectionParameters commandConnectionParameters;
    private static ConnectionParameters recommendationConnectionParameters;
    private static AbstractCommandDB commandDB;
    private static AbstractRecommendationDB recommendationDB;
    private static IndexMap userIndexMap;
    private static IndexMap toolIndexMap;
    private static String dbName = "commands-production";
    private static String dbUrl = "localhost";
    private static int port = 27017;
    private static int amount = 10;
    private static String algorithmName = "MOST_WIDELY_USED";
    private static AlgorithmType algorithmType;
    private static AbstractLearningAcceptance acceptance;
    private static Logger logger = LogManager.getLogger(App.class);
    private static boolean useCache = false;

    public static void main(String[] args) throws UnknownHostException, DBConnectionException {
        //establish connection
        //TODO: use authorization for production
        //TODO: close connections properly
        //TODO: make more robust
        try {
            parseCommandLineArguments(args);
        } catch (ParseException e) {
           System.out.println("Invalid argument: " + e.getMessage());
           System.exit(1);
        }
        initializeDatabases();
        IRecGen recGen = algorithmType.getRecGen(acceptance, recommendationDB.getNumberOfKnownCommands());
        long time = System.currentTimeMillis();
        List<ToolUseCollection> toolUses =  commandDB.getAllUsageData();
        logger.debug("Time to Retrieve Data From Database: {}", getAmountOfTimeTaken(time));
        time = System.currentTimeMillis();
        for (ToolUseCollection uses : toolUses)
            recGen.trainWith(uses);
        logger.debug("Trained with {}, in {}", toolUses.size(), getAmountOfTimeTaken(time));
        time = System.currentTimeMillis();
        recGen.runAlgorithm();
        logger.debug("Ran Algorithm {} in {}", algorithmType.getRationale(), getAmountOfTimeTaken(time));
        int totalUserRecommendation = 0;
        long allUsersTime = System.currentTimeMillis();
        List<User> users = recommendationDB.getAllUsers();
        logger.debug("Retrieved all users in {}", getAmountOfTimeTaken(allUsersTime));
        for (User user : users) {
            if (user.isTimeToGenerateRecs()) {
                logger.trace("Generating recommendation process for user: {}", user.getUserId());
                int userId = userIndexMap.getItemByItemId(user.getUserId());
                time = System.currentTimeMillis();
                ToolUseCollection history = commandDB.getUsersUsageData(user.getUserId());
                logger.trace("Retrieving Usage Data for user: {}, number of entries: {}, in {}", user.getUserId(), history.size(), getAmountOfTimeTaken(time));
                time = System.currentTimeMillis();
                RecommendationCollector recommendations = recGen.getRecommendationsForUser(user, history, amount, userId);
                logger.trace("Recommendations for user: {}, gathered in {}", user.getUserId(), getAmountOfTimeTaken(time));
                user.saveRecommendations(recommendations, algorithmType.getRationale(), algorithmType.name(), toolIndexMap);
                logger.trace("Saved and completed recommendation gathering process for user: {}", user.getUserId());
                totalUserRecommendation++;
            }
        }
        logger.debug("Finished generating recommendations for {} users in {}", totalUserRecommendation, getAmountOfTimeTaken(allUsersTime));
    }

    private static String getAmountOfTimeTaken(long time) {
        long difference = System.currentTimeMillis() - time;
        long second = (difference / 1000) % 60;
        long minute = (difference / (1000 * 60) % 60);
        long hour = (difference / (1000 * 60 * 60) % 60);
        return String.format("%02d:%02d:%02d:%d", hour, minute, second, (difference%1000));
    }

    private static Options createCommandLineOptions() {
        Options options = new Options();
        options.addOption(COMMAND_HOST, true, "Specify the host of your command data store. Default: " + dbUrl);
        options.addOption(COMMAND_PORT, true, "Specify the port of your command data store on. Default: " + port);
        options.addOption(COMMAND_DB_NAME, true, "Name for the database which contains your commands. Default: " + dbName);
        options.addOption(RECOMMENDATION_HOST, true, "Specify the host of your recommendation data store. Default: Same as command data store");
        options.addOption(RECOMMENDATION_PORT, true, "Specify the port of your recommendation data store. Default: Same as command data store");
        options.addOption(RECOMMENDATION_DB_NAME, true, "Specify the name of the database that contains your recommendation and user data. Default: Same as command data store");
        options.addOption(COMMAND_USER, true, "User for your command data store. Default: none");
        options.addOption(COMMAND_PASS, true, "Password for the user for the command data store. Default: none");
        options.addOption(RECOMMENDATION_USER, true, "User for your recommendation data store. Default: none");
        options.addOption(RECOMMENDATION_PASS, true, "Password for the user for the recommendation data store. Default: none");
        options.addOption(AMOUNT, true, "Number of recommendations to generate for each user. Default: " + amount);
        options.addOption(ALGORITHM_TYPE, true, "Type of algorithm you want to use to generate the recommendations. Default: " + algorithmName);
        options.addOption(ACCEPTANCE_TYPE, true, "Acceptance type for the algorithm. Default: none");
        options.addOption(USE_CACHE, false, "Cache all usage data");
        return options;
    }

    private static void parseCommandLineArguments(String[] args) throws ParseException {
        Options options = createCommandLineOptions();
        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse(options, args);
        if(cmd.hasOption(COMMAND_HOST)){
            dbUrl = cmd.getOptionValue(COMMAND_HOST);
        }
        if(cmd.hasOption(COMMAND_PORT)){
            try {
                port = Integer.parseInt(cmd.getOptionValue(COMMAND_PORT));
            }catch (NumberFormatException ex){
                throw new ParseException("Command port number is not valid.");
            }
        }
        if(cmd.hasOption(COMMAND_DB_NAME)){
            dbName = cmd.getOptionValue(COMMAND_DB_NAME);
        }
        commandConnectionParameters = new ConnectionParameters(dbUrl, port, dbName);

        if(cmd.hasOption(COMMAND_USER)){
            commandConnectionParameters.setDbUser(cmd.getOptionValue(COMMAND_USER));
        }

        if(cmd.hasOption(COMMAND_PASS)){
            commandConnectionParameters.setDbPassword(cmd.getOptionValue(COMMAND_PASS));
        }

        if(cmd.hasOption(RECOMMENDATION_HOST)){
            dbUrl = cmd.getOptionValue(RECOMMENDATION_HOST);
        }
        if(cmd.hasOption(RECOMMENDATION_PORT)){
            try {
                port = Integer.parseInt(cmd.getOptionValue(RECOMMENDATION_PORT));
            }catch (NumberFormatException ex){
                throw new ParseException("Recommendation port number is not valid.");
            }
        }
        if(cmd.hasOption(RECOMMENDATION_DB_NAME)){
            dbName = cmd.getOptionValue(RECOMMENDATION_DB_NAME);
        }
        recommendationConnectionParameters = new ConnectionParameters(dbUrl, port, dbName);

        if(cmd.hasOption(RECOMMENDATION_USER)){
            recommendationConnectionParameters.setDbUser(cmd.getOptionValue(RECOMMENDATION_USER));
        }else{
            recommendationConnectionParameters.setDbUser(commandConnectionParameters.getDbUser());
        }

        if(cmd.hasOption(RECOMMENDATION_PASS)){
            recommendationConnectionParameters.setDbPassword(cmd.getOptionValue(RECOMMENDATION_PASS));
        }else{
            recommendationConnectionParameters.setDbPassword(commandConnectionParameters.getDbPassword());
        }

        if(cmd.hasOption(USE_CACHE)){
            useCache = true;
        }

        if(cmd.hasOption(AMOUNT)){
            try {
                amount = Integer.parseInt(cmd.getOptionValue(AMOUNT));
            }catch (NumberFormatException ex){
                throw new ParseException("Not a valid amount.");
            }
        }

        if(cmd.hasOption(ALGORITHM_TYPE)){
            try {
                algorithmType = AlgorithmType.valueOf(cmd.getOptionValue(ALGORITHM_TYPE));
            }catch (IllegalArgumentException exp){
                throw new ParseException("Invalid algorithm");
            }
        }else{
            algorithmType = AlgorithmType.valueOf(algorithmName);
        }

        if(cmd.hasOption(ACCEPTANCE_TYPE)){
            try {
                acceptance = LearningAcceptanceType.valueOf(cmd.getOptionValue('c')).getAcceptance();
            }catch (IllegalArgumentException ex){
                throw new ParseException("Invalid acceptance type.");
            }
        }else{
            acceptance = null;
            if (algorithmType.needsAcceptance())
                throw new ParseException("Acceptance type must be specified for the selected algorithm");
        }
    }

    private static void initializeDatabases() throws DBConnectionException {
        userIndexMap = new IndexMap();
        toolIndexMap = new IndexMap();
        toolConverter = new EclipseCommandToolConverter(toolIndexMap);
        logger.debug("Connecting to Command database with: " + commandConnectionParameters.toString());
        commandDB = new MongoCommandDB(commandConnectionParameters, toolConverter, userIndexMap, useCache);
        logger.debug("Connecting to Recommendation database with: " + recommendationConnectionParameters.toString());
        recommendationDB = new MongoRecommendationDB(recommendationConnectionParameters, userIndexMap);
    }

}
