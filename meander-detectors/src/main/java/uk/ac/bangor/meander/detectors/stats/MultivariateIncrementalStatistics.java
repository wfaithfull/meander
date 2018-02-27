package uk.ac.bangor.meander.detectors.stats;

/**
 * @author Will Faithfull
 */
public class MultivariateIncrementalStatistics extends AbstractIncrementalStatistics {

    private int features;
    private double[] mus;
    private double[] newMus;
    private double[] sqs;
    private long n;

    public MultivariateIncrementalStatistics(int features) {
        this.features = features;

        mus = new double[features];
        newMus = new double[features];
        sqs = new double[features];
    }

    public void update(double[] x) {
        n++;
        if(x.length != features)
            throw new IllegalStateException("Cannot change number of features for statistics calculation.");

        double[] newMus = new double[features];
        for(int i=0;i<features;i++) {
            newMus[i] = updateMean(x[i], mus[i], n);
        }

        for(int i=0;i<features;i++) {
            sqs[i] = updateSq(x[i], mus[i], sqs[i], newMus[i]);
        }
    }

    public double[] getMean() {
        return mus;
    }

    public double[] getVar() {
        return sqs;
    }

    public long getN() {
        return n;
    }

    @Override
    void reset() {
        mus = new double[features];
        newMus = new double[features];
        sqs = new double[features];
        n = 0;
    }
}
