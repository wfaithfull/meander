package uk.ac.bangor.meander.detectors.clusterers;

import java.util.List;

/**
 * @author Will Faithfull
 */
public interface Clustering {

    List<Cluster> getClusters();
    double[] getDistribution();

}
