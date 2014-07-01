package ca.ubc.cs.commandrecommender.model.cf;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of {@link ca.ubc.cs.commandrecommender.model.cf.ItemFactory}
 * for Learning-related CF algorithms
 */
//TODO: check over
//TODO: might want to use a bimap instead to improve efficiency
public class LearningRuleFactory implements ItemFactory {


    private Map<Pair, Long> ruleMap = new HashMap<Pair, Long>();

    private long count = 0;

    //TODO: P1 the return value of this method seems very strange
    public long getOrCreateToolForName(Pair pair){
        if(!ruleMap.containsKey(pair)) {
            ruleMap.put(pair, count);
            count++;
        }
        return count;
    }

    @Override
    public Long toolForToolID(Long itemID) {
        if(ruleMap.containsValue(itemID)) {
            return itemID;
        }
        return (long) -1;
    }

    public Pair pairForToolID(Long itemID) {
        Set<Pair> keySet = ruleMap.keySet();
        for (Iterator iterator = keySet.iterator(); iterator.hasNext();) {
            Pair pair = (Pair) iterator.next();
            if(ruleMap.get(pair).equals(itemID))
                return pair;
        }
        return null;
    }

    @Override
    public long[] tools() {
        Object[] a = ruleMap.values().toArray();
        //TODO: (Minor) better way?
        long[] tools= new long[a.length];
        for (int i = 0; i < a.length; i++) {
            tools[i]= (Long) a[i];
        }

        return tools;
    }

}
