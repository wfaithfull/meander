package uk.ac.bangor.meander.detectors.ensemble;

import uk.ac.bangor.meander.detectors.AbstractMultivariateDetector;
import uk.ac.bangor.meander.detectors.Detector;

/**
 * Ensemble that uses a function to decay votes over time. Instead of boolean
 * voting, members that signal change set their vote to 1.0. This is decayed
 * as time passes from their vote.
 *
 * The idea of this ensemble is that we may have detectors which detect the
 * same change at slightly different times. Suppose one a detector signals
 * at t=30. Another signals at t=35. These relate to a true change at 29.
 * If we simply check at any given time whether there is agreement, we will
 * miss the change. This way, even after 5 steps have passed, there is still
 * a decayed remnant of the vote at t=30, enough to tip the vote at t=35 over
 * the threshold.
 *
 * @author Will Faithfull
 */
public class VoteDecayEnsemble extends AbstractMultivariateDetector {

    private double               threshold;
    private DecayFunction        decayFunction;
    private Detector<Double[]>[] detectors;
    private double[]             votes;
    private long[]               lastChange;
    private long n;

    public VoteDecayEnsemble(double threshold, Detector<Double[]>... detectors) {
        this(threshold, new LogisticDecayFunction(), detectors);
    }


    public VoteDecayEnsemble(double threshold, DecayFunction decayFunction, Detector<Double[]>... detectors) {
        this.threshold = threshold;
        this.decayFunction = decayFunction;
        this.detectors = detectors;
        this.votes = new double[detectors.length];
        this.lastChange = new long[detectors.length];
    }

    @Override
    public void update(Double[] input) {
        for (int i=0;i<detectors.length;i++) {
            detectors[i].update(input);
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
