package ca.ubc.cs.commandrecommender.model.cf;

import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of {@link ca.ubc.cs.commandrecommender.model.cf.UserFactory}
 * for CF algorithms
 */
public class ProgrammerFactory implements UserFactory{
	
	private Set<Long> programmersSet = new HashSet<Long>();

    /**
     * add {@code userId} to {@code programmersSet} if {@code userId} is not in the
     * {@code programmersSet}. Returns {@code userId} for convenience
     * @param userId
     * @return userId
     */
	public Long getOrCreateProgrammerForName(Long userId ) {
		if (!programmersSet.contains(userId)) {
			programmersSet.add(userId);
		}
		return userId;
	}

    @Override
	public Long userForUserId(Long id) {
		if (programmersSet.contains(id)) {
			return id;
		}
		return (long) -1;
	}

    @Override
	public long[] users() {
		Object[] a = programmersSet.toArray();
		//TODO: better way?
		long[] programmers= new long[a.length];
		for (int i = 0; i < a.length; i++) {
			programmers[i]= (Long)a[i];
		}
		return programmers;
	}

}