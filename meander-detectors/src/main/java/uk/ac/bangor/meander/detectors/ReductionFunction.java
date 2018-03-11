package uk.ac.bangor.meander.detectors;

/**
 * @author Will Faithfull
 */
public interface ReductionFunction extends Pipe<Double[], Double> {

    double reduce(Double[] example);

    default Double execute(Double[] in) {
        return reduce(in);
    }

}
