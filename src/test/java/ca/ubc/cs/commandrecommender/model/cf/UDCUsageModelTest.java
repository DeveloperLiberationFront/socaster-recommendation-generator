package ca.ubc.cs.commandrecommender.model.cf;

import junit.framework.TestCase;
import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.bag.HashBag;
import org.junit.Test;

public class UDCUsageModelTest extends TestCase {

	private UDCUsageModel model;

    private static final double DELTA = 0.0000001;


	double third = 1/(double)3;
	double half = 1/(double)2;
	
	public void setUp(){
		model = new UDCUsageModel();	
	}

    @Test
	public void test1(){
		
		int userId = 1;
		insertUse(userId,1,2,3,4);
		model.finish();//preferences get recomputed here		
		
		assertEquals(0.0,valueOfPreferenceFor(1,1),DELTA);
		assertEquals(0.0,valueOfPreferenceFor(1,2),DELTA);
		assertEquals(0.0,valueOfPreferenceFor(1,3),DELTA);
		assertEquals(0.0,valueOfPreferenceFor(1,4),DELTA);
	}

    @Test
	public void test2(){
		insertUse(1,1,2,3,4);		
		insertUse(2,1,2,3,4);
		model.finish();//preferences get recomputed here		
		
		assertEquals(0.0,valueOfPreferenceFor(1,1),DELTA);
		assertEquals(0.0,valueOfPreferenceFor(2,1),DELTA);
	}

    @Test
	public void test3(){
		insertUse(1,3,4);		
		insertUse(2,1,2);
		model.finish();//preferences get recomputed here		
		
		assertEquals(Math.log(2) * half,valueOfPreferenceFor(1,3),DELTA);
		assertEquals(Math.log(2) * half,valueOfPreferenceFor(2,1),DELTA);
	}

    @Test
	public void test4(){
		insertUse(1,1,2);		
		insertUse(2,2,3);
		insertUse(3,3,4);
		model.finish();//preferences get recomputed here		
		
		assertEquals(valueOfPreferenceFor(1,1),valueOfPreferenceFor(3,4),DELTA);
		assertEquals(valueOfPreferenceFor(1,2),valueOfPreferenceFor(2,2),DELTA);
		assertEquals(valueOfPreferenceFor(2,3),valueOfPreferenceFor(3,3),DELTA);
		assertEquals(valueOfPreferenceFor(1,2),valueOfPreferenceFor(3,3),DELTA);
		
		assertEquals(Math.log(3)*half,valueOfPreferenceFor(1,1),DELTA);
		assertEquals(Math.log(1.5)*half,valueOfPreferenceFor(2,2),DELTA);
	}

    @Test
	public void test5(){		
		insertUse(1,1);		
		insertUse(2,2,3);
		insertUse(3,4,5,6);
		model.finish();//preferences get recomputed here
		
		assertEquals(Math.log(3),valueOfPreferenceFor(1,1),DELTA);
		
		assertEquals(Math.log(3)*half,valueOfPreferenceFor(2,2),DELTA);
		assertEquals(Math.log(3)*half,valueOfPreferenceFor(2,3),DELTA);
		
		assertEquals(Math.log(3)*third,valueOfPreferenceFor(3,4),DELTA);
		assertEquals(Math.log(3)*third,valueOfPreferenceFor(3,5),DELTA);
		assertEquals(Math.log(3)*third,valueOfPreferenceFor(3,6),DELTA);
	}
	
	public void test6(){		
		insertUse(1,1);		
		insertUse(2,1,2);
		insertUse(3,1,2,3);
		model.finish();//preferences get recomputed here
		
		assertEquals(Math.log(1),valueOfPreferenceFor(1,1),DELTA);

		assertEquals(Math.log(1)  *half,valueOfPreferenceFor(2,1),DELTA);
		assertEquals(Math.log(1.5)*half,valueOfPreferenceFor(2,2),DELTA);
		
		assertEquals(Math.log(1)  *third,valueOfPreferenceFor(3,1),DELTA);
		assertEquals(Math.log(1.5)*third,valueOfPreferenceFor(3,2),DELTA);
		assertEquals(Math.log(3)  *third,valueOfPreferenceFor(3,3),DELTA);
	}

    @Test
	public void test7(){		
		insertUse(1,1,1);		
		insertUse(2,1,1,2,2,2);
		insertUse(3,1,1,2,2,2,3,3,3,3,3);
		model.finish();//preferences get recomputed here
		
		assertEquals(Math.log(1),valueOfPreferenceFor(1,1));		
		
		assertEquals(Math.log(1)  *0.4,valueOfPreferenceFor(2,1),DELTA);
		assertEquals(Math.log(1.5)*0.6,valueOfPreferenceFor(2,2),DELTA);
		
		assertEquals(Math.log(1)  *0.2,valueOfPreferenceFor(3,1),DELTA);
		assertEquals(Math.log(1.5)*0.3,valueOfPreferenceFor(3,2),DELTA);
		assertEquals(Math.log(3)  *0.5,valueOfPreferenceFor(3,3),DELTA);
	}

	private double valueOfPreferenceFor(int userId, int itemID) {
		ToolUsePreference[] prefs = model.getPreferenceForItem((long) itemID);
		for(ToolUsePreference pref : prefs){
			if(pref.getUserID() == userId){
				return pref.getValue();
			}
		}
		throw new RuntimeException("Unknown user");
	}

	private void insertUse(int userid, int... tools) {
		Bag<Integer> toolBag = new HashBag<Integer>();
		for(int t : tools){
			toolBag.add(t);
		}
		
		for(int t : toolBag.uniqueSet()){
			model.insertUse(toolBag.getCount(t), userid, t);	
		}
	}
	 
}
