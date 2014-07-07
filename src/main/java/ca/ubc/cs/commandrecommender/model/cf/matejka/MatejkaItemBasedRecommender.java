package ca.ubc.cs.commandrecommender.model.cf.matejka;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveArrayIterator;
import org.apache.mahout.cf.taste.impl.recommender.AbstractRecommender;
import org.apache.mahout.cf.taste.impl.recommender.TopItems;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.recommender.IDRescorer;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Item-based recommender based on Matejka tuning and similarity.
 * Simplified version of 
 * {@link org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender}
 */
public final class MatejkaItemBasedRecommender extends AbstractRecommender implements
		Recommender {
	
	/*
	 * Alpha tuning param from matejka p. 196
	 */
	private double alpha;
	private MatejkaSimilarity similarity;

	public MatejkaItemBasedRecommender(DataModel dataModel, MatejkaOptions ops) {
		super(dataModel);

		try {
			similarity = new MatejkaSimilarity(dataModel, ops);
		} catch (TasteException e) {
			e.printStackTrace();
		}

		this.alpha = ops.alpha;
	}

	@Override
	public float estimatePreference(long userID, long itemID)
			throws TasteException {
        //TODO: Not obvious where or how this method is used
		DataModel model = getDataModel();
	    Float actualPref = model.getPreferenceValue(userID, itemID);
	    if (actualPref != null) {
	      return (float) (alpha*actualPref);
	    }
	    return (float) doEstimatePreference(userID, itemID);
	}

	@Override
	public List<RecommendedItem> recommend(long userID, int howMany,
			IDRescorer rescorer) throws TasteException {
		if (userID < 0) {
			throw new IllegalArgumentException("userID is negative");
		}
		if (howMany < 1) {
			throw new IllegalArgumentException("howMany must be at least 1");
		}

		DataModel model = getDataModel();
		PreferenceArray preferenceArray = model.getPreferencesFromUser(userID);
		if (preferenceArray == null || preferenceArray.length() == 0) {
			return Collections.emptyList();
		}

		FastIDSet allItems = getAllOtherItems(userID,preferenceArray); // TODO: possible failure point
		TopItems.Estimator<Long> estimator = new Estimator(userID);
		List<RecommendedItem> topItems = TopItems.getTopItems(howMany,
				new LongPrimitiveArrayIterator(allItems.toArray()), rescorer, estimator);

		return topItems;
	}

    // basically, the estimated preference is the average of the similarity between itemid and
    // all other items the user has preference of.
    //This is where we can get the item that it is being compared to.
	private double doEstimatePreference(long userid, long itemid)
			throws TasteException {
		
		DataModel model = getDataModel();
		PreferenceArray userPrefs = model.getPreferencesFromUser(userid);
		
		double sum = 0.0;
		
		for(Preference p : userPrefs){
			double itemSimilarity = similarity.itemSimilarity(itemid, p.getItemID());
			if(!Double.isNaN(itemSimilarity))
				sum += itemSimilarity;
		}		
		
		double ans = sum / (double)userPrefs.length();
		return ans==0.0 ? Double.NaN : ans;
	}

	@Override
	public void refresh(Collection<Refreshable> alreadyRefreshed) {
        //do nothing
    }

    // A wrapper class for estimating the preference of a user for an item
	private final class Estimator implements TopItems.Estimator<Long> {

		private final long theUser;

		private Estimator(long theUser) {
			this.theUser = theUser;
		}

		@Override
		public double estimate(Long item) throws TasteException {
			return doEstimatePreference(theUser, item);
		}
	}

}