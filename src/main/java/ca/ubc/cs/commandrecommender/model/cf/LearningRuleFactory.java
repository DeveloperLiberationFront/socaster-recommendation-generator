package ca.ubc.cs.commandrecommender.model.cf;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of {@link ca.ubc.cs.commandrecommender.model.cf.ItemFactory}
 * for Learning-related CF algorithms
 */
public class LearningRuleFactory implements ItemFactory {

    private BiMap<Pair, Long> ruleMap = HashBiMap.create();

    private long count = 0;

    public long getOrCreateToolForName(Pair pair){
        Long itemIndex = ruleMap.get(pair);
        if(itemIndex == null) {
            ruleMap.put(pair, count);
            itemIndex = count;
            count++;
        }
        return itemIndex;
    }

    @Override
    public Long toolForToolID(Long itemID) {
        if(ruleMap.containsValue(itemID)) {
            return itemID;
        }
        return (long) -1;
    }

    public Pair pairForToolID(Long itemID) {
        return ruleMap.inverse().get(itemID);
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
