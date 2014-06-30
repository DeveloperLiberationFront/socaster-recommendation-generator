package ca.ubc.cs.commandrecommender.generator;

import ca.pfv.spmf.Itemset;
import ca.ubc.cs.commandrecommender.model.RecommendationCollector;
import ca.ubc.cs.commandrecommender.model.acceptance.AbstractLearningAcceptance;
import ca.pfv.spmf.Sequence;
import ca.ubc.cs.commandrecommender.util.SortingUtils;
import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.SortedBag;
import org.apache.commons.collections4.bag.HashBag;

/**
 * Created by KeEr on 2014-06-20.
 */
public abstract class AbstractLearningRuleRecGen extends AbstractFilteredLearningRecGen {

    public AbstractLearningRuleRecGen(String algorithm,
                                      AbstractLearningAcceptance acceptance){
        super(algorithm, acceptance);
    }

    /**
     * Process {@link ca.ubc.cs.commandrecommender.generator.AbstractLearningRuleRecGen#trainer}
     * depending on how {@link ca.ubc.cs.commandrecommender.generator.AbstractLearningRuleRecGen#processSequence(ca.pfv.spmf.Itemset, ca.pfv.spmf.Itemset)}
     * is implemented in the subclasses
     */
    @Override
    public void runAlgorithm() {
        for(Sequence s : trainer){
            if(s.size() != 2){
                System.err.println("Sequence not in form antecedent->consequent:" + s);
                continue;
            }

            Itemset antecedent = s.get(0);
            Itemset consequent = s.get(1);

            processSequence(antecedent, consequent);
        }
    }

    /**
     * Fill {@code rc} based on how {@link ca.ubc.cs.commandrecommender.generator.AbstractLearningRuleRecGen#addRecsTo(Iterable, org.apache.commons.collections4.Bag)}
     * is implemented in the subclasses
     *
     * @param rc is modified
     */
    @Override
    public void fillRecommendations(RecommendationCollector rc) {

        final Bag<Integer> tempRecs = new HashBag<Integer>();

        addRecsTo(rc.tools(), tempRecs);

        // remove used tools
        for(Integer tool : rc.tools()){
            tempRecs.remove(tool);
        }

        SortedBag<Integer> recs = SortingUtils.sortBagByCount(tempRecs);

        for(Integer i : recs.uniqueSet()){
            rc.add(i, (double)recs.getCount(i));
            if(rc.isSatisfied())
                break;
        }
    }

    /**
     * Store and model {@code antecedent} and {@code consequent} so that this information
     * can be properly used to generate recommendations
     * @param antecedent
     * @param consequent
     */
    protected abstract void processSequence(Itemset antecedent, Itemset consequent);

    /**
     * Modify {@code tempRecs} with the stored information and history of an user so that
     * @param tools
     * @param tempRecs
     */
    protected abstract void addRecsTo(Iterable<Integer> tools, final Bag<Integer> tempRecs);

}
