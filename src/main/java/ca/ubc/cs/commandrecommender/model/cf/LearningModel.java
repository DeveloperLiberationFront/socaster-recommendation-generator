package ca.ubc.cs.commandrecommender.model.cf;

import ca.ubc.cs.commandrecommender.model.cf.matejka.MatejkaPreferenceAdjustment;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by KeEr on 2014-06-23.
 */
//TODO: check over
public class LearningModel extends GenericUsageModel<ToolUsePreference, ItemFactory, UserFactory> {

    public LearningModel() {
        super(new ProgrammerFactory(), new LearningRuleFactory());
    }

    public LearningRuleFactory getLearningRuleFactory() {
        return (LearningRuleFactory) itemFactory;
    }

    protected void recomputePreference(){
        new MatejkaPreferenceAdjustment(this).recomputePreferences();
    }

    private LearningRulePreferenceMaker pm = new LearningRulePreferenceMaker();

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

    public void done(){
        this.toolsToPrefs = pm.toolsToPrefs();
        this.usersToPrefs = pm.usersToPrefs();
    }

}
