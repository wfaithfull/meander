package uk.ac.bangor.meander.detectors;

/**
 * @author Will Faithfull
 */
public interface Detector<T> {

    void update(T input);

    boolean isChangeDetected();

    void reset();
}
