package uk.ac.bangor.meander.detectors.clusterers;

/**
 * @author Will Faithfull
 */
public interface Cluster {

    int getWeight();
    double[] getCentre();
    double[][] getCovariance();
    void add(double[] example);
    void drop(double[] example);

}
