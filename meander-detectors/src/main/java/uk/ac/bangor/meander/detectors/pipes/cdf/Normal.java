package uk.ac.bangor.meander.detectors.pipes.cdf;

import org.apache.commons.math3.distribution.NormalDistribution;
import uk.ac.bangor.meander.detectors.Pipe;
import uk.ac.bangor.meander.detectors.stats.IncrementalStatistics;
import uk.ac.bangor.meander.streams.StreamContext;

/**
 * @author Will Faithfull
 */
public class Normal extends AbstractCDFPipe<Double> implements Pipe<Double, Double> {

    NormalDistribution cdf;
    IncrementalStatistics statistics = new IncrementalStatistics();

    @Override
    public Double execute(Double value, StreamContext context) {
        statistics.update(value);
        cdf = new NormalDistribution(statistics.mean(), statistics.var());

        double cumulativeProbability = cdf.cumulativeProbability(value);

        return cumulativeProbability;
    }
}
