package uk.ac.bangor.meander.detectors;

/**
 * @author Will Faithfull
 */
public abstract class AbstractUnboxingDetector extends AbstractMultivariateDetector {

    @Override
    public void update(Double[] input) {
        double[] unboxed = new double[input.length];
        for(int i=0;i<input.length;i++) {
            unboxed[i] = input[i];
        }
        update(unboxed);
    }

    abstract void update(double[] input);
}
