package uk.ac.bangor.meander.streams;

import uk.ac.bangor.meander.MeanderException;

/**
 * @author Will Faithfull
 */
abstract class AbstractClassSampler implements ClassSampler {

    public ExampleProviderFactory toFactory(int label) {
        if(label > getClasses())
            throw new MeanderException("Class \"" + label + "\" was not found in this sampler.");

        return () -> context -> new Example(AbstractClassSampler.this.sample(label), context);
    }

}
