package uk.ac.bangor.meander.detectors.windowing;

import lombok.Getter;
import uk.ac.bangor.meander.detectors.CollectionUtils;
import uk.ac.bangor.meander.detectors.Pipe;
import uk.ac.bangor.meander.detectors.clusterers.StreamClusterer;
import uk.ac.bangor.meander.streams.StreamContext;

import java.util.function.Supplier;

/**
 * @author Will Faithfull
 */
public class WindowPairClusteringQuantizer implements Pipe<Double[], DistributionPair> {

    private WindowPair<double[]>     windowPair;
    private @Getter ClusteringWindow tail, head;

    double[] p;
    double[] q;

    public WindowPairClusteringQuantizer(int size, Supplier<StreamClusterer> clustererSupplier) {
        tail = new ClusteringWindow(size, clustererSupplier.get());
        head = new ClusteringWindow(size, clustererSupplier.get());

        windowPair = new WindowPair<>(tail, head);
    }

    public void update(Double[] input) {
        windowPair.update(CollectionUtils.unbox(input));

        p = tail.getClusterer().getDistribution();
        q = head.getClusterer().getDistribution();
    }

    public double[] getP() {
        return p;
    }

    public double[] getQ() {
        return q;
    }

    @Override
    public DistributionPair execute(Double[] value, StreamContext context) {
        update(value);
        return new DistributionPair(getP(), getQ());
    }

    @Override
    public boolean ready() {
        return tail.isAtFullCapacity();
    }
}
