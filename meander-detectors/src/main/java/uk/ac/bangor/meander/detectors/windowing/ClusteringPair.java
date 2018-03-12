package uk.ac.bangor.meander.detectors.windowing;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.ac.bangor.meander.detectors.Pipe;
import uk.ac.bangor.meander.detectors.clusterers.Clustering;
import uk.ac.bangor.meander.streams.StreamContext;

/**
 * @author Will Faithfull
 */
@AllArgsConstructor @Getter
public class ClusteringPair {
    Clustering p, q;

    public static class Distribution implements Pipe<ClusteringPair, DistributionPair> {
        @Override
        public DistributionPair execute(ClusteringPair value, StreamContext context) {
            return new DistributionPair(value.getP().getDistribution(), value.getQ().getDistribution());
        }
    }
}
