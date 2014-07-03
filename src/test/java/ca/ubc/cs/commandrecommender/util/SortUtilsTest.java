package ca.ubc.cs.commandrecommender.util;

import junit.framework.TestCase;
import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.bag.HashBag;
import org.junit.Test;

public class SortUtilsTest extends TestCase{

    @Test
	public void testSortBag(){
		Bag<Integer> b = new HashBag<Integer>();
		b.add(1);
		b.add(2);
		b.add(2);
		b.add(3);
		b.add(3);
		b.add(3);
		
		assertAbout(b);
		
		Bag<Integer> bCopy = new HashBag<Integer>();
		bCopy.addAll(b);
		
		assertAbout(bCopy);
		assertEquals(b,bCopy);
		
		Bag<Integer> bSorted = SortingUtils.sortBagByCount(b);
		
		assertAbout(bSorted);
		assertEquals(b,bSorted);
		
	}

	private void assertAbout(Bag<Integer> b) {
		assertEquals(6,b.size());
		assertEquals(3,b.uniqueSet().size());
	}
}
