package ca.ubc.cs.commandrecommender.model.cf;

import junit.framework.TestCase;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Iterator;

public class UsageModelTest extends TestCase{

	private static final Long T1 = (long) 1;
	private static final Long T2 = (long) 2;
	private static final Long T3 = (long) 3;
	private static final Long[] TOOL_NAMES = new Long[] {T1,T2,T3};
	
	private static final Long A = (long) 1;
	private static final Long B = (long) 2;
	private static final Long C = (long) 3;
	private static final Long[] USER_NAMES = new Long[] {A,B,C};

	private UsageModel model;
	
	public void setUp(){ 
		BasicUsageModel model = new BasicUsageModel();
		
		model.makeUseOf(10, A, T1);
		model.makeUseOf(20, A, T2);
		model.makeUseOf(30, A, T3);
		
		model.makeUseOf(15, B, T1);
		model.makeUseOf(25, B, T2);

		model.makeUseOf(13, C, T3);
		model.makeUseOf( 3, C, T2);		
		
		model.done();
		
		this.model = model;
	}

    @Test
	public void testGetItem(){
		
		LongPrimitiveIterator itemIDs = model.getItemIDs();
		
		for (Iterator iterator = itemIDs; iterator.hasNext();) {
			Long itemid = (Long) iterator.next();
//			assertEquals(itemid.longValue(), (long)1);
			System.out.println(itemid);
		}
		
//		for(Long toolName : TOOL_NAMES){
//			assertEquals(model.getItem(toolName),model.getItem(toolName));
//			assertEquals(toolName,model.getItem(toolName).getID());
//		}
//		assertNull(model.getItem(10));		
	}

    @Test
	public void testGetItems(){
		
		for(Long toolName : TOOL_NAMES){
			findToolName(toolName);
		}
	}

	private void findToolName(Long toolName) {
		boolean found = false;
		LongPrimitiveIterator itemIDs = model.getItemIDs();
		
		for (Iterator iterator = itemIDs; iterator.hasNext();) {
			Long itemid = (Long) iterator.next();
			found |= itemid.equals(toolName);
		}
		
		assertTrue(found);
	}

    @Test
	public void testGetNumItems(){
		assertEquals(TOOL_NAMES.length, model.getNumItems());
	}

    @Test
	public void testGetUser(){
		
		//TODO
//		for(Long userName : USER_NAMES){
//			assertEquals(model.getUser(userName), model.getUser(userName));
//			assertEquals(userName, model.getUser(userName).getID());
//		}
	}

    @Test  //TODO: understand why and fix it
    @Ignore
	public void testGetUsers(){
		for(Long userName : USER_NAMES){
			//findUserName(userName);
		}
	}

    //TODO: understand why and fix its
	private void findUserName(Long userName) {
		boolean found = false;
//		for(Programmer u : model.getUsers()){
//			found |= u.getID().equals(userName);
//		}
		assertTrue(found);
	}

    @Test
	public void testGetNumUsers(){
		assertEquals(USER_NAMES.length, model.getNumUsers());
	}

    @Test
	public void testGetPreferencesForItem(){
		for(Long toolName : TOOL_NAMES){
//			for(ToolUse use : model.getPreferencesForItem(toolName)){
//				findUserName(use.getUser().getID().toString());
//			}
		}
	}

    @Test
	public void testOrderingOfArrays(){
//		Collection<Programmer> users = model.getUsers();
//		for(Programmer p : users){
//			Preference[] prefs = p.getPreferencesAsArray();
//			Preference[] copy = Arrays.copyOf(prefs,prefs.length);
//			Arrays.sort(copy,new Comparator<Preference>() {
//
//				@Override
//				public int compare(Preference o1, Preference o2) {
//					return o1.getItem().compareTo(o2.getItem());
//				}
//			});
//			for(int i = 0; i<prefs.length; i++){
//				assertEquals(copy[i],prefs[i]);
//			}
//		}
	}
}
