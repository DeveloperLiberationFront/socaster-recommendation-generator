package ca.ubc.cs.commandrecommender.model.cf;

import org.apache.mahout.cf.taste.model.Preference;

import java.util.*;

/**
 * A generic model to keep track of relations between users, items and preferences
 * For CF related algorithms. Basically maintains a map of
 * @param <P> the type of Preference being modeled
 */
public abstract class GenericPreferenceMaker<P extends Preference>{
	
	protected Map<Long, List<P>> usersToPrefs = new HashMap<Long, List<P>>();
	protected Map<Long, List<P>> toolsToPrefs = new HashMap<Long, List<P>>();

    /**
     * Get the usersToPrefs map
     * @return
     */
	public Map<Long, P[]> usersToPrefs(){
		Map<Long, P[]> prefs = new HashMap<Long, P[]>();
		genericCopy(usersToPrefs,prefs);
		return prefs;
	}

    /**
     * Get the toolsToPrefs map
     * @return
     */
	public Map<Long, P[]> toolsToPrefs() {
		Map<Long, P[]> prefs = new HashMap<Long, P[]>();
		genericCopy(toolsToPrefs,prefs);
		return prefs;
	}

	/**
	 * @param source
	 * 
	 * @param destination a copy of the source with the values being an array. The values in the array are
	 * 			sorted by Item, as specified  by {@link //TODO User#getPreferencesAsArray()}
	 */
	private <T> void genericCopy(Map<T, List<P>> source, Map<T, P[]> destination){
		
		for(Map.Entry<T, List<P>> entry : source.entrySet()){
			P[] prefs = makeArray(entry.getValue().size());
			int i = 0;
			for(P pref : entry.getValue()){
				prefs[i++] = pref;
			}
			Arrays.sort(prefs,new Comparator<P>() {

				@Override
				public int compare(P a,
								   P b) {
					
					return a.getItemID() == b.getItemID() ? 0
							: (a.getItemID() < b.getItemID() ? -1 : 1);
					//TODO: just do a simple subtract?
				}
			});
			destination.put(entry.getKey(), prefs);
		}
	}

    /**
     * For inserting the user-preference or item-preference information into
     * one of the map fields
     * @param map
     * @param key
     * @param value
     * @param <T>
     */
	public <T> void genericInsert(Map<T, List<P>> map, T key, P value){
		List<P> allUses = map.get(key);
		if(allUses==null){
			allUses = new ArrayList<P>();
			map.put(key, allUses);
		}
		allUses.add(value);
	}

	protected abstract P[] makeArray(int size);

}