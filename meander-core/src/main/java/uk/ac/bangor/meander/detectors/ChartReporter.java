package uk.ac.bangor.meander.detectors;

import uk.ac.bangor.meander.streams.StreamContext;

/**
 * @author Will Faithfull
 */
public interface ChartReporter {

    void statistic(double statistic, Pipe pipe, StreamContext context);
    void ucl(double ucl, Pipe pipe, StreamContext context);
    void lcl(double lcl, Pipe pipe, StreamContext context);

}
