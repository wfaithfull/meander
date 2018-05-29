package uk.ac.bangor.meander.detectors.controlchart.pipes;

import uk.ac.bangor.meander.detectors.Threshold;
import uk.ac.bangor.meander.detectors.controlchart.support.MovingRangeState;
import uk.ac.bangor.meander.streams.StreamContext;

/**
 * @author Will Faithfull
 */
public class MovingRangeThreshold extends Threshold<MovingRangeState> {

    protected final static double D4_2 = 3.267;

    public MovingRangeThreshold() {
        super(Op.GT, MovingRangeThreshold::limit, (mr, ctx) -> mr.getStatistic());
    }

    private static Double limit(MovingRangeState mr, StreamContext ctx) {
        if (mr.getStatistics().getN() < 2)
            return Double.POSITIVE_INFINITY;

        return D4_2 * mr.getCenter();
    }
}

