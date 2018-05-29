package uk.ac.bangor.meander.detectors.stats.cdf;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import uk.ac.bangor.meander.detectors.Pipe;
import uk.ac.bangor.meander.streams.StreamContext;

import java.util.Optional;

/**
 * @author Will Faithfull
 */
public class ChiSquared extends AbstractCDFPipe<Double> implements Pipe<Double, Double> {

    ChiSquaredDistribution cdf;
    private Optional<Double> df = Optional.empty();

    public double chiSq(Double statistic, double df) {
        if (cdf == null) {
            cdf = new ChiSquaredDistribution(df);
        }

        double cumulativeProbability = cdf.cumulativeProbability(statistic);

        return cumulativeProbability;
    }

    public ChiSquared() {
    }

    public ChiSquared(double df) {
        this.df = Optional.of(df);
    }

    public ChiSquared(int df) {
        this((double) df);
    }

    @Override
    public Double execute(Double value, StreamContext context) {
        return chiSq(value, df.orElse((double) context.getDimensionality()));
    }
}
