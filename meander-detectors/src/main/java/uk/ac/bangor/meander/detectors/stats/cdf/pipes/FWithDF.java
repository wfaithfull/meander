package uk.ac.bangor.meander.detectors.stats.cdf.pipes;

import org.apache.commons.math3.distribution.FDistribution;
import uk.ac.bangor.meander.detectors.Pipe;
import uk.ac.bangor.meander.detectors.stats.cdf.support.FStatisticAndDegreesFreedom;
import uk.ac.bangor.meander.streams.StreamContext;

/**
 * @author Will Faithfull
 */
public class FWithDF extends AbstractCDFPipe<FStatisticAndDegreesFreedom> implements Pipe<FStatisticAndDegreesFreedom, Double> {

    private FDistribution fDistribution;

    @Override
    public Double execute(FStatisticAndDegreesFreedom value, StreamContext context) {
        if (value.getDf1() <= 0 || value.getDf2() <= 0) {
            return 0d;
        }

        fDistribution = new FDistribution(value.getDf1(), value.getDf2());

        double pst = fDistribution.cumulativeProbability(value.getStatistic());
        return pst;
    }

}
