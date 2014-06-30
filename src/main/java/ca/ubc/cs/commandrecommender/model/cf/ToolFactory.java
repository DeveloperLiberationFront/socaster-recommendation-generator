package ca.ubc.cs.commandrecommender.model.cf;

import java.util.HashSet;
import java.util.Set;

//TODO: check over
class ToolFactory implements ItemFactory {

	private Set<Long> toolset = new HashSet<Long>();

    /**
     * add {@code id} to {@code toolset} if {@code id} is not in the
     * {@code toolset}. Returns {@code id} for convenience
     * @param id
     * @return
     */
	public long getOrCreateToolForName(long id) {
		if (!toolset.contains(id)) {
			toolset.add(id);
		}
		return id;
	}

    @Override
	public Long toolForToolID(Long itemID) {
		if (toolset.contains(itemID)) {
			return itemID;
		}
		return (long) -1;
	}

    @Override
	public long[] tools() {
		Object[] a = toolset.toArray();
		long[] tools= new long[a.length];
		for (int i = 0; i < a.length; i++) {
			tools[i]= (Long) a[i];
		}

		return tools;
	}

}