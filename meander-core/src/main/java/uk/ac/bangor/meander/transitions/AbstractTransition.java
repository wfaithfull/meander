package uk.ac.bangor.meander.transitions;

import lombok.Getter;
import uk.ac.bangor.meander.MeanderException;
import uk.ac.bangor.meander.streams.StreamContext;

import java.util.Arrays;

/**
 * @author Will Faithfull
 *
 * Superclass for transitions which holds boilerplate method implementations.
 */
public abstract class AbstractTransition implements Transition {

    @Getter private final long     start;
    @Getter private final long     end;
    @Getter private double[] p1;
    @Getter private double[] p2;
    @Getter private boolean prepared;

    /**
     * Create the transition.
     * @param start The starting index.
     * @param end The ending index.
     */
    public AbstractTransition(long start, long end) {
        this.start = start;
        this.end = end;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double[] getDistribution(StreamContext context) {

        if(!isValidFor(context.getIndex()))
            throw new MeanderException("Transition was applied to an illegal index");

        return mixture(context.getIndex());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValidFor(long index) {
        return index >= start && index <= end;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void prepare(double[] p1, double[] p2) {
        if(p1.length != p2.length)
            throw new MeanderException("Distributions must be of equal size");

        this.p1 = Arrays.copyOf(p1, p1.length);
        this.p2 = Arrays.copyOf(p2, p2.length);

        this.prepared = true;
    }

    /**
     * Actual provision of mixing function delegated to implementing class.
     * @param index The stream index.
     * @return A distribution.
     */
    protected abstract double[] mixture(long index);

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("%s [start=%d, end=%d]", getClass().getSimpleName(), start, end);
    }
}
