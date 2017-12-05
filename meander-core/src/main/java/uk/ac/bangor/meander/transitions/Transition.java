package uk.ac.bangor.meander.transitions;

/**
 * @author Will Faithfull
 */
public interface Transition {

    boolean isValidFor(long index);

    double[] getMixture(long index);

    void prepare(double[] p1, double[] p2);

    boolean isPrepared();

    long getStart();

    long getEnd();

}
