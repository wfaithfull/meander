package uk.ac.bangor.meander.streams;

import lombok.NonNull;
import lombok.extern.java.Log;

import java.util.Collections;
import java.util.List;
import java.util.Random;
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
 * between these data sources.
 */
@Log
public class InstantMixtureDataSource implements DataSource {

    final Random RNG = new Random(System.currentTimeMillis());
    final double EPS = 0.0000001;
    final List<Pair<Double, DataSource>> dataSources;

    /**
     * Create a mixture distribution of sources for a given instant of the specified
     * data sources and probabilities.
     * @param dataSources List of data sources and their respective probabilities.
     */
    public InstantMixtureDataSource(@NonNull final List<Pair<Double, DataSource>> dataSources) {
        if(dataSources.isEmpty()) {
            throw new IllegalArgumentException("InstantMixtureDataSource requires at least one source!");
        }

        double sum = dataSources.stream().
                mapToDouble(ds -> ds.getElement0()).sum();

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
    public Double[] sample() {
        double choice = RNG.nextDouble();
        Supplier<Double[]> source = null;

        double cumulative = 0.0;
        for(Pair<Double, DataSource> entry : this.dataSources) {
            double probability = entry.getElement0();
            cumulative += probability;
            if(choice <= cumulative) {
                source = entry.getElement1().getSource();
                break;
            }
        }

        return source.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Supplier<Double[]> getSource() {
        return () -> sample();
    }
}
