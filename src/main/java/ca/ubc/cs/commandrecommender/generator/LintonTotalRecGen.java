package ca.ubc.cs.commandrecommender.generator;


import ca.ubc.cs.commandrecommender.model.ToolUseCollection;

/**
 * This algorithm recommends the most frequently used commands that a user is not using
 */
public class LintonTotalRecGen extends AbstractLintonRecGen {


    private double totalToolUses = -1;

    public LintonTotalRecGen(String algorithm) {
        super(algorithm);
    }

    @Override
    protected double getPercentUsage(Integer toolToRecommend) {
        if(totalToolUses == -1){
            initializeToolUses();
        }
        double numberOfToolUses = toolCount.getCount(toolToRecommend);
        return (numberOfToolUses / totalToolUses) * 100;
    }

    private void initializeToolUses() {
        for(int toolUses : toolCount){
            totalToolUses+= toolUses;
        }
    }
    @Override
    public void trainWith(ToolUseCollection tools) {
        toolCount.addAll(tools.toolsUsedBag());
    }

}