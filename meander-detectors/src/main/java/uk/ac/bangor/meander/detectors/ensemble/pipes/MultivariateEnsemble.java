package uk.ac.bangor.meander.detectors.ensemble.pipes;

import uk.ac.bangor.meander.detectors.Pipe;

/**
 * @author Will Faithfull
 */
public class MultivariateEnsemble extends BasicEnsemble<Double[]> {
    public MultivariateEnsemble(Pipe<Double[], Boolean>... detectors) {
        super(detectors);
    }
}
