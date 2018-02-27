package uk.ac.bangor.meander.detectors.clusterers;

import java.util.List;

/**
 * @author Will Faithfull
 */
public interface StreamClusterer {

    int update(double[] example);

    List<Cluster> getClusters();

    double[] getDistribution();

    void drop(int cluster, double[] example);

}
