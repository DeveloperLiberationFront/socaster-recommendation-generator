package ca.ubc.cs.commandrecommender.model.cf;

/**
 * A model to keep track of relations between users, items and preferences
 * For CF related algorithms
 */
public class PreferenceMaker extends GenericPreferenceMaker<ToolUsePreference>{

    @Override
	protected ToolUsePreference[] makeArray(int size) {
		return new ToolUsePreference[size];
	}

    /**
     * Insert the given use into the model (the two maps) of the class.
     * @param use
     */
	public void insert(ToolUsePreference use){
		genericInsert(usersToPrefs,use.getUserID(),use);		
		genericInsert(toolsToPrefs,use.getItemID(),use);
	}
}