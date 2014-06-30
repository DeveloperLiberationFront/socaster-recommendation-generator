package ca.ubc.cs.commandrecommender.model.cf.matejka;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.PreferenceInferrer;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import java.util.Collection;

//TODO: check over
public class MatejkaSimilarity implements UserSimilarity, ItemSimilarity{
	
	private boolean calculateJustWithOverlap;
	private boolean inferMissingValues;
	private DataModel dataModel;

	public MatejkaSimilarity(DataModel m, MatejkaOptions ops) throws TasteException {
		this.dataModel = m;
		this.inferMissingValues = ops.inferMissingValues;
		this.calculateJustWithOverlap = ops.calculateJustWithOverlap;
	}

	@Override
	public double userSimilarity(long userID1, long userID2)
			throws TasteException {

		if (userID1 < 0 || userID2 <0) {
			throw new IllegalArgumentException("user1 or user2 is negative");
		}
		double a = similarity(dataModel.getPreferencesFromUser(userID1),
                              dataModel.getPreferencesFromUser(userID2),
                              true);
		return a;
	}
	

	@Override
	public double itemSimilarity(long itemID1, long itemID2)
			throws TasteException {

	    if (itemID1 < 0 || itemID2 < 0) {
	      throw new IllegalArgumentException("item1 or item2 is negative");
	    }
	    
	    return similarity(dataModel.getPreferencesForItem(itemID1), 
	    				  dataModel.getPreferencesForItem(itemID2),
	    				  false);
	  }
	
		@SuppressWarnings("unchecked")
		public final double similarity(PreferenceArray xPrefs, PreferenceArray yPrefs,
				boolean userBased) throws TasteException {
	
			if (xPrefs.length() == 0 || yPrefs.length() == 0) {
				return Double.NaN;
			}
	
			Preference xPref = xPrefs.get(0);
			Preference yPref = yPrefs.get(0);
			Comparable xIndex = getComparable(userBased, xPref);
			Comparable yIndex = getComparable(userBased, yPref);
			int xPrefIndex = 1;
			int yPrefIndex = 1;
	
			double sumX2 = 0.0;
			double sumY2 = 0.0;
			double sumXY = 0.0;
	
			while (true) {
				int compare = xIndex.compareTo(yIndex);
				if (inferMissingValues || compare == 0) {
					double x;
					double y;
					if (compare == 0) {
						// Both users expressed a preference for the item
						x = xPref.getValue();
						y = yPref.getValue();
					} else {
						// Only one user expressed a preference, but infer the
						// other one's preference and tally
						// as if the other user expressed that preference
						if (compare < 0) {
							// X has a value; use 0 for Y's
							x = xPref.getValue();
							y = 0;
						} else {
							// compare > 0
							// Y has a value; use 0 for X's
							x = 0;
							y = yPref.getValue();
						}
					}
	
					if(xPref == yPref || !calculateJustWithOverlap){
						sumXY += x * y;
						sumX2 += x * x;
						sumY2 += y * y;
					}
				}
				if (compare <= 0) {
					if (xPrefIndex == xPrefs.length()) {
						if (inferMissingValues)
							sumY2 += squareSumRest(yPrefs, yPrefIndex);
						break;
					}
					xPref = xPrefs.get(xPrefIndex++);
					xIndex = getComparable(userBased, xPref);
				}
				if (compare >= 0) {
					if (yPrefIndex == yPrefs.length()) {
						if (inferMissingValues)
							sumX2 += squareSumRest(xPrefs,
									xPref == yPref ? xPrefIndex - 1 : xPrefIndex);
						break;
					}
					yPref = yPrefs.get(yPrefIndex++);
					yIndex = getComparable(userBased, yPref);
				}
			}
	
			return computeResult(sumXY, sumX2, sumY2);
		}
	
		private final Comparable<?> getComparable(boolean userBased, Preference pref) {
			return userBased ? pref.getItemID() : pref.getUserID();
		}
	
		private final double squareSumRest(PreferenceArray preferences, int startingFrom) {
			double squareSum = 0;
			for (int i = startingFrom; i < preferences.length(); i++) {
				double val = preferences.get(i).getValue();
				squareSum += val * val;
			}
			return squareSum;
		}
	
		private final double computeResult(double sumXY, double sumX2, double sumY2) {
			double denominator = Math.sqrt(sumX2) * Math.sqrt(sumY2);
			if (denominator == 0.0) {
				// One or both parties has -all- the same ratings;
				// can't really say much similarity under this measure
				return Double.NaN;
			}
			return sumXY / denominator;
		}

	@Override
	public void setPreferenceInferrer(PreferenceInferrer inferrer) {
		throw new RuntimeException("Cannot set inferrer here... I have my own!");
	}
	
	@Override
	public void refresh(Collection<Refreshable> alreadyRefreshed) {}

	@Override
	public double[] itemSimilarities(long itemID1, long[] itemID2s)
			throws TasteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long[] allSimilarItemIDs(long itemID) throws TasteException {
		// TODO Auto-generated method stub
		return null;
	}
	
}
