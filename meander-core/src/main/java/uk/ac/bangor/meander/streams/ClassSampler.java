package uk.ac.bangor.meander.streams;

/**
 * @author Will Faithfull
 */
interface ClassSampler {

    Double[] sample(int label);
    int getClasses();
    double[] getDistribution();
    DataSource toDataSource(int label);

}
