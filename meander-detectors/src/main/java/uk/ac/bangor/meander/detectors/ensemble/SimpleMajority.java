package uk.ac.bangor.meander.detectors.ensemble;

import uk.ac.bangor.meander.detectors.Pipe;
import uk.ac.bangor.meander.streams.StreamContext;

/**
 * Created by wfaithfull on 11/03/18.
 */
public class SimpleMajority implements Pipe<Boolean[], Boolean> {

    private double threshold;

    public SimpleMajority(double threshold) {
        this.threshold = threshold;
    }

    public SimpleMajority() {
        this(0.5);
    }

    @Override
    public Boolean execute(Boolean[] value, StreamContext context) {

        int total = 0;
        for(boolean vote : value) {
            if(vote)
                total++;
        }

        return (total / (double)value.length) > threshold;
    }
}
