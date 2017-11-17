package uk.ac.bangor.meander.streams;

/**
 * @author Will Faithfull
 */
public interface ClassSampler {

    Double[] sample(int source);
    int[] getClasses();
    ProbabilisticDataSource toDataSource(double probability, int label);

}
