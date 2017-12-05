package uk.ac.bangor.meander.transitions;

import uk.ac.bangor.meander.streams.MixingFunction;

/**
 * @author Will Faithfull
 *
 * Interface representing a transition between distributions as a special case of a mixing function.
 */
public interface Transition extends MixingFunction {

    /**
     * Is the transition supposed to be applied for the given index?
     * @param index Index to test.
     * @return true, if the transition is supposed to be applied.
     */
    boolean isValidFor(long index);

    /**
     * Prepare the transition between p1 and p2.
     * @param p1 The distribution to transition from.
     * @param p2 The distribution to transition to.
     */
    void prepare(double[] p1, double[] p2);

    /**
     * Is the transition prepared?
     * @return true, if it is.
     */
    boolean isPrepared();

    /**
     * The start index of the transition.
     * @return The start index of the transition.
     */
    long getStart();

    /**
     * The end index of the transition.
     * @return The end index of the transition.
     */
    long getEnd();

}
