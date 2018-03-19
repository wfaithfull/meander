package uk.ac.bangor.meander.evaluators;

/**
 * @author Will Faithfull
 */
public interface ProgressReporter {

    void update(long progress);
    void update(long progress, String message);
    void update(long progress, long total);
    void update(long progress, long total, String message);

}
