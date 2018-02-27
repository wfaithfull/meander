package uk.ac.bangor.meander.detectors.stats;

/**
 * @author Will Faithfull
 */
public abstract class AbstractIncrementalStatistics {

    protected double updateMean(double x, double mu, long n) {
        return mu + (x-mu)/n;
    }

    protected double updateSq(double x, double mu, double sq, double muNew) {
        return sq + (x-mu)*(x-muNew);
    }

    abstract void reset();

}
