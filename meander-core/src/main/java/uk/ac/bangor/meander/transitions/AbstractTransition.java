package uk.ac.bangor.meander.transitions;

import lombok.Getter;

import java.util.Arrays;

/**
 * @author Will Faithfull
 */
public abstract class AbstractTransition implements Transition {

    @Getter private final long     start;
    @Getter private final long     end;
    @Getter private double[] p1;
    @Getter private double[] p2;
    @Getter private boolean prepared;

    public AbstractTransition(long start, long end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public double[] getMixture(long index) {

        if(!isValidFor(index))
            throw new IllegalStateException("Transition was applied to an illegal index");

        return mixture(index);
    }

    @Override
    public boolean isValidFor(long index) {
        return index >= start && index <= end;
    }

    @Override
    public void prepare(double[] p1, double[] p2) {
        if(p1.length != p2.length)
            throw new IllegalArgumentException("Distributions must be of equal size");

        this.p1 = Arrays.copyOf(p1, p1.length);
        this.p2 = Arrays.copyOf(p2, p2.length);

        this.prepared = true;
    }

    protected abstract double[] mixture(long index);

    @Override
    public String toString() {
        return String.format("%s [start=%d, end=%d]", getClass().getSimpleName(), start, end);
    }
}
