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



###Algorithms
####Overview
These are different methods to generate recommendations. This page provides an overview of all the current algorithms we have in the system. Please also refer to this paper ("Improving software developers' fluency by recommending development environment commands" is the name of the paper if the link is broken).

####Collaborative Filtering
These algorithm uses the recommenders from Apache Mahout with Matejka's similarity and preference computation algorithm to generate recommendations. The preference level of a user for an item (command in our case) is the user's use count of that command. 
 - **Item-based**
   - Name: ITEM_BASED_CF
   - Make recommendations by finding similarities between items
 - **User-based**
   - Name: USER_BASED_CF
   -Make recommendations by looking at the data of similar users determined by the commands used
 - **Latent matrix factorization**
   -Name: LATENT_MODEL_BASED_CF
   -Uses SVDRecommender from Mahout


####Learning Rule (Discovery)
Construct learning sequences based on the order in which commands are used. For users to qualify for these algorithms, the user must have an existing knowledge base of commands, that is, at least 40 one-hour sessions. The general idea is if a person invoke command A before invoking command B for the first time, then we have a learning pattern A->B, if B is outside the knowledge base. There may be additional criteria specified by Learning Acceptance Type for such sequence to be counted as a learning pattern. Then, these sequences are processed in different ways to generate recommendations.

**Learning Acceptance Types:**
 - INCLUDE_ALL: any learning sequences are accepted
 - MULTI_SESSION: for a command to be considered as learned, the command has to be used in different sessions (hours)
 - MULTI_USE:  for a command to be considered as learned, the command has to be use more than once

 
The algorithms that come under this category:
 - **Most popular**
   - Name: MOST_POPULAR_LEARNING_RULE
   - This algorithm recommend the most commonly "learned" or "discovered" commands that a user is not using. Really, it's just the topmost popular commands beyond people's knowledge base
 - **Advanced**
   - Name: LEARNING_RULE
   - Recommends the most popular discoveries that a user has the prerequisites for. For example, if we have A->C, C->D, A->C, A->B as our discovery pattern and Person1 only used A, then we would recommend C first and then B but we won't recommend D because the user does not know C yet.
 - **Most prerequisite**
   - Name: MOST_PREREQ_LEARNING_RULE
   - Recommends commend based on the number of prerequisite commands a user use for learning a unknown command


####Collaborative Filtering with Learning (Discovery)
This category of algorithms uses collaborative filtering on the learning sequences extracted by the Learning Rule algorithms to recommend learning sequences. These learning sequences are then processed to recommend commands based on what the user has already used before. If the sequence A -> B is recommended by the CF recommender, we recommend B if user has used A, recommend A if user has not used A.

 - Item-based learning rule
   - Name: ITEM_BASED_CF_WITH_DISCOVERY
 - User-based learning rule
   - Name: USER_BASED_CF_WITH_DISCOVERY

####Others
 - Most widely used
   - Name: MOST_WIDELY_USED
   - The command with the most number of users gets recommended first
 - Most frequently used
   - Name: MOST_FREQUENTLY_USED
   - The most frequently used command gets recommended first
 - Hotkey not used (deprecated)
   - Name: HOTKEY_NOT_USED
   - The commands that the user has not used a hotkey for
