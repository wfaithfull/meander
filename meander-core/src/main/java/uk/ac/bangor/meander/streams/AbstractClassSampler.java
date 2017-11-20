package uk.ac.bangor.meander.streams;

/**
 * @author Will Faithfull
 */
public abstract class AbstractClassSampler implements ClassSampler {

    public DataSource toDataSource(int label) {
        if(label > getClasses())
            throw new IllegalArgumentException("Class \"" + label + "\" was not found in this sampler.");

        return () -> () -> AbstractClassSampler.this.sample(label);
    }

}
