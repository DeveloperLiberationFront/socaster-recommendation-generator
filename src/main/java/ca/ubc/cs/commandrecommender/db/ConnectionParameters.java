package ca.ubc.cs.commandrecommender.db;

public class ConnectionParameters {
    private String dbUrl;
    private int dbPort;
    private String dbPassword;
    private String dbUser;
    private String dBName;

    public ConnectionParameters(String dbUrl, int dbPort, String cmdDbName, String dbUser, String dbPassword) {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPort = dbPort;
        this.dbPassword = dbPassword;
        this.dBName = cmdDbName;
    }

    public ConnectionParameters(String dbUrl, int dbPort, String dBName){
        this.dbUrl = dbUrl;
        this.dBName = dBName;
        this.dbPort = dbPort;
        this.dbUser = "";
        this.dbPassword = "";
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public String getDbUser() {
        return dbUser;
    }

    public int getDbPort() {
        return dbPort;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public String getdBName() {
        return dBName;
    }

    public String toString(){
        return "Host: " + dbUrl + "Port : " + dbPort + " Database Name: " + dBName + " User: " + dbUser;
    }
}
