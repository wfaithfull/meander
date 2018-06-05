package uk.ac.bangor.meander.detectors.ensemble.pipes;

import uk.ac.bangor.meander.detectors.Pipe;
import uk.ac.bangor.meander.streams.StreamContext;

/**
 * @author Will Faithfull
 */
public abstract class BasicEnsemble<T> implements Pipe<T, Boolean[]> {

    private Pipe<T, Boolean>[] detectors;
    private Boolean[]          votes;

    public BasicEnsemble(Pipe<T, Boolean>[] detectors) {
        this.detectors = detectors;
        this.votes = new Boolean[detectors.length];
    }

    @Override
    public Boolean[] execute(T value, StreamContext context) {
        for (int i = 0; i < detectors.length; i++) {
            votes[i] = detectors[i].execute(value, context);
        }

        return votes;
    }

}
