package uk.ac.bangor.meander.detectors.ensemble;

import uk.ac.bangor.meander.detectors.Pipe;
import uk.ac.bangor.meander.streams.StreamContext;

/**
 * Created by wfaithfull on 11/03/18.
 */
public class DecayingMajority implements Pipe<Boolean[], Boolean> {

    public DecayingMajority(DecayFunction decayFunction, double threshold) {
        this.decayFunction = decayFunction;
        this.threshold = threshold;
    }

    public DecayingMajority() {
        this(new LogisticDecayFunction(), 0.5);
    }

    private double threshold;
    private double[] votes;
    private long[] lastChange;
    private DecayFunction decayFunction;

    @Override
    public Boolean execute(Boolean[] value, StreamContext context) {

        if(votes == null) {
            votes = new double[value.length];
            lastChange = new long[value.length];
        }

        for (int i = 0; i < value.length; i++) {
            if (value[i]) {
                votes[i] = 1.0;
                lastChange[i] = context.getIndex();
            } else {
                votes[i] = decayFunction.apply(context.getIndex() - lastChange[i]);
            }
        }

        double total = 0;
        for(double vote : votes) {
            total += vote;
        }

        return (total / (double)value.length) > threshold;
    }
}
