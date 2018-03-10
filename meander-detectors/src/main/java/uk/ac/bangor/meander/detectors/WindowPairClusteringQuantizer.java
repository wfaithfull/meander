package uk.ac.bangor.meander.detectors;

import lombok.Getter;
import uk.ac.bangor.meander.detectors.clusterers.StreamClusterer;
import uk.ac.bangor.meander.detectors.windowing.ClusteringWindow;
import uk.ac.bangor.meander.detectors.windowing.WindowPair;

import java.util.function.Supplier;

/**
 * @author Will Faithfull
 */
public class WindowPairClusteringQuantizer {

    private WindowPair<double[]> windowPair;
    private @Getter ClusteringWindow     w1, w2;

    double[] p;
    double[] q;

    public WindowPairClusteringQuantizer(int size, Supplier<StreamClusterer> clustererSupplier) {
        w1 = new ClusteringWindow(size, clustererSupplier.get());
        w2 = new ClusteringWindow(size, clustererSupplier.get());

        windowPair = new WindowPair<>(w1, w2);
    }

    public void update(Double[] input) {
        windowPair.update(CollectionUtils.unbox(input));

        p = w1.getClusterer().getDistribution();
        q = w2.getClusterer().getDistribution();
    }

    public double[] getP() {
        return p;
    }

    public double[] getQ() {
        return q;
    }

}
