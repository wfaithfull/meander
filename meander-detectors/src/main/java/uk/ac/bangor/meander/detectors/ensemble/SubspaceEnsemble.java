package uk.ac.bangor.meander.detectors.ensemble;

import uk.ac.bangor.meander.MeanderException;
import uk.ac.bangor.meander.detectors.AbstractMultivariateDetector;
import uk.ac.bangor.meander.detectors.Detector;
import uk.ac.bangor.meander.detectors.Pipe;
import uk.ac.bangor.meander.streams.StreamContext;

/**
 * @author Will Faithfull
 */
public class SubspaceEnsemble implements Pipe<Double[], Boolean[]> {

    private double threshold;
    private Detector<Double>[] detectors;
    private Boolean[] votes;

    public SubspaceEnsemble(double threshold, Detector<Double>... detectors) {
        this.threshold = threshold;
        this.detectors = detectors;
        this.votes = new Boolean[detectors.length];
    }

    public SubspaceEnsemble(Detector<Double>... detectors) {
        this(.5, detectors);
    }

    @Override
    public Boolean[] execute(Double[] value, StreamContext context)  {
        if(value.length != detectors.length) {
            throw new MeanderException("Number of detectors must equal number of features!");
        }

        for(int i=0;i<detectors.length;i++) {
            detectors[i].update(value[i]);
            votes[i] = detectors[i].isChangeDetected();
        }

        return votes;
    }
}
