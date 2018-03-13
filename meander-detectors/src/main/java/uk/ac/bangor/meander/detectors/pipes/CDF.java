package uk.ac.bangor.meander.detectors.pipes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.distribution.FDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import uk.ac.bangor.meander.detectors.Pipe;
import uk.ac.bangor.meander.detectors.stats.IncrementalStatistics;
import uk.ac.bangor.meander.streams.StreamContext;

/**
 * Created by wfaithfull on 11/03/18.
 */
public class CDF {

    @Getter
    @AllArgsConstructor
    public static class FStatisticAndDegreesFreedom {
        private int df1,df2;
        private double statistic;
    }

    public static class FWithDF implements Pipe<FStatisticAndDegreesFreedom, Double> {

        private FDistribution fDistribution;

        @Override
        public Double execute(FStatisticAndDegreesFreedom value, StreamContext context) {
            if(value.getDf1() <= 0 || value.getDf2() <= 0) {
                return 1d;
            }

            fDistribution = new FDistribution(value.getDf1(), value.getDf2());

            double pst = 1-fDistribution.cumulativeProbability(value.getStatistic());
            return pst;
        }
    }

    public static class Inverse implements Pipe<Double, Double> {
        @Override
        public Double execute(Double value, StreamContext context) {
            return 1d - value;
        }
    }

    public static class F implements Pipe<Double, Double> {

        private final int df1;
        private final int df2;
        private FDistribution fDistribution;

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


    public static class ChiSquared implements Pipe<Double,Double> {

        ChiSquaredDistribution cdf;

        public double chiSq(Double statistic, int df) {
            if(cdf == null) {
                cdf = new ChiSquaredDistribution(df);
            }

            double cumulativeProbability = cdf.cumulativeProbability(statistic);

            return cumulativeProbability;
        }

        @Override
        public Double execute(Double value, StreamContext context) {
            return chiSq(value, context.getDimensionality());
        }
    }

    public static class Normal implements Pipe<Double,Double> {

        NormalDistribution cdf;
        IncrementalStatistics statistics = new IncrementalStatistics();

        @Override
        public Double execute(Double value, StreamContext context) {
            statistics.update(value);
            cdf = new NormalDistribution(statistics.mean(), statistics.var());

            double cumulativeProbability = cdf.cumulativeProbability(value);

            return cumulativeProbability;
        }
    }
}
