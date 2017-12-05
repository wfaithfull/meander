package uk.ac.bangor.meander.transitions;

import uk.ac.bangor.meander.MeanderException;

import java.util.Arrays;

/**
 * @author Will Faithfull
 *
 * A transition between distributions which uses a logistic function to phase out p1 and phase in p2.
 *
 * f(x) = 1 / (1 + exp(-k(x-x_0)))
 */
public class LogisticTransition extends AbstractTransition {

    private final int k;
    private double[] distribution;
    private double[] difference;
    long duration;

    /**
     * {@inheritDoc}
     */
    public LogisticTransition(long start, long end) {
        this(start, end, 1);
    }

    /**
     * {@inheritDoc}
     */
    public LogisticTransition(long start, long end, int k) {
        super(start, end);

        if(end - start <= 0)
            throw new MeanderException("Transition must have a duration");

        this.k = k;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void prepare(double[] p1, double[] p2) {
        super.prepare(p1, p2);

        this.distribution = Arrays.copyOf(p1, p1.length);
        this.difference = new double[p1.length];

        for(int i=0;i<p1.length;i++) {
            difference[i] = (getP2()[i] - getP1()[i]);
        }

        // +2 because start-1 and end+1 are the true ends of the transition. We want the first intermediate distribution
        // at start, and the last intermediate distribution at end.
        duration = 2 + (getEnd() - getStart());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected double[] mixture(long index) {

        for(int i=0;i<difference.length;i++) {

            long norm = index - (getStart()-1) - (duration / 2);

            if(difference[i] == 0.0) {
                distribution[i] = 0.0;
            } else if (difference[i] > 0.0) {
                distribution[i] = up(norm);
            } else {
                distribution[i] = down(norm);
            }
        }
        return distribution;
    }

    private double up(long index) {
        return 1 / (1 + Math.exp(-k*index));
    }

    private double down(long index) {
        return 1 / (1 + Math.exp(k*index));
    }
}
