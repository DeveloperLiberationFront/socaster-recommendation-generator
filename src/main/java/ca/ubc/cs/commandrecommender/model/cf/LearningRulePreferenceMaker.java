package ca.ubc.cs.commandrecommender.model.cf;

import java.util.List;

/**
 * A helper class to allow insertion and update of LearningRule related ToolUsePreference
 */
public class LearningRulePreferenceMaker extends PreferenceMaker {

    public void updatePrefs(long userId, long itemId) {
        ToolUsePreference use = new ToolUsePreference((long)userId, itemId, 1);
        if (!incrementUsersToPrefs(userId, itemId))
            genericInsert(usersToPrefs, use.getUserID(), use);
        if (!incrementToolsToPrefs(userId, itemId))
            genericInsert(toolsToPrefs, use.getItemID(), use);
    }

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
