package uk.ac.bangor.meander.detectors;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * @author Will Faithfull
 */
public abstract class AbstractMultivariateDetector implements Detector<Double[]> {

    @Override
    public void reset() {
        throw new NotImplementedException();
    }

}
