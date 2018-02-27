package uk.ac.bangor.meander.detectors.controlchart;

/**
 * Shewhart individuals moving range chart which thresholds the individual values.
 *
 * @author Will Faithfull
 */
public class IMR extends MR {

    @Override
    public boolean isChangeDetected() {

        double ucl = statistics.mean() + (2.66 * center);
        double lcl = statistics.mean() - (2.66 * center);

        return last > ucl || last < lcl;
    }

}
