package uk.ac.bangor.meander.detectors;

/**
 * @author Will Faithfull
 */
public interface DecisionFunction {
    boolean decide(Double statistic);
}
