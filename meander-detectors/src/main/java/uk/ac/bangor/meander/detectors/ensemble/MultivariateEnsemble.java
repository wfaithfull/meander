package uk.ac.bangor.meander.detectors.ensemble;

import uk.ac.bangor.meander.detectors.AbstractMultivariateDetector;
import uk.ac.bangor.meander.detectors.Detector;

/**
 * @author Will Faithfull
 */
public class MultivariateEnsemble extends AbstractMultivariateDetector {

    private double          threshold;
    private Detector<Double[]>[] detectors;
    private boolean[]          votes;

    public MultivariateEnsemble(double threshold, Detector<Double[]>... detectors) {
        this.threshold = threshold;
        this.detectors = detectors;
        this.votes = new boolean[detectors.length];
    }

    @Override
    public void update(Double[] input) {
        for (int i=0;i<detectors.length;i++) {
            detectors[i].update(input);
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
