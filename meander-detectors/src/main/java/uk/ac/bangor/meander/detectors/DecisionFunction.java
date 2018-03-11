package uk.ac.bangor.meander.detectors;

/**
 * @author Will Faithfull
 */
public interface DecisionFunction extends Pipe<Double, Boolean> {
    boolean decide(Double statistic);

    default Boolean execute(Double in) {
        return decide(in);
    }
}
