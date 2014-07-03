package ca.ubc.cs.commandrecommender;


import ca.ubc.cs.commandrecommender.Exception.DBConnectionException;
import ca.ubc.cs.commandrecommender.db.*;
import ca.ubc.cs.commandrecommender.generator.AlgorithmType;
import ca.ubc.cs.commandrecommender.generator.IRecGen;
import ca.ubc.cs.commandrecommender.model.IndexMap;
import ca.ubc.cs.commandrecommender.model.ToolUseCollection;
import ca.ubc.cs.commandrecommender.model.User;
import ca.ubc.cs.commandrecommender.model.acceptance.AbstractLearningAcceptance;
import ca.ubc.cs.commandrecommender.model.acceptance.LearningAcceptanceType;
import org.apache.commons.cli.*;

import java.net.UnknownHostException;

/**
 * Created by KeEr on 2014-06-09.
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
    public static final String ALORITHM_TYPE = "t";
    public static final String ACCEPTANCE_TYPE = "c";
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
        IRecGen recGen = algorithmType.getRecGen(acceptance);
        String reason = recGen.getAlgorithmUsed();
        for (ToolUseCollection uses : commandDB.getAllUsageData())
            recGen.trainWith(uses);

        recGen.runAlgorithm();
        for (User user : recommendationDB.getAllUsers()) {
            if (user.isTimeToGenerateRecs()) {
                int userId = userIndexMap.getItemByItemId(user.getUserId());
                ToolUseCollection history = commandDB.getUsersUsageData(user.getUserId());
                Iterable<Integer> recommendations = recGen.getRecommendationsForUser(user, history, amount, userId);
                user.saveRecommendations(recommendations, reason, toolIndexMap);
                user.updateRecommendationStatus();
            }
        }
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
        options.addOption(ALORITHM_TYPE, true, "Type of algorithm you want to use to generate the recommendations. Default: " + algorithmName);
        options.addOption(ACCEPTANCE_TYPE, true, "Acceptance type for the algorithm. Default: none");
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

        if(cmd.hasOption(AMOUNT)){
            try {
                amount = Integer.parseInt(cmd.getOptionValue(AMOUNT));
            }catch (NumberFormatException ex){
                throw new ParseException("Not a valid amount.");
            }
        }

        if(cmd.hasOption(ALORITHM_TYPE)){
            try {
                algorithmType = AlgorithmType.valueOf(cmd.getOptionValue(ALORITHM_TYPE));
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
        }
    }

    private static void initializeDatabases() throws DBConnectionException {
        userIndexMap = new IndexMap();
        toolIndexMap = new IndexMap();
        toolConverter = new EclipseCommandToolConverter(toolIndexMap);
        commandDB = new MongoCommandDB(commandConnectionParameters, toolConverter, userIndexMap);
        recommendationDB = new MongoRecommendationDB(recommendationConnectionParameters, toolConverter, userIndexMap);
    }

}
