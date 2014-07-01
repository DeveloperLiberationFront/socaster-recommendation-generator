package ca.ubc.cs.commandrecommender.model.cf;

/**
 * Keeps track of valid items for CF recommendation and what the long id
 * corresponds to
 */
public interface ItemFactory{

    /**
     * Check whether an itemID is valid
     * @param itemID
     * @return @{code itemID} if @{code itemID} is valid, else return -1
     */
	Long toolForToolID(Long itemID);

    /**
     * Get the array of tools available.
     * Here tools means the id of the item we want to recommend.
     * the item could be a pair (learning sequence) or a command.
     * @return
     */
	long[] tools();
	
}