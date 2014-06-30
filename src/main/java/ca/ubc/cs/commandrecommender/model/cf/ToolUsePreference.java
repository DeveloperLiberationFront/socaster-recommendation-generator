package ca.ubc.cs.commandrecommender.model.cf;

import org.apache.mahout.cf.taste.model.Preference;

/**
 * Encapsulates a command (or tool) and a preference value, which indicates the
 * strength of the preference for the command. the values are associated to users.
 *
 * Created by KeEr on 2014-06-23.
 */
public class ToolUsePreference implements Preference {

    private final long toolId;
    private final long userId;
    private float value;

    public ToolUsePreference(long toolId, long userId, int value) {
        this.toolId = toolId;
        this.userId = userId;
        this.value = value;
    }

    @Override
    public long getItemID() {
        return toolId;
    }

    @Override
    public long getUserID() {
        return userId;
    }

    @Override
    public float getValue() {
        return value;
    }

    @Override
    public void setValue(float value) {
        this.value = value;
    }

    @Override
    public String toString(){
        return userId +"@"+ toolId + "="+value;
    }

}
