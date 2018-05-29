package uk.ac.bangor.meander.detectors.m2d.pipes;

import uk.ac.bangor.meander.detectors.Pipe;
import uk.ac.bangor.meander.detectors.m2d.support.KLState;
import uk.ac.bangor.meander.streams.StreamContext;

/**
 * @author Will Faithfull
 */
public class KLLikelihoodRatioThreshold implements Pipe<KLState, Double> {
    @Override
    public Double execute(KLState value, StreamContext context) {

        double sumlogP = 0;
        for (int i = 0; i < value.getP().length; i++) {
            sumlogP += Math.log(value.getP()[i]);
        }

        return -Math.log(value.getK()) - (sumlogP / value.getK());
    }
}
