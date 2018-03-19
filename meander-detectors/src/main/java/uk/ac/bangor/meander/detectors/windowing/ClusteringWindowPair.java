package uk.ac.bangor.meander.detectors.windowing;

import lombok.Getter;
import uk.ac.bangor.meander.detectors.Pipe;
import uk.ac.bangor.meander.detectors.clusterers.Clustering;
import uk.ac.bangor.meander.streams.StreamContext;

/**
 * @author Will Faithfull
 */
@Getter
public class ClusteringWindowPair extends WindowPair<Double[]> {
    Clustering p;
    Clustering q;

    public ClusteringWindowPair(Window<Double[]> window1, Window<Double[]> window2, Clustering p, Clustering q) {
        super(window1, window2);
        this.p = p;
        this.q = q;
    }

    public static class Distribution implements Pipe<ClusteringWindowPair, DistributionPair> {
        @Override
        public DistributionPair execute(ClusteringWindowPair value, StreamContext context) {
            return new DistributionPair(value.getP().getDistribution(), value.getQ().getDistribution());
        }
    }
}
