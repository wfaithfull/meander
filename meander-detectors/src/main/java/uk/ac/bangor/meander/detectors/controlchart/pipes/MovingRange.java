package uk.ac.bangor.meander.detectors.controlchart.pipes;

import lombok.Setter;
import uk.ac.bangor.meander.detectors.Pipe;
import uk.ac.bangor.meander.detectors.controlchart.support.MovingRangeState;
import uk.ac.bangor.meander.detectors.stats.support.IncrementalStatistics;
import uk.ac.bangor.meander.streams.StreamContext;

/**
 * @author Will Faithfull
 */

public class MovingRange implements Pipe<Double, MovingRangeState> {

    protected double last      = 0;
    protected double statistic = 0;
    protected double sum;
    protected double center = 0;

    protected IncrementalStatistics statistics = new IncrementalStatistics();

    @Setter
    protected boolean resetOnChangeDetected;

    public void update(Double input) {

        statistics.update(input);

        statistic = Math.abs(input - last);
        sum += statistic;

        if (statistics.getN() >= 2)
            center = sum / (statistics.getN() - 1);

        last = input;
    }

    @Override
    public void reset() {
        last = 0;
        statistic = 0;
        sum = 0;
        center = 0;
        statistics.reset();
    }

    @Override
    public MovingRangeState execute(Double value, StreamContext context) {
        update(value);
        return new MovingRangeState(statistic, center, last, statistics);
    }
}
