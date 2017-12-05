package uk.ac.bangor.meander.streams;

/**
 * @author Will Faithfull
 */
abstract class AbstractClassSampler implements ClassSampler {

    public ExampleProviderFactory toDataSource(int label) {
        if(label > getClasses())
            throw new IllegalArgumentException("Class \"" + label + "\" was not found in this sampler.");

        return () -> context -> new Example(AbstractClassSampler.this.sample(label), context);
    }

}
