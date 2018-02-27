package uk.ac.bangor.meander.detectors.ensemble;

import uk.ac.bangor.meander.MeanderException;
import uk.ac.bangor.meander.detectors.AbstractMultivariateDetector;
import uk.ac.bangor.meander.detectors.Detector;

/**
 * @author Will Faithfull
 */
public class SubspaceEnsemble extends AbstractMultivariateDetector {

    private double threshold;
    private Detector<Double>[] detectors;
    private boolean[] votes;

    public SubspaceEnsemble(double threshold, Detector<Double>... detectors) {
        this.threshold = threshold;
        this.detectors = detectors;
        this.votes = new boolean[detectors.length];
    }

    public SubspaceEnsemble(Detector<Double>... detectors) {
        this(.5, detectors);
    }

    @Override
    public void update(Double[] input) {
        if(input.length != detectors.length) {
            throw new MeanderException("Number of detectors must equal number of features!");
        }

        for(int i=0;i<detectors.length;i++) {
            detectors[i].update(input[i]);
            votes[i] = detectors[i].isChangeDetected();
        }
    }

    @Override
    public boolean isChangeDetected() {

        int total = 0;
        for(boolean vote : votes) {
            if(vote)
                total++;
        }

        return (total / (double)detectors.length) > threshold;
    }
}
