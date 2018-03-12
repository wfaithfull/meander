package uk.ac.bangor.meander.detectors.ensemble;

import uk.ac.bangor.meander.detectors.Pipe;
import uk.ac.bangor.meander.streams.StreamContext;

/**
 * Created by wfaithfull on 11/03/18.
 */
public class DecayingMajority implements Pipe<Boolean[], Double> {

    public DecayingMajority(DecayFunction decayFunction) {
        this.decayFunction = decayFunction;
    }

    public DecayingMajority() {
        this(new LogisticDecayFunction());
    }

    private double[] votes;
    private long[] lastChange;
    private DecayFunction decayFunction;

    @Override
    public Double execute(Boolean[] value, StreamContext context) {

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

        return (total / (double)value.length);
    }
}
