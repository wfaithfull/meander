package uk.ac.bangor.meander.detectors.controlchart.pipes;

import uk.ac.bangor.meander.detectors.controlchart.support.MovingRangeState;
import uk.ac.bangor.meander.streams.StreamContext;

/**
 * @author Will Faithfull
 */
public class IndividualsMovingRangeThreshold extends MovingRangeThreshold {

    @Override
    public Boolean execute(MovingRangeState value, StreamContext context) {

        double ucl = value.getStatistics().mean() + (2.66 * value.getCenter());
        double lcl = value.getStatistics().mean() - (2.66 * value.getCenter());

        return value.getLast() > ucl || value.getLast() < lcl;
    }

}

