package uk.ac.bangor.meander.transitions;

/**
 * @author Will Faithfull
 */
public class AbruptTransition extends AbstractTransition {

    public AbruptTransition(long start) {
        super(start, start);
    }

    @Override
    protected double[] mixture(long index) {
        if(index < getStart()) {
            return getP1();
        } else {
            return getP2();
        }
    }
}
