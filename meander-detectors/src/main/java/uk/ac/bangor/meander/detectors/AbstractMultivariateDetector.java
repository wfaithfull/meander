package uk.ac.bangor.meander.detectors;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import uk.ac.bangor.meander.streams.StreamContext;

/**
 * @author Will Faithfull
 */
public abstract class AbstractMultivariateDetector implements Detector<Double[]> {

    protected ChartReporter reporter;

    @Override
    public void reset() {
        throw new NotImplementedException();
    }

    @Override
    public void setReporter(ChartReporter chartReporter) {
        this.reporter = chartReporter;
    }

    @Override
    public void after(StreamContext context) {

    }

    @Override
    public boolean ready() {
        return true;
    }
}
