package uk.ac.bangor.meander.detectors;

import uk.ac.bangor.meander.streams.StreamContext;

/**
 * @author Will Faithfull
 */
public abstract class AbstractFunctionalDetector extends AbstractMultivariateDetector implements ReductionFunction, DecisionFunction {

    private boolean change;

    @Override
    public void update(Double[] input) {
        double statistic = reduce(input);
        change = decide(statistic);
    }

    @Override
    public boolean isChangeDetected() {
        return change;
    }

    @Override
    public void after(StreamContext context) {
        if(reporter != null) {
            reporter.report(getState());
        }
    }

    public abstract State getState();
}
