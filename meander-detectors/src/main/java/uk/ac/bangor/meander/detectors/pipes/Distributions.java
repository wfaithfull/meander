package uk.ac.bangor.meander.detectors.pipes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.math3.distribution.FDistribution;
import uk.ac.bangor.meander.detectors.Pipe;
import uk.ac.bangor.meander.streams.StreamContext;

/**
 * Created by wfaithfull on 11/03/18.
 */
public class Distributions {

    @Getter
    @AllArgsConstructor
    public static class TsqState {
        private int df1,df2;
        private double tsq;
    }

    public static class FDistributedProbability implements Pipe<TsqState, Double> {

        private FDistribution fDistribution;

        @Override
        public Double execute(TsqState value, StreamContext context) {
            if(value.getDf1() <= 0 || value.getDf2() <= 0) {
                return 1d;
            }

            fDistribution = new FDistribution(value.getDf1(), value.getDf2());

            double pst = 1-fDistribution.cumulativeProbability(value.getTsq());
            return pst;
        }
    }
}
