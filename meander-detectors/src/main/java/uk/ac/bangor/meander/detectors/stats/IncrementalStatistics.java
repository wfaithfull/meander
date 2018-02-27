package uk.ac.bangor.meander.detectors.stats;

/**
 * @author Will Faithfull
 */
public class IncrementalStatistics extends AbstractIncrementalStatistics {

    long n;
    double mu = 0.0;
    double sq = 0.0;

    public void update(double x) {
        ++n;
        double muNew = updateMean(x, mu, n);
        sq = updateSq(x, mu, sq, muNew);
        mu = muNew;
    }

    public double mean() {
        return mu;
    }

    public double var() {
        return n > 1 ? sq/n : 0.0;
    }

    public long getN() {
        return n;
    }

    public void reset() {
        n = 0;
        mu = 0.0;
        sq = 0.0;
    }

}
