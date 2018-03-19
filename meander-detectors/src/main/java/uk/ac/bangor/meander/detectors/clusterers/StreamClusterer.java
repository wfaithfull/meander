package uk.ac.bangor.meander.detectors.clusterers;

/**
 * @author Will Faithfull
 */
public interface StreamClusterer extends Clustering {

    int update(double[] example);

    void drop(int cluster, double[] example);

}
