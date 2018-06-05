package uk.ac.bangor.meander.detectors.ensemble.pipes;

import uk.ac.bangor.meander.detectors.Pipe;

/**
 * @author Will Faithfull
 */
public class UnivariateEnsemble extends BasicEnsemble<Double> {

    public UnivariateEnsemble(Pipe<Double, Boolean>... detectors) {
        super(detectors);
    }
}
