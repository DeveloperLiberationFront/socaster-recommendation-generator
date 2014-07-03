package ca.ubc.cs.commandrecommender.model;

import ca.ubc.cs.commandrecommender.model.learning.Transaction;
import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.bag.HashBag;
import org.apache.commons.collections4.set.ListOrderedSet;

import java.util.*;

/**
 * The usage history for a user and related helpful utility methods
 */
public class ToolUseCollection extends ArrayList<ToolUse>{

    public final int userId;

    public ToolUseCollection(){
        this(-1);
    }

    public ToolUseCollection(int userId){
        super();
        this.userId = userId;
    }

    public ToolUseCollection(int userId, Collection<? extends ToolUse> c) {
        super(c);
        this.userId = userId;
    }

    public ToolUseCollection(int userId, int initialCapacity) {
        super(initialCapacity);
        this.userId = userId;
    }

    public boolean userIsIdentified(){
        return userId!=-1;
    }

    public boolean userIdIs(int id){
        return userId==id;
    }

    private static final long serialVersionUID = 8820630451728423087L;

    public ListOrderedSet<Integer> toolsUsedInOrder(){
        ListOrderedSet<Integer> set = new ListOrderedSet<Integer>();
        for(ToolUse u : this)
            set.add(u.tool);
        return set;
    }

    public String firstTimeUsed(int toolId) {
        for(ToolUse tu : this){
            if(tu.tool==toolId)
                return tu.time.toString();
        }
        return "<unknown>";
    }

    public Bag<Integer> toolsUsedBag(){
        Bag<Integer> bag = new HashBag<Integer>();
        for(ToolUse u : this)
            bag.add(u.tool);
        return bag;
    }

    public HashSet<Integer> toolsUsedHashSet() {
        HashSet<Integer> set = new HashSet<Integer>();
        for(ToolUse u : this)
            set.add(u.tool);
        return set;
    }

    public List<Transaction> divideIntoTransactions() {

        List<Transaction> ts = new ArrayList<Transaction>();

        //TODO:this is wasteful for transactions that contain a lot less commands than the toolCount
        int toolCount = 10000;

        Transaction trans = new Transaction(toolCount);
        for(ToolUse t : this){

            if (!trans.include(userId, t.time)) {

                ts.add(trans);
                trans = new Transaction(toolCount,userId,t.time,t.tool);
            }else{
                trans.add(t.tool);
            }
        }

        ts.add(trans);

        return ts;
    }

    /**
     * A convenience method for sorting the uses by timestamp
     */
    public void sort() {
        Collections.sort(this, new Comparator<ToolUse>() {

            @Override
            public int compare(ToolUse o1, ToolUse o2) {
                return o1.time.compareTo(o2.time);
            }
        });
    }

}