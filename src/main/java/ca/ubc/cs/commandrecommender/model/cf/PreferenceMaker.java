package ca.ubc.cs.commandrecommender.model.cf;

/**
 *
 *
 */
//TODO: check over
public class PreferenceMaker extends GenericPreferenceMaker<ToolUsePreference>{

    @Override
	protected ToolUsePreference[] makeArray(int size) {
		return new ToolUsePreference[size];
	}
	
	@Override
	public void insert(ToolUsePreference use){
		genericInsert(usersToPrefs,use.getUserID(),use);		
		genericInsert(toolsToPrefs,use.getItemID(),use);
	}
}