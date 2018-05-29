package uk.ac.bangor.meander.detectors.ensemble.support;

/**
 * @author Will Faithfull
 */
public class LinearDecayFunction implements DecayFunction {

    private double increment;
    private int length;

    public LinearDecayFunction(int length) {
        this.increment = 1/length;
        this.length = length;
    }

    @Override
    public Double apply(Long index) {
        if(index > length) {
            return 0d;
        } else {
            return 1-(index*increment);
        }
    }

}
