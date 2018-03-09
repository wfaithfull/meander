package uk.ac.bangor.meander.detectors;

/**
 * @author Will Faithfull
 */
public interface Detector {
    boolean isChangeDetected();
    void reset();
}
