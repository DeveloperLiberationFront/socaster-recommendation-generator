package ca.ubc.cs.commandrecommender.generator;

import ca.ubc.cs.commandrecommender.model.IndexMap;
import ca.ubc.cs.commandrecommender.model.ToolUseCollection;
import ca.ubc.cs.commandrecommender.model.User;

/**
 * Interface for recommendation generation algorithms
 *
 * Created by KeEr on 2014-06-19.
 */
public interface IRecGen {

    /**
     * Get the purpose of the algorithm used
     *
     * @return the rationale for the recommendations
     */
    String getAlgorithmUsed();

    /**
     * Supplies usage data of a single user to the algorithm
     *
     * Implementing classes should use this method to gradually
     * build a data model for generating recommendations
     *
     * This method should always be called before {@link #trainWith(ca.ubc.cs.commandrecommender.model.ToolUseCollection)}
     * and {@link #getRecommendationsForUser(ca.ubc.cs.commandrecommender.model.User, int)}
     *
     * @param uses
     */
    void trainWith(ToolUseCollection uses);

    /**
     * Do any necessary operations with the data before recommendations can
     * be generated by the algorithm
     *
     * This method should always be called after {@link #trainWith(ca.ubc.cs.commandrecommender.model.ToolUseCollection)}
     * and before {@link #getRecommendationsForUser(ca.ubc.cs.commandrecommender.model.User, int)}
     */
    void runAlgorithm();


    /**
     * Generate recommendations for user
     *
     * This method should always be called after {@link #trainWith(ca.ubc.cs.commandrecommender.model.ToolUseCollection)}
     * and {@link #runAlgorithm()}
     *
     * @param user    the user we want to get recommendations for
     * @param amount  the number of recommendation we want to generate
     */
    Iterable<Integer> getRecommendationsForUser(User user, int amount, IndexMap userIndexMap);

}