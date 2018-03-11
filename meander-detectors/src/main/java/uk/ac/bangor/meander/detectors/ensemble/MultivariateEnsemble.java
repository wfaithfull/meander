package uk.ac.bangor.meander.detectors.ensemble;

import uk.ac.bangor.meander.detectors.AbstractMultivariateDetector;
import uk.ac.bangor.meander.detectors.Detector;
import uk.ac.bangor.meander.detectors.Pipe;
import uk.ac.bangor.meander.streams.StreamContext;

/**
 * @author Will Faithfull
 */
public class MultivariateEnsemble implements Pipe<Double[], Boolean[]> {

    private Detector<Double[]>[] detectors;
    private Boolean[]          votes;

    public MultivariateEnsemble(Detector<Double[]>... detectors) {
        this.detectors = detectors;
        this.votes = new Boolean[detectors.length];
    }

    @Override
    public Boolean[] execute(Double[] value, StreamContext context) {
        for (int i=0;i<detectors.length;i++) {
            detectors[i].update(value);
            votes[i] = detectors[i].isChangeDetected();
        }

        return votes;
    }
}
