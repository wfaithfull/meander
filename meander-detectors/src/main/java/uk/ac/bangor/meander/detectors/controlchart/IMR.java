package uk.ac.bangor.meander.detectors.controlchart;

import uk.ac.bangor.meander.streams.StreamContext;

/**
 * Shewhart individuals moving range chart which thresholds the individual values.
 *
 * @author Will Faithfull
 */
public class IMR extends MR.MRThreshold {

    @Override
    public Boolean execute(MR.MRState value, StreamContext context) {

        double ucl = value.getStatistics().mean() + (2.66 * value.getCenter());
        double lcl = value.getStatistics().mean() - (2.66 * value.getCenter());

        return value.getLast() > ucl || value.getLast() < lcl;
    }

}
