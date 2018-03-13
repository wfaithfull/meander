package uk.ac.bangor.meander.detectors.pipes;

import lombok.extern.java.Log;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import uk.ac.bangor.meander.detectors.AbstractKMeansQuantizingDetector;
import uk.ac.bangor.meander.detectors.Pipe;
import uk.ac.bangor.meander.detectors.windowing.WindowPair;
import uk.ac.bangor.meander.streams.StreamContext;

/**
 * @author Will Faithfull
 */
@Log
public class SPLL extends AbstractKMeansQuantizingDetector implements Pipe<Double[], Double> {

    ChiSquaredDistribution cdf;
    private double statistic;

    public SPLL(WindowPair<double[]> windowPair, int K) {
        super(windowPair, K);
    }

    @Override
    public Double execute(Double[] value, StreamContext context) {
        super.update(value);

        double[] distances = getMinClusterToObservationDistances();

        if (distances == null) {
            throw new NotReadyException(this);
        }

        double likelihoodTerm = 0;
        for(int i=0;i<distances.length;i++) {
            likelihoodTerm += distances[i];
        }
        return likelihoodTerm / distances.length;
    }

    @Override
    public boolean needReset() {
        return true;
    }

    @Override
    public void reset() {
        super.windowPair.clear();
    }

    @Override
    public boolean ready() {
        return windowPair.size() == windowPair.capacity();
    }

}
