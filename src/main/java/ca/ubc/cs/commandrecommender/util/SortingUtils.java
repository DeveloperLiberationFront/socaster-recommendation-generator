package ca.ubc.cs.commandrecommender.util;

import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.SortedBag;
import org.apache.commons.collections4.bag.TreeBag;

import java.util.Comparator;

/**
 * Provide utility functions for sorting
 * Created by KeEr on 2014-06-19.
 */
public class SortingUtils {

    /**
     * Return a new bag whose elements are the elements in tempRecs but sorted
     * by putting the most occurring element first. The original list is not modified
     * @param tempRecs
     * @return
     */
    public static SortedBag<Integer> sortBagByCount(final Bag<Integer> tempRecs) {
        SortedBag<Integer> recs = new TreeBag<Integer>(new Comparator<Integer>() {
            public int compare(Integer a, Integer b) {
                //gotta use two because of infinite recursion
                int countA = tempRecs.getCount(a);
                int countB = tempRecs.getCount(b);

                //if the two are equal, don't return 0,
                //or they'll be smooshed together!
                if(countA==countB)
                    return a.compareTo(b);
                else
                    return -(countA-countB);
            }
        });
        recs.addAll(tempRecs);
        return recs;
    }

}
