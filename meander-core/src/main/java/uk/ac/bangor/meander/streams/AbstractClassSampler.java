package uk.ac.bangor.meander.streams;

import uk.ac.bangor.meander.MeanderException;

/**
 * @author Will Faithfull
 *
 * Base class for samplers holding boilerplate methods.
 */
abstract class AbstractClassSampler implements ClassSampler {

    /**
     * {@inheritDoc}
     */
    public ExampleProviderFactory toFactory(int label) {
        if(label > getClasses())
            throw new MeanderException("Class \"" + label + "\" was not found in this sampler.");

        return () -> context -> new Example(AbstractClassSampler.this.sample(label), context);
    }

}
