package ca.ubc.cs.commandrecommender.model.cf;

/**
 * Created by KeEr on 2014-06-23.
 */
//TODO: check over
public class UDCUsageModel extends UsageModel {

    private PreferenceMaker converter = new PreferenceMaker();

    public void insertUse(int useCount, long userid, long originid){
        converter.insert(makeUseOf(useCount, userid, originid));
    }

    public void finish(){
        usersToPrefs = converter.usersToPrefs();
        toolsToPrefs = converter.toolsToPrefs();

        super.recomputePreference();
    }

}
