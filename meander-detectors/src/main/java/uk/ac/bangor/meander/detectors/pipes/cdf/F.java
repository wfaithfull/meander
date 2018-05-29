package uk.ac.bangor.meander.detectors.pipes.cdf;

import org.apache.commons.math3.distribution.FDistribution;
import uk.ac.bangor.meander.detectors.Pipe;
import uk.ac.bangor.meander.streams.StreamContext;

/**
 * @author Will Faithfull
 */
public class F extends AbstractCDFPipe<Double> implements Pipe<Double, Double> {

    private final int           df1;
    private final int           df2;
    private       FDistribution fDistribution;

    public F(int df1, int df2) {
        this.df1 = df1;
        this.df2 = df2;
        fDistribution = new FDistribution(df1, df2);
    }

    @Override
    public Double execute(Double value, StreamContext context) {
        double pst = fDistribution.cumulativeProbability(value);
        return pst;
    }
}

