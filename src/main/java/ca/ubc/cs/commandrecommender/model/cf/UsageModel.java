package ca.ubc.cs.commandrecommender.model.cf;

import ca.ubc.cs.commandrecommender.model.cf.matejka.MatejkaPreferenceAdjustment;

import java.util.HashSet;
import java.util.Set;

/*
 * Everything here should be linear time, with the exception of 
 * getNumUsersWithPreferenceFor, which could be precomputed, 
 * but would produce too-large data structure.  It's no worse
 * than linear. 
 */
//TODO: check over
public abstract class UsageModel extends GenericUsageModel<ToolUsePreference, ItemFactory, UserFactory>
			implements IPreferenceMapper<ToolUsePreference> {
	

	public UsageModel() {
		super(new ProgrammerFactory(),new ToolFactory());
	}
	
	protected void recomputePreference(){
		new MatejkaPreferenceAdjustment(this).recomputePreferences();
	}
	
	protected ToolUsePreference makeUseOf(int useCount, long userid, long originid){
		//originid represents toolid
		((ToolFactory)itemFactory).getOrCreateToolForName(originid);
		((ProgrammerFactory)userFactory).getOrCreateProgrammerForName(userid); 
		ToolUsePreference use = new ToolUsePreference(originid,userid,useCount);
		
		Set<Long> users = itemsToUsers.get(originid);
		if(users == null){
			users = new HashSet<Long>();
			itemsToUsers.put((long) originid,users);
		}
		users.add((long) userid);
		
		return use;
	}
}
