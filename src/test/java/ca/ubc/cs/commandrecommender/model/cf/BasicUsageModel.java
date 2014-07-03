/**
 * 
 */
package ca.ubc.cs.commandrecommender.model.cf;

/**
 * A mock for UDCUsageModel for testing CF related classes
 */
public class BasicUsageModel extends UsageModel{
	
	PreferenceMaker pm = new PreferenceMaker();

	public ToolUsePreference makeUseOf(int useCount, Long userid, Long originid){
		ToolUsePreference ans = super.makeUseOf(useCount, userid, originid);
		pm.insert(ans);
		return ans;
	}
	
	public void done(){
		this.toolsToPrefs = pm.toolsToPrefs();
		this.usersToPrefs = pm.usersToPrefs();
	}
}