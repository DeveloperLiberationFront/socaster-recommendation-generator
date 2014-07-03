package ca.ubc.cs.commandrecommender.generator;

import ca.ubc.cs.commandrecommender.model.RecommendationCollector;
import ca.ubc.cs.commandrecommender.model.cf.matejka.MatejkaOptions;
import ca.ubc.cs.commandrecommender.model.cf.matejka.MatejkaSimilarity;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.model.DataModel;
import org.junit.Test;

import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

//TODO: debug
public class UserBasedCFRecGenTest extends AbstractCFRecGenTest {

    private static final double DELTA = 0.00000001;

	@Test
	public void testBasicUser(){
		rec.trainWith(person(1,1,2,3));
		rec.trainWith(person(2,1,2));
		rec.trainWith(addDummyPerson());
		
		rec.runAlgorithm();
		
		RecommendationCollector rc = new RecommendationCollector(
                2, null, new HashSet<Integer>(), 1);
		rec.fillRecommendations(rc);
		assertEquals(new Integer(3),rc.iterator().next());
	}

    @Test
	public void testSomeSimilarity() throws TasteException{
		rec.trainWith(person(1,1,2));
		rec.trainWith(person(2,1,3));
		rec.trainWith(addDummyPerson());
		
		rec.runAlgorithm();
		
		DataModel model = getModel();
		MatejkaSimilarity sim = sim(model);
		
//		User p1 = model.getUser(1);
//		User p2 = model.getUser(2);
//
        assertTrue(sim.userSimilarity(1,1)==1.0);
		assertTrue(sim.userSimilarity(1, 2)<1.0);
		assertTrue(sim.userSimilarity(1, 2)>0.0);
	}

	private MatejkaSimilarity sim(DataModel model) throws TasteException {
		return new MatejkaSimilarity(model, new MatejkaOptions(false, true, -1));
	}

    @Test
	public void testMoreSimilar() throws TasteException{
		
		
		rec.trainWith(person(1,1,2,4));
		rec.trainWith(person(2,1,3,5));
		rec.trainWith(person(3,1,3,6));
		
		rec.runAlgorithm();
		
		DataModel model = getModel();
		MatejkaSimilarity sim = sim(model);
		
		assertTrue(sim.userSimilarity(1, 2)<sim.userSimilarity(2, 3));
	}

    @Test
	public void testEqualSimilarity() throws TasteException{
		rec.trainWith(person(1,1,2));		
		rec.trainWith(person(2,2,3));
		rec.trainWith(person(3,3,4));
		
		rec.runAlgorithm();				
		
		DataModel model = getModel();
		MatejkaSimilarity sim = sim(model);

//		User p1 = model.getUser(1);
//		User p2 = model.getUser(2);
//		User p3 = model.getUser(3);

		//sim 	= cos(a,b)
		//		= a.b / |a|*|b|
		//		= 2*0.2027325540540822 / |a|*|b|
		//		= 2*0.2027325540540822 / sqrt(0.5493061443340549^2+0.2027325540540822^2)*|b|
		//		= 2*0.2027325540540822 / 0.585523465521611*sqrt(0.2027325540540822^2+0.2027325540540822^2)
		//		= 2*0.2027325540540822 / 0.585523465521611*0.28670712747782
		//		= 0.198539842181415
		System.out.println(sim.userSimilarity(1, 2));
        System.out.println(sim.userSimilarity(2, 1));
        System.out.println(sim.userSimilarity(2, 2));
        System.out.println(sim.userSimilarity(1, 3));
        System.out.println(sim.userSimilarity(3, 1));
        System.out.println(sim.userSimilarity(2, 3));
        System.out.println(sim.userSimilarity(3, 2));
		assertEquals(sim.userSimilarity(1, 2),sim.userSimilarity(2, 3),DELTA);
//		assertEquals(sim.userSimilarity(2, 1),sim.userSimilarity(2, 3),DELTA); //Expected:0.3462415386331469; Actual:0.2448297398959621
//		assertEquals(sim.userSimilarity(1, 2),sim.userSimilarity(3, 2),DELTA); //Expected:0.2448297398959621; Actual:0.7071067811865475
//		assertEquals(sim.userSimilarity(2, 1),sim.userSimilarity(3, 2),DELTA); //Expected:0.3462415386331469; Actual:0.7071067811865475
	}
	
	private DataModel getModel() {
		return ((AbstractCFRecGen) rec).getModel();
	}

	@Override
    protected AbstractCFRecGen getRec() {
		return new UserBasedCFRecGen("",32,new MatejkaOptions(false, true, 1.0));
	}
}
