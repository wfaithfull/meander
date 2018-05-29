package uk.ac.bangor.meander.detectors.m2d.pipes;

import uk.ac.bangor.meander.detectors.Pipe;
import uk.ac.bangor.meander.detectors.m2d.support.KLState;
import uk.ac.bangor.meander.streams.StreamContext;

/**
 * @author Will Faithfull
 */
public class KLStateStatistic implements Pipe<KLState, Double> {
    @Override
    public Double execute(KLState value, StreamContext context) {
        return value.getStatistic();
    }
}
