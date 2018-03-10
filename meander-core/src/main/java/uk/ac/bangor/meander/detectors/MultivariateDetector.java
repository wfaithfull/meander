package uk.ac.bangor.meander.detectors;

/**
 * @author Will Faithfull
 */
public interface MultivariateDetector extends Detector {
    void update(double[] input);
}
