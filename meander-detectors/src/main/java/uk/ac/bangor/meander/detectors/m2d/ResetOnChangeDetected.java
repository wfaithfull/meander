package uk.ac.bangor.meander.detectors.m2d;

import uk.ac.bangor.meander.detectors.Pipe;
import uk.ac.bangor.meander.streams.StreamContext;

/**
 * @author Will Faithfull
 */
public class ResetOnChangeDetected implements Pipe<Boolean, Boolean> {
    @Override
    public Boolean execute(Boolean value, StreamContext context) {
        if (value) {
            context.detection();
        }
        return value;
    }
}
