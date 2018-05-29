package uk.ac.bangor.meander.detectors.pipes.cdf;

import uk.ac.bangor.meander.detectors.Pipe;

/**
 * @author Will Faithfull
 */
public abstract class AbstractCDFPipe<T> implements Pipe<T, Double> {

    /**
     * Maps this cdf to it's folded probability:
     * <p>
     * min(p, 1-p)
     *
     * @return min(p, 1-p)
     */
    public Pipe<T, Double> folded() {
        return then((value, context) -> Math.min(value, 1d - value));
    }

    /**
     * Maps this cdf to it's complementary probability:
     * <p>
     * 1-p
     *
     * @return 1-p
     */
    public Pipe<T, Double> complementary() {
        return then((value, context) -> 1d - value);
    }

}
