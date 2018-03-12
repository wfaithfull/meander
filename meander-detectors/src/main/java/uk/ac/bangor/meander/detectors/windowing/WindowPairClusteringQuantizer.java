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
public class WindowPairClusteringQuantizer implements Pipe<Double[], ClusteringPair> {

    private WindowPair<Double[]>     windowPair;
    private @Getter ClusteringWindow tail, head;

    double[] p;
    double[] q;

    public WindowPairClusteringQuantizer(int size, Supplier<StreamClusterer> clustererSupplier) {
        tail = new ClusteringWindow(size, clustererSupplier.get());
        head = new ClusteringWindow(size, clustererSupplier.get());

        windowPair = new WindowPair<>(tail, head);
    }

    public void update(Double[] input) {
        windowPair.update(input);

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
    public ClusteringPair execute(Double[] value, StreamContext context) {
        update(value);
        return new ClusteringPair(tail, head, tail.getClusterer(), head.getClusterer());
    }

    @Override
    public boolean ready() {
        return tail.isAtFullCapacity();
    }
}
