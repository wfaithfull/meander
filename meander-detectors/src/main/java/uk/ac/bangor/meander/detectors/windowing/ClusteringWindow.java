package uk.ac.bangor.meander.detectors.windowing;

import lombok.Getter;
import uk.ac.bangor.meander.detectors.clusterers.StreamClusterer;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author Will Faithfull
 */
public class ClusteringWindow extends FixedWindow<double[]> {

    @Getter
    private final StreamClusterer clusterer;
    private       Queue<Integer>  assignments;

    public ClusteringWindow(int size, StreamClusterer clusterer) {
        super(size, double[].class);
        this.clusterer = clusterer;
        this.assignments = new LinkedList<>();
    }

    @Override
    public void update(double[] observation) {
        int cluster = clusterer.update(observation);
        assignments.add(cluster);

        if(isAtFullCapacity()) {
            clusterer.drop(assignments.remove(), getOldest());
        }

        super.update(observation);
    }
}
