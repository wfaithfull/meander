package uk.ac.bangor.meander.transitions;

/**
 * @author Will Faithfull
 *
 * A transition between distributions which simply replaces p1 with p2 at the specified index.
 */
public class AbruptTransition extends AbstractTransition {

    /**
     * {@inheritDoc}
     */
    public AbruptTransition(long start) {
        super(start, start);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected double[] mixture(long index) {
        if(index < getStart()) {
            return getP1();
        } else {
            return getP2();
        }
    }
}
