package uk.ac.bangor.meander.detectors.clusterers;

/**
 * @author Will Faithfull
 */
public interface Cluster {

    int getWeight();
    boolean isEmpty();
    double[] getCentre();
    double[] getVariance();
    double[] getStdDev();
    double[][] getCovariance();
    void add(double[] example);
    void drop(double[] example);

}
