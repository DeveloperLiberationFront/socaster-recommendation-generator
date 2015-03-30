generate recommendations

To build:

- mvn assembly:assembly -Dmaven.test.skip=true
- cd target
- unzip commandrecommender-1.0-SNAPSHOT-bin-with-dep.zip #if necessary choose 'A' for extract all, overwriting conflicts
- *jar* is in commandrecommender-1.0-SNAPSHOT

To run: 
- cd target/commandrecommender-1.0-SNAPSHOT
- java -jar commandrecommender-1.0-SNAPSHOT.jar

Commandline options available via -h


Documentation copied from UBC Atlassian:

##Recommendation Generator Overview
The Recommendation Generator accesses our database directly and uses collected data to generate recommendations using different algorithms. The current approach to record these recommendations, as requested by NCSU, is to store for each user the rank or recommendation value of each command determined by each algorithm we have. But this can be easily adjusted and customized with command line flags. The generator is supposed to run on a regular basis possibly scheduled by setting up a cron task on the server.

###How to use the command line tool to generate recommendations (manual page)

Note that the service can be run with no commands and will have the following defaults.   
`Host: localhost, Port: 27017, DBName: commands-production, User: none, password: none, Type: Most Widely Used: Acceptance type: none. `

RECGEN(1)                                                                         User Manuals                                                                        RECGEN(1)                         
NAME  
recgen - a tool to generate recommendations  
SYNOPSIS  
recgen [-t algo-type] [-a amount] [-l acceptance-type] [-c] [-u user-id]  
recgen [-report] [-a amount] [-p period]  


DESCRIPTION  
generate recommendations from the usage data collected and store the recommendation into database  
OPTIONS  
-t algorithm-type,  
Type of algorithm you want to use to generate the recommendations. Default: MOST_WIDELY_USED. Possible values: MOST_FREQUENTLY_USED, MOST_WIDELY_USED, HOTKEY_NOT_USED, MOST_POPULAR_LEARNING_RULE, USER_BASED_CF, USER_BASED_CF_WITH_DISCOVERY, LEARNING_RULE, MOST_PREREQ_LEARNING_RULE, ITEM_BASED_CF_WITH_DISCOVERY, ITEM_BASED_CF, LATENT_MODEL_BASED_CF, ALL  

-l acceptance-type,  
Acceptance type for the algorithm. Default: INCLUDE_ALL. Possible values: INCLUDE_ALL, MULTI_SESSION, MULTI_USE  

-a amount,  
The maximum number of commands to include in the user report. Default: unlimited-1  

-c,  
Cache all usage data  

-u user-id,  
Generate recommendations only for the user specified in this option. Default: generate recommendations for all users  

-report,  
Whether to generate report or recommendations. Default: false (generate recommendations)  

-p period,  
The time period for the usage report in days. Default: 7  

-rp port-number,   
Specify the port of your recommendation data store. Default: Same as command data store  

-cpass command-db-password,  
Specify password for the user for the command data store. Default: none  

-rh host-name,  
Specify the host of your recommendation data store. Default: Same as command data store  

-ru username,  
User for your recommendation data store. Default: none  

-rpass recommendation-db-password,  
Password for the user for the recommendation data store. Default: none  

-cu username,  
User for your command data store. Default: none  

-cp port-number,  
Specify the port of your command data store on. Default: 27017  

-cn table-name,  
Name for the database which contains your commands. Default: commands-development  

-ch host-name,  
Specify the host of your command data store. Default: localhost  

-rn database-name,  
Specify the name of the database that contains your recommendation and user data. Default: Same as command data store  

-recommendation_table table-name,  
Specify table that is used to store the generated recommendations. Default: recommendations  

-user_table table-name,  
Specify table name that is used to store the users. Default: users  

-command_detail_table table-name,  
Specify table that is used to store the command details. Default: command_details  

-command_table table-name,  
Table that is used for command data. Default: commands  

-h,  
Prints help (ie. the list of command line options and how to use them  
