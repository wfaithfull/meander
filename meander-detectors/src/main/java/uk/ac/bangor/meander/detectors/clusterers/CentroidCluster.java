package uk.ac.bangor.meander.detectors.clusterers;

import java.util.Arrays;

/**
 * @author Will Faithfull
 */
public class CentroidCluster implements Cluster {

    private double[] totals;
    private double[] mean;
    private double[][] cov;
    private int      weight;

    public CentroidCluster(int dimension) {
        totals = new double[dimension];
        mean = new double[dimension];
        cov = new double[dimension][dimension];
    }

    public CentroidCluster(double[] example) {
        this(example.length);
        add(example);
    }

    /**
     * Adds the example to the cluster by updating the cluster statistics.
     * @param example The next stream example to be added to the cluster.
     */
    public void add(double[] example) {
        weight++;

        double[] prevMean = Arrays.copyOf(mean, mean.length);

        for(int i=0;i<example.length;i++) {
            totals[i] += example[i];
            mean[i] = totals[i] / weight;

            for(int j=0;j<example.length;j++) {
                cov[i][j] = (cov[i][j] * (weight-1) + (example[i] - mean[i]) * (example[j] - prevMean[j]))/weight;
            }
        }
    }

    public void drop(double[] example) {
        weight--;
        for(int i=0;i<mean.length;i++) {
            totals[i] -= example[i];
            mean[i] = totals[i] / weight;
        }
    }

    @Override
    public int getWeight() {
        return weight;
    }

    @Override
    public double[] getCentre() {
        return mean;
    }

    public double[][] getCovariance() {
        return cov;
    }
}
