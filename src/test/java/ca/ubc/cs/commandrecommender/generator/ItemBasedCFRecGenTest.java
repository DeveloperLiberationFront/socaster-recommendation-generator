package ca.ubc.cs.commandrecommender.generator;

import ca.ubc.cs.commandrecommender.model.RecommendationCollector;
import ca.ubc.cs.commandrecommender.model.cf.matejka.MatejkaOptions;
import org.junit.Test;

import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ItemBasedCFRecGenTest extends AbstractCFRecGenTest {

    @Test
	public void testBasic(){
		rec.trainWith(person(1,1,2,3));
		rec.trainWith(person(2,1,2));
		rec.trainWith(person(3,2,3));
		
		rec.runAlgorithm();
		
		RecommendationCollector rc = new RecommendationCollector(
                2, null, new HashSet<Integer>(), 1);
		rec.fillRecommendations(rc);
		assertTrue(rc.containsRec(3));
	}

    @Test
	public void testMoreLikeRec2(){
		rec.trainWith(person(1,1,2,3));
		rec.trainWith(person(2,1,4,5));
		rec.trainWith(person(3,1,6,7));
		rec.trainWith(person(4,1,6,8));
		rec.trainWith(addDummyPerson());
		
		rec.runAlgorithm();
		
		RecommendationCollector rc = new RecommendationCollector(
                2, null, new HashSet<Integer>(), 5);
		rec.fillRecommendations(rc);
		assertEquals(new Integer(6),rc.iterator().next());
		assertTrue(rc.containsRec(2));
		assertTrue(rc.containsRec(3));
		assertTrue(rc.containsRec(7));
		assertTrue(rc.containsRec(8));
	}
	
	@Override
    protected ItemBasedCFRecGen getRec() {
        return new ItemBasedCFRecGen("", new MatejkaOptions(false, true, 1.0));
    }
}
