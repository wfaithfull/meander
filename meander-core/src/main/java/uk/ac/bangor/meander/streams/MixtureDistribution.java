package uk.ac.bangor.meander.streams;

import lombok.NonNull;
import lombok.extern.java.Log;
import uk.ac.bangor.meander.MeanderException;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

/**
 * @author Will Faithfull
 *
 * Represents a mixture distribution at a given instant in the data stream, i.e.
 * the probability that the next instance in the stream is drawn from class ω is
 * defined by the probability in the pair.
 *
 * This is intended to be a stable, immutable concept over a time-span of the
 * data stream. Changes over time in data stream concepts could  be transitions
 * at these data sources.
 */
@Log
class MixtureDistribution implements ExampleProviderFactory {

    private final Random RNG = new Random(System.currentTimeMillis());
    private final double EPS = 0.0000001;
    private final List<ExampleProviderFactory> exampleProviderFactories;
    private final MixingFunction               mixingFunction;
    private final StreamConcept                concept;

    /**
     * Create a mixture distribution of sources for a given instant of the specified
     * data sources and probabilities.
     * @param exampleProviderFactories List of data sources and their respective probabilities.
     */
    private MixtureDistribution(@NonNull final List<ExampleProviderFactory> exampleProviderFactories,
                                @NonNull final MixingFunction mixingFunction,
                                @NonNull final StreamConcept concept,
                                @NonNull final StreamContext context) {
        this.mixingFunction = mixingFunction;
        this.concept = concept;
        if(exampleProviderFactories.isEmpty()) {
            throw new MeanderException(getClass().getSimpleName() + " requires at least one source!");
        }

        double[] distribution = mixingFunction.getDistribution(context);

        if(distribution.length != exampleProviderFactories.size()) {
            throw new MeanderException("Distribution must be over the data sources (must be same number of elements)");
        }

        double sum = 0;
        for(double probability : distribution) {
            sum += probability;
        }

        double difference = Math.abs(1 - sum);
        if(difference > EPS) {
            throw new MeanderException(getClass().getSimpleName() + " probabilities must sum to 1 (" + sum + ")");
        }

        if(exampleProviderFactories.size() == 1) {
            log.warning("Only 1 source supplied (p = 1.0) - why bother using a mixture distribution?");
        }

        this.exampleProviderFactories = Collections.unmodifiableList(exampleProviderFactories);
    }

    /**
     * Sample a data point from the list of sources with the class probability defined by
     * the probabilities in the list of sources.
     * @return A sample data point.
     */
    private Double[] sample(StreamContext context) {
        double choice = RNG.nextDouble();
        Function<StreamContext, Example> source = null;

        double[] distribution = mixingFunction.getDistribution(context);

        double cumulative = 0.0;
        int label = 0;
        for(int i=0; i < distribution.length; i++) {
            double probability = distribution[i];
            cumulative += probability;
            if(choice <= cumulative) {
                label = i;
                source = exampleProviderFactories.get(label).getProvider();
                break;
            }
        }

        if(this.concept == StreamConcept.CLASS) {
            context.setLabel(label);
        } else {
            context.setSequence(label);
            context.setSourcePriors(distribution);

            for(ExampleProviderFactory exampleProviderFactory : exampleProviderFactories) {
                double[] classPriors = ((MixtureDistribution) exampleProviderFactory).mixingFunction.getDistribution(context);
                context.setClassPriors(classPriors);
            }
        }

        assert source != null;
        return source.apply(context).getData();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExampleProvider getProvider() {
        return context -> new Example(sample(context), context);
    }

    static ExampleProviderFactory ofClasses(@NonNull final List<ExampleProviderFactory> exampleProviderFactories,
                                            @NonNull final MixingFunction mixingFunction,
                                            @NonNull final StreamContext context) {
        return new MixtureDistribution(exampleProviderFactories, mixingFunction, StreamConcept.CLASS, context);
    }

    static ExampleProviderFactory ofSources(@NonNull final List<ExampleProviderFactory> exampleProviderFactories,
                                            @NonNull final MixingFunction mixingFunction,
                                            @NonNull final StreamContext context) {
        return new MixtureDistribution(exampleProviderFactories, mixingFunction, StreamConcept.SOURCE, context);
    }
}
