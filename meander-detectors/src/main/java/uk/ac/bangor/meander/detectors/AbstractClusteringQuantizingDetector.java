package uk.ac.bangor.meander.detectors;

import uk.ac.bangor.meander.detectors.clusterers.StreamClusterer;
import uk.ac.bangor.meander.detectors.windowing.ClusteringWindow;
import uk.ac.bangor.meander.detectors.windowing.WindowPair;

import java.util.function.Supplier;

/**
 * @author Will Faithfull
 */
public abstract class AbstractClusteringQuantizingDetector extends AbstractUnboxingDetector {

    private WindowPair<double[]> windowPair;
    protected ClusteringWindow w1, w2;

    double[] p;
    double[] q;

    public AbstractClusteringQuantizingDetector(int size, Supplier<StreamClusterer> clustererSupplier) {
        w1 = new ClusteringWindow(size, clustererSupplier.get());
        w2 = new ClusteringWindow(size, clustererSupplier.get());

        windowPair = new WindowPair<>(w1, w2);
    }

    public void update(double[] input) {
        windowPair.update(input);

        p = w1.getClusterer().getDistribution();
        q = w2.getClusterer().getDistribution();
    }

    protected double[] getP() {
        return p;
    }

    protected double[] getQ() {
        return q;
    }

}
