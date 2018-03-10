package uk.ac.bangor.meander.detectors;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import uk.ac.bangor.meander.streams.StreamContext;

/**
 * @author Will Faithfull
 */
public abstract class AbstractUnivariateDetector implements Detector<Double>, DecisionFunction {

    protected ChartReporter reporter;

    @Override
    public boolean decide(Double statistic) {
        update(statistic);
        return isChangeDetected();
    }

    @Override
    public void reset() {
        throw new NotImplementedException();
    }

    @Override
    public void setReporter(ChartReporter chartReporter) {
        this.reporter = chartReporter;
    }

    @Override
    public boolean ready() {
        return true;
    }

    @Override
    public void after(StreamContext context) {

    }
}
