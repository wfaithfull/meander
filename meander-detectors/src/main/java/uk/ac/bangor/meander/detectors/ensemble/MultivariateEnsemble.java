package uk.ac.bangor.meander.detectors.ensemble;

import uk.ac.bangor.meander.detectors.Pipe;
import uk.ac.bangor.meander.streams.StreamContext;

/**
 * @author Will Faithfull
 */
public class MultivariateEnsemble implements Pipe<Double[], Boolean[]> {

    private Pipe<Double[], Boolean>[] detectors;
    private Boolean[]          votes;

    public MultivariateEnsemble(Pipe<Double[], Boolean>... detectors) {
        this.detectors = detectors;
        this.votes = new Boolean[detectors.length];
    }

    @Override
    public Boolean[] execute(Double[] value, StreamContext context) {
        for (int i=0;i<detectors.length;i++) {
            votes[i] = detectors[i].execute(value, context);
        }

        return votes;
    }
}
