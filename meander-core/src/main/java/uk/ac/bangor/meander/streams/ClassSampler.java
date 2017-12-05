package uk.ac.bangor.meander.streams;

/**
 * @author Will Faithfull
 *
 * Interface for anything that samples from a dataset and subsequently provides examples of specific classes.
 */
interface ClassSampler {

    /**
     * Sample an instance of the class identified by {@code label}.
     * @param label The class label to retrieve a sample of.
     * @return A sample.
     */
    Double[] sample(int label);

    /**
     * The number of classes indexed by this component.
     * @return Number of classes > 0.
     */
    int getClasses();

    /**
     * Builds an {@link ExampleProviderFactory} from this sampler.
     * @param label The class label the factory will provide samples of.
     * @return The factory.
     */
    ExampleProviderFactory toFactory(int label);

    /**
     * Returns the posterior distribution of the classes, as they were sampled.
     * @return A distribution.
     */
    double[] getDistribution();

}
