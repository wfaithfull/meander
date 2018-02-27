package uk.ac.bangor.meander.detectors;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * @author Will Faithfull
 */
public abstract class AbstractUnivariateDetector implements Detector<Double>, DecisionFunction {

    @Override
    public boolean decide(Double statistic) {
        update(statistic);
        return isChangeDetected();
    }

    @Override
    public void reset() {
        throw new NotImplementedException();
    }

}
