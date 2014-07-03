package ca.ubc.cs.commandrecommender.model.cf;

/**
 * A DataModel for pure CF related algorithms.
 */
public class UDCUsageModel extends UsageModel {

    private PreferenceMaker converter = new PreferenceMaker();

    /**
     * Put the information about what user used which tool how many times into
     * the model
     * @param useCount
     * @param userid
     * @param originid
     */
    public void insertUse(int useCount, long userid, long originid){
        converter.insert(makeUseOf(useCount, userid, originid));
    }

    /**
     * Finalize the model and recompute the preference
     */
    public void finish(){
        usersToPrefs = converter.usersToPrefs();
        toolsToPrefs = converter.toolsToPrefs();

        super.recomputePreference();
    }

}
