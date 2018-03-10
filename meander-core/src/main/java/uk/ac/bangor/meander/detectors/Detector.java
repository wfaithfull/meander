package uk.ac.bangor.meander.detectors;

import uk.ac.bangor.meander.streams.StreamContext;

/**
 * @author Will Faithfull
 */
public interface Detector<T> {

    void update(T input);

    boolean isChangeDetected();

    boolean ready();

    void reset();

    void after(StreamContext context);

    void setReporter(ChartReporter chartReporter);
}
