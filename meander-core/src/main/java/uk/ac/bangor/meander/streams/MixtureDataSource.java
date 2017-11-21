package uk.ac.bangor.meander.streams;

import lombok.NonNull;
import lombok.extern.java.Log;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

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
class MixtureDataSource implements DataSource {

    private final Random RNG = new Random(System.currentTimeMillis());
    private final double EPS = 0.0000001;
    private final List<DataSource> dataSources;
    private final MixtureProvider mixtureProvider;
    private final StreamConcept concept;

    /**
     * Create a mixture distribution of sources for a given instant of the specified
     * data sources and probabilities.
     * @param dataSources List of data sources and their respective probabilities.
     */
    private MixtureDataSource(@NonNull final List<DataSource> dataSources,
                              @NonNull final MixtureProvider mixtureProvider,
                              @NonNull final StreamConcept concept) {
        this.mixtureProvider = mixtureProvider;
        this.concept = concept;
        if(dataSources.isEmpty()) {
            throw new IllegalArgumentException("InstantMixtureDataSource requires at least one source!");
        }

        double[] distribution = mixtureProvider.getDistribution(StreamContext.START);

        if(distribution.length != dataSources.size()) {
            throw new IllegalArgumentException("Distribution must be over the data sources (must be same number of elements)");
        }

        double sum = 0;
        for(double probability : distribution) {
            sum += probability;
        }

        double difference = Math.abs(1 - sum);
        if(difference > EPS) {
            throw new IllegalArgumentException("InstantMixtureDataSource probabilities must sum to 1 (" + sum + ")");
        }

        if(dataSources.size() == 1) {
            log.warning("Only 1 source supplied (p = 1.0) - why bother using a mixture distribution?");
        }

        this.dataSources = Collections.unmodifiableList(dataSources);
    }

    /**
     * Sample a data point from the list of sources with the class probability defined by
     * the probabilities in the list of sources.
     * @return A sample data point.
     */
    private Double[] sample(StreamContext context) {
        double choice = RNG.nextDouble();
        Function<StreamContext, Example> source = null;

        double[] distribution = mixtureProvider.getDistribution(StreamContext.START);

        double cumulative = 0.0;
        int label = 0;
        for(int i=0; i < distribution.length; i++) {
            double probability = distribution[i];
            cumulative += probability;
            if(choice <= cumulative) {
                label = i;
                source = dataSources.get(label).getSource();
                break;
            }
        }

        if(this.concept == StreamConcept.CLASS) {
            context.setLabel(label);
        } else {
            context.setSequence(label);
        }

        assert source != null;
        return source.apply(context).getData();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Function<StreamContext, Example> getSource() {
        return context -> new Example(sample(context), context);
    }

    static DataSource ofClasses(@NonNull final List<DataSource> dataSources,
                                @NonNull final MixtureProvider mixtureProvider) {
        return new MixtureDataSource(dataSources, mixtureProvider, StreamConcept.CLASS);
    }

    static DataSource ofSequences(@NonNull final List<DataSource> dataSources,
                                  @NonNull final MixtureProvider mixtureProvider) {
        return new MixtureDataSource(dataSources, mixtureProvider, StreamConcept.SEQUENCE);
    }
}
