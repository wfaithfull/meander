package uk.ac.bangor.meander.streams;

/**
 * @author Will Faithfull
 */
public interface MixingFunction {

    /**
     * Get the distribution from this mixing function
     * @param context The stream context.
     * @return A distribution.
     */
    double[] getDistribution(StreamContext context);

}
