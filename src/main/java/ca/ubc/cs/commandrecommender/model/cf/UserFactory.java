package ca.ubc.cs.commandrecommender.model.cf;

/**
 * Keeps track of valid users for CF recommendation
 */
public interface UserFactory{

    /**
     * Check whether an id is valid
     * @param id
     * @return @{code id} if @{code id} is valid, else return -1
     */
	Long userForUserId(Long id);

    /**
     * Get the array of users available.
     * @return
     */
	long[] users();

}