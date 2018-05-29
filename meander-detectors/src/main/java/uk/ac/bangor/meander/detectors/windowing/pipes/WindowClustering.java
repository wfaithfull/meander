package uk.ac.bangor.meander.detectors.windowing.pipes;

import lombok.Getter;
import uk.ac.bangor.meander.detectors.CollectionUtils;
import uk.ac.bangor.meander.detectors.Pipe;
import uk.ac.bangor.meander.detectors.clusterers.Clustering;
import uk.ac.bangor.meander.detectors.clusterers.StreamClusterer;
import uk.ac.bangor.meander.detectors.windowing.support.FixedWindow;
import uk.ac.bangor.meander.streams.StreamContext;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author Will Faithfull
 */
public class WindowClustering extends FixedWindow<Double[]> implements Pipe<Double[], Clustering> {

    @Getter
    private final StreamClusterer clusterer;
    private       Queue<Integer>  assignments;

    public WindowClustering(int size, StreamClusterer clusterer) {
        super(size, Double[].class);
        this.clusterer = clusterer;
        this.assignments = new LinkedList<>();
    }

    @Override
    public void update(Double[] observation) {
        int cluster = clusterer.update(CollectionUtils.unbox(observation));
        assignments.add(cluster);

        if(isAtFullCapacity()) {
            clusterer.drop(assignments.remove(), CollectionUtils.unbox(getOldest()));
        }

        super.update(observation);
    }

    @Override
    public Clustering execute(Double[] value, StreamContext context) {
        update(value);
        return clusterer;
    }

    @Override
    public boolean ready() {
        return isAtFullCapacity();
    }

    public static class Distribution implements Pipe<Clustering, Double[]> {
        @Override
        public Double[] execute(Clustering value, StreamContext context) {
            return CollectionUtils.box(value.getDistribution());
        }
    }
}
