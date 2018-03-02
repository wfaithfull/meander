package uk.ac.bangor.meander.detectors.controlchart;

import lombok.Setter;
import uk.ac.bangor.meander.detectors.AbstractUnivariateDetector;
import uk.ac.bangor.meander.detectors.stats.IncrementalStatistics;

/**
 * Shewhart Moving Range control chart
 *
 * @author Will Faithfull
 */
public class MR extends AbstractUnivariateDetector {

    protected double last      = 0;
    protected double statistic = 0;
    protected double sum;
    protected double center    = 0;

    protected IncrementalStatistics statistics = new IncrementalStatistics();

    @Setter
    protected boolean resetOnChangeDetected;

    protected final static double D4_2 = 3.267;

    @Override
    public void update(Double input) {

        statistics.update(input);

        statistic = Math.abs(input - last);
        sum += statistic;

        if(statistics.getN() >= 2)
            center = sum / (statistics.getN() - 1);

        last = input;
    }

    public void reset() {
        last = 0;
        statistic = 0;
        sum = 0;
        center = 0;
        statistics.reset();
    }

    @Override
    public boolean isChangeDetected() {
        if(statistics.getN() < 2)
            return false;

        boolean mrUCL = statistic > D4_2 * center;

        if(mrUCL && resetOnChangeDetected) {
            reset();
        }

        return mrUCL;
    }

}
