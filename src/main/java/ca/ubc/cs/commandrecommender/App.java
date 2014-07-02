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
    private static AbstractCommandToolConverter toolConverter;
    private static ConnectionParameters connectionParameters;
    private static AbstractCommandDB commandDB;
    private static AbstractRecommendationDB recommendationDB;
    private static IndexMap userIndexMap;
    private static IndexMap toolIndexMap;
    private static String dbName = "commands-production";
    private static String dbUrl = "localhost";
    private static int port = 27017;
    private static int amount = 10;
    private static String algoName = "MOST_WIDELY_USED";
    private static AlgorithmType algoType;
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

        IRecGen recGen = algoType.getRecGen(acceptance);
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
        options.addOption("h", true, "Specify the host of your data store. Default: " + dbUrl);
        options.addOption("p", true, "Specify the port to connect to to your data store on. Default: " + port);
        options.addOption("n", true, "Name for the database which contains your commands. Default: " + dbName);
        options.addOption("u", true, "User for your data store. Default: none");
        options.addOption("P", true, "Password for the user for data store. Default: none");
        options.addOption("a", true, "Number of recommendations to generate for each user. Default: " + amount);
        options.addOption("t", true, "Type of algorithm you want to use to generate the recommendations. Default: " + algoName);
        options.addOption("c", true, "Acceptance type for the algorithm. Default: none");
        return options;
    }

    private static void parseCommandLineArguments(String[] args) throws ParseException {
        Options options = createCommandLineOptions();
        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse(options, args);
        if(cmd.hasOption('h')){
            dbUrl = cmd.getOptionValue('h');
        }
        if(cmd.hasOption('p')){
            try {
                port = Integer.parseInt(cmd.getOptionValue('p'));
            }catch (NumberFormatException ex){
                throw new ParseException("Not a valid port number.");
            }
        }
        if(cmd.hasOption('n')){
            dbName = cmd.getOptionValue('n');
        }
        connectionParameters = new ConnectionParameters(dbUrl, port, dbName);

        if(cmd.hasOption('u')){
            connectionParameters.setDbUser(cmd.getOptionValue('u'));
        }

        if(cmd.hasOption('P')){
            connectionParameters.setDbPassword(cmd.getOptionValue('P'));
        }

        if(cmd.hasOption('a')){
            try {
                amount = Integer.parseInt(cmd.getOptionValue('a'));
            }catch (NumberFormatException ex){
                throw new ParseException("Not a valid amount.");
            }
        }

        if(cmd.hasOption('t')){
            try {
                algoType = AlgorithmType.valueOf(cmd.getOptionValue('t'));
            }catch (IllegalArgumentException exp){
                throw new ParseException("Invalid algorithm");
            }
        }else{
            algoType = AlgorithmType.valueOf(algoName);
        }

        if(cmd.hasOption('c')){
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
        commandDB = new MongoCommandDB(connectionParameters, toolConverter, userIndexMap);
        recommendationDB = new MongoRecommendationDB(connectionParameters, toolConverter, userIndexMap);
    }

}
