package ca.ubc.cs.commandrecommender.model.cf;

import java.util.List;

/**
 * A helper class to allow insertion and update of LearningRule related ToolUsePreference
 */
public class LearningRulePreferenceMaker extends PreferenceMaker {

    /**
     * update and insert the preferences related to userId and itemId
     * to build a usable data model
     */
    public void updatePrefs(long userId, long itemId) {
        ToolUsePreference use = new ToolUsePreference(itemId, userId, 1);
        if (!incrementUsersToPrefs(userId, itemId))
            genericInsert(usersToPrefs, use.getUserID(), use);
        if (!incrementToolsToPrefs(userId, itemId))
            genericInsert(toolsToPrefs, use.getItemID(), use);
    }

    //update usersToPrefs with the userId and the itemId if possible
    private boolean incrementUsersToPrefs(long userId, long itemId) {
        List<ToolUsePreference> list = usersToPrefs.get(userId);
        if (list == null)
            return false;
        for (ToolUsePreference toolUse : list) {
            if(toolUse.getItemID()==itemId){
                toolUse.setValue(toolUse.getValue()+1);
                return true;
            }
        }
        return false;
    }

    //update toolsToPrefs with the userId and the itemId if possible
    private boolean incrementToolsToPrefs(long userId, long itemId) {
        List<ToolUsePreference> list = toolsToPrefs.get(itemId);
        if (list == null)
            return false;
        for (ToolUsePreference toolUse : list) {
            if(toolUse.getUserID()==userId){
                toolUse.setValue(toolUse.getValue()+1);
                return true;
            }
        }
        return false;
    }

}
