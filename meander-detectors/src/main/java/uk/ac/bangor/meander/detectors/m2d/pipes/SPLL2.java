package uk.ac.bangor.meander.detectors.m2d.pipes;

import uk.ac.bangor.meander.detectors.Pipe;
import uk.ac.bangor.meander.detectors.Threshold;
import uk.ac.bangor.meander.detectors.clusterers.KMeansStreamClusterer;
import uk.ac.bangor.meander.detectors.stats.cdf.pipes.ChiSquared;
import uk.ac.bangor.meander.detectors.windowing.pipes.WindowPairClustering;

/**
 * @author Will Faithfull
 */
public class SPLL2 {

    public static Pipe<Double[], Boolean> detector(int size, int K) {
        return chiSq(size,K)
                .then(Threshold.lessThan(0.05));
    }

    public static Pipe<Double[], Double> st(int size, int K) {
        return new WindowPairClustering(size, () -> new KMeansStreamClusterer(K))
                .then(new SPLL());
    }

    public static Pipe<Double[], Double> chiSq(int size, int K) {
        return new WindowPairClustering(size, () -> new KMeansStreamClusterer(K))
                .then(new SPLL())
                .then(new ChiSquared());
    }
}
