package uk.ac.bangor.meander.streams;

/**
 * @author Will Faithfull
 */
public abstract class AbstractClassSampler implements ClassSampler {

    public DataSource toDataSource(int label) {

        for(int i=0;i<=getClasses().length;i++) {
            if(i == getClasses().length)
                throw new IllegalArgumentException("Class \"" + label + "\" was not found in this sampler.");

            if(getClasses()[i] == label)
                break;
        }

        return () -> () -> AbstractClassSampler.this.sample(label);
    }

}
