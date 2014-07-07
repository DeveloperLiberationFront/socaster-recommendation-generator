package ca.ubc.cs.commandrecommender.generator;


import ca.ubc.cs.commandrecommender.model.ToolUseCollection;

/**
 * This algorithm recommends the most widely used commands (most number of users uses it)
 * which the target user is not using
 */
public class LintonUserRecGen extends AbstractLintonRecGen {

    private int numUsers = 0;

    public LintonUserRecGen(String algorithm) {
        super(algorithm);
    }

    @Override
    protected double getPercentUsage(Integer toolToRecommend) {
        double numberOfToolUses = toolCount.getCount(toolToRecommend);
        return (numberOfToolUses / numUsers) * 100;
    }

    @Override
    public void trainWith(ToolUseCollection tools) {
        toolCount.addAll(tools.toolsUsedBag().uniqueSet());
        numUsers++;
    }

}