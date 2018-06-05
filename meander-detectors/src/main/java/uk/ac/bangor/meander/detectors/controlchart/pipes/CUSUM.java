package uk.ac.bangor.meander.detectors.controlchart.pipes;

import uk.ac.bangor.meander.detectors.Pipe;
import uk.ac.bangor.meander.streams.StreamContext;

/**
 * @author Will Faithfull
 */
public class CUSUM implements Pipe<Double, Double> {

    private final static double DEFAULT_MAGNITUDE   = 0.05;
    private final static long   DEFAULT_READY_AFTER = 50;

    private double cusumPrev = 0;
    private double cusum;
    private double magnitude;
    private double magnitudeMultiplier;
    private long   readyAfter;

    private long   observationCount = 0;
    private double runningMean      = 0.0;
    private double runningVariance  = 0.0;

    /**
     * Create a CUSUM detector
     *
     * @param magnitudeMultiplier Magnitude of acceptable change in stddevs
     * @param readyAfter          Number of observations before allowing change to be signalled
     */
    public CUSUM(double magnitudeMultiplier,
                 long readyAfter) {
        this.magnitudeMultiplier = magnitudeMultiplier;
        this.readyAfter = readyAfter;
    }

    public CUSUM() {
        this(DEFAULT_MAGNITUDE, DEFAULT_READY_AFTER);
    }

    @Override
    public boolean ready() {
        return this.observationCount >= readyAfter;
    }

    public void reset() {
        this.cusum = 0;
        this.cusumPrev = 0;
        this.runningMean = 0;
        this.observationCount = 0;
    }

    @Override
    public Double execute(Double value, StreamContext context) {
        ++observationCount;

        // Instead of providing the target mean as a parameter as
        // we would in an offline test, we calculate it as we go to
        // create a target of normality.
        double newMean = runningMean + (value - runningMean) / observationCount;
        runningVariance += (value - runningMean) * (value - newMean);
        runningMean = newMean;
        double std = Math.sqrt(runningVariance);

        magnitude = magnitudeMultiplier * std;

        cusum = Math.max(0, cusumPrev + (value - runningMean - magnitude));

        if (!ready()) {
            throw new NotReadyException(this);
        }

        cusumPrev = cusum;
        return cusum;
    }
}
