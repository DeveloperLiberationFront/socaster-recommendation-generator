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
