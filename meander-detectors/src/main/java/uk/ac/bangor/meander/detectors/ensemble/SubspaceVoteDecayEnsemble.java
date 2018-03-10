package uk.ac.bangor.meander.detectors.ensemble;

import uk.ac.bangor.meander.MeanderException;
import uk.ac.bangor.meander.detectors.AbstractMultivariateDetector;
import uk.ac.bangor.meander.detectors.Detector;

/**
 * Ensemble that uses a function to decay votes over time. Instead of boolean
 * voting, members that signal change set their vote to 1.0. This is decayed
 * as time passes from their vote.
 *
 * This version applies univariate detectors to each feature of the input space.
 * @author Will Faithfull
 */
public class SubspaceVoteDecayEnsemble extends AbstractMultivariateDetector {

    private double               threshold;
    private DecayFunction        decayFunction;
    private Detector<Double>[] detectors;
    private double[]             votes;
    private long[]               lastChange;
    private long                 n;

    public SubspaceVoteDecayEnsemble(double threshold, Detector<Double>... detectors) {
        this(threshold, new LogisticDecayFunction(), detectors);
    }


    public SubspaceVoteDecayEnsemble(double threshold, DecayFunction decayFunction, Detector<Double>... detectors) {
        this.threshold = threshold;
        this.decayFunction = decayFunction;
        this.detectors = detectors;
        this.votes = new double[detectors.length];
        this.lastChange = new long[detectors.length];
    }

    @Override
    public void update(Double[] input) {

        if(input.length != detectors.length) {
            throw new MeanderException("Number of detectors must equal number of features!");
        }

        for(int i=0;i<detectors.length;i++) {
            detectors[i].update(input[i]);
            if(detectors[i].isChangeDetected()) {
                votes[i] = 1.0;
                lastChange[i] = n;
            } else {
                votes[i] = decayFunction.apply(n - lastChange[i]);
            }
        }

        n++;
    }

    @Override
    public boolean isChangeDetected() {

        double total = 0;
        for(double vote : votes) {
            total += vote;
        }

        return (total / (double)detectors.length) > threshold;
    }

}
