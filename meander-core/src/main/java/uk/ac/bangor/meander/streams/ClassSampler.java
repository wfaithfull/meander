package uk.ac.bangor.meander.streams;

/**
 * @author Will Faithfull
 */
public interface ClassSampler {

    Double[] sample(int label);
    int getClasses();
    DataSource toDataSource(int label);

}
