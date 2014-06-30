package ca.ubc.cs.commandrecommender.model.cf;

import ca.ubc.cs.commandrecommender.model.cf.matejka.MatejkaPreferenceAdjustment;

import java.util.HashSet;
import java.util.Set;

/**
 * The DataModel ({@link org.apache.mahout.cf.taste.model.DataModel}) for Learning (discovery) related
 * CF algorithms.
 */
//TODO: maybe make the typing more strict to avoid later casting
public class LearningModel extends GenericUsageModel<ToolUsePreference, ItemFactory, UserFactory> {

    private LearningRulePreferenceMaker pm = new LearningRulePreferenceMaker();

    public LearningModel() {
        super(new ProgrammerFactory(), new LearningRuleFactory());
    }

    public LearningRuleFactory getLearningRuleFactory() {
        return (LearningRuleFactory) itemFactory;
    }

    protected void recomputePreference(){
        new MatejkaPreferenceAdjustment(this).recomputePreferences();
    }

    /**
     * Put the information of the learning sequence (p) and the userid into the model
     * @param userid
     * @param p
     */
    public void makeUseOf(int userid, Pair p){
        //TODO: the casting here is rather ugly
        Long itemid = ((LearningRuleFactory)itemFactory).getOrCreateToolForName(p);
        ((ProgrammerFactory)userFactory).getOrCreateProgrammerForName((long) userid);

        Set<Long> users = itemsToUsers.get(itemid);
        if(users == null){
            users = new HashSet<Long>();
            itemsToUsers.put(itemid,users);
        }
        users.add((long) userid);

        pm.updatePrefs(userid, itemid);
    }

    /**
     * finalize the model
     */
    public void done(){
        this.toolsToPrefs = pm.toolsToPrefs();
        this.usersToPrefs = pm.usersToPrefs();
    }

}
