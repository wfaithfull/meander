package uk.ac.bangor.meander.detectors.windowing;

import lombok.Getter;
import uk.ac.bangor.meander.detectors.Pipe;
import uk.ac.bangor.meander.detectors.clusterers.StreamClusterer;
import uk.ac.bangor.meander.streams.StreamContext;

import java.util.function.Supplier;

/**
 * @author Will Faithfull
 */
public class ClusteringWindowPairPipe implements Pipe<Double[], ClusteringWindowPair> {

    private final int                       size;
    private final Supplier<StreamClusterer> clustererSupplier;
    double[] p;
    double[] q;
    private WindowPair<Double[]> windowPair;
    @Getter
    private ClusteringWindow     tail, head;

    public ClusteringWindowPairPipe(int size, Supplier<StreamClusterer> clustererSupplier) {
        this.size = size;
        this.clustererSupplier = clustererSupplier;
        reset();
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
    public boolean needReset() {
        return true;
    }

    @Override
    public void reset() {
        tail = new ClusteringWindow(size, clustererSupplier.get());
        head = new ClusteringWindow(size, clustererSupplier.get());

        windowPair = new WindowPair<>(tail, head);
    }

    @Override
    public ClusteringWindowPair execute(Double[] value, StreamContext context) {
        update(value);
        return new ClusteringWindowPair(tail, head, tail.getClusterer(), head.getClusterer());
    }

    @Override
    public boolean ready() {
        return tail.isAtFullCapacity();
    }
}
