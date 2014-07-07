package ca.ubc.cs.commandrecommender.generator;

import ca.ubc.cs.commandrecommender.model.RecommendationCollector;
import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.bag.TreeBag;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

/**
 * The base class for Linton recommendation algorithms
 */
public abstract class AbstractLintonRecGen extends AbstractRecGen {

	public AbstractLintonRecGen(String algorithm) {
		super(algorithm);
	}

	protected Bag<Integer> toolCount = new TreeBag<Integer>();
	protected Set<Integer> mostPopularToolKeys = new TreeSet<Integer>();


    /**
     * Fill {@code rc} with the first several legitimate commands or tools in
     * {@link ca.ubc.cs.commandrecommender.generator.AbstractLintonRecGen#mostPopularToolKeys}
     * which is determined by subclass implementation of
     * {@link ca.ubc.cs.commandrecommender.generator.AbstractLintonRecGen#trainWith(ca.ubc.cs.commandrecommender.model.ToolUseCollection)}.
     *
     * @param rc is modified
     */
	@Override
	public void fillRecommendations(RecommendationCollector rc) {
		checkSorting();
		for(Integer toolToRecommend : mostPopularToolKeys){
			if(!rc.toolsContain(toolToRecommend)){
                double percentUsage = getPercentUsage(toolToRecommend);
				rc.add(toolToRecommend, (double) toolCount.getCount(toolToRecommend));
				if(rc.isSatisfied())
					break;
			}			
		}
	}



    @Override
    public void runAlgorithm() {
        // Nothing to do here
    }

    protected abstract  double getPercentUsage(Integer toolToRecommend);
    /**
     * Ensures that {@link AbstractLintonRecGen#mostPopularToolKeys}
     * are sorted by most popular ones first
     */
	private synchronized void checkSorting() {
		if(mostPopularToolKeys.size()!= toolCount.uniqueSet().size()){
			mostPopularToolKeys = new TreeSet<Integer>(new Comparator<Integer>() {
				public int compare(Integer a, Integer b) {
					//gotta use two because of infinite recursion
					int countA = toolCount.getCount(a);
					int countB = toolCount.getCount(b);
					
					//if the two are equal, don't return 0, 
					//or they'll be smooshed together!
					if(countA==countB)
						return a.compareTo(b);
					else
						return -(countA-countB);
				}
			});
			mostPopularToolKeys.addAll(toolCount.uniqueSet());
		}
	}

}