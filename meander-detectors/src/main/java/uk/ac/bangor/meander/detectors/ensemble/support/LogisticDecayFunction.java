package uk.ac.bangor.meander.detectors.ensemble.support;

/**
 * @author Will Faithfull
 */
public class LogisticDecayFunction implements DecayFunction {

    private double k;

    public LogisticDecayFunction() {
        this(1);
    }

    public LogisticDecayFunction(double k) {
        this.k = k;
    }

    @Override
    public Double apply(Long index) {
        return 1 / (1 + Math.exp(k*index));
    }

}
