package uk.ac.bangor.meander.transitions;

import uk.ac.bangor.meander.MeanderException;

import java.util.Arrays;

/**
 * @author Will Faithfull
 */
public class LinearTransition extends AbstractTransition {

    private double[] distribution;
    private double[] increments;

    public LinearTransition(int start, int end) {
        super(start, end);

        if(end - start <= 0)
            throw new MeanderException("Transition must have a duration");
    }

    @Override
    public void prepare(double[] p1, double[] p2) {
        super.prepare(p1, p2);

        this.distribution = Arrays.copyOf(p1, p1.length);
        this.increments = new double[p1.length];

        // +2 because start-1 and end+1 are the true ends of the transition. We want the first intermediate distribution
        // at start, and the last intermediate distribution at end.
        double duration = 2 + (getEnd() - getStart());

        for(int i=0;i<p1.length;i++) {
            double increment = (getP2()[i] - getP1()[i]) / duration;
            increments[i] = increment;
        }
    }

    @Override
    protected double[] mixture(long index) {
        for(int i=0;i<getP1().length;i++) {
            distribution[i] += increments[i];

            // Cap to account for floating point error
            if(distribution[i] > 1) {
                distribution[i] = 1.0;
            } else if (distribution[i] < 0) {
                distribution[i] = 0.0;
            }
        }
        return distribution;
    }

}
