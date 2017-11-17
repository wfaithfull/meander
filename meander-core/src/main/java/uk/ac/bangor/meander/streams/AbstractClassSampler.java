package uk.ac.bangor.meander.streams;

/**
 * @author Will Faithfull
 */
public abstract class AbstractClassSampler implements ClassSampler {

    public ProbabilisticDataSource toDataSource(double probability, int label) {

        for(int i=0;i<=getClasses().length;i++) {
            if(i == getClasses().length)
                throw new IllegalArgumentException("Class \"" + label + "\" was not found in this sampler.");

            if(getClasses()[i] == label)
                break;
        }

        return new ProbabilisticDataSource(probability, () -> this.sample(label));
    }

}
