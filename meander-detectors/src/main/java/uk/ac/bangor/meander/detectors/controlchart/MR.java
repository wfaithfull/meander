package uk.ac.bangor.meander.detectors.controlchart;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import uk.ac.bangor.meander.detectors.Pipe;
import uk.ac.bangor.meander.detectors.pipes.Threshold;
import uk.ac.bangor.meander.detectors.stats.IncrementalStatistics;
import uk.ac.bangor.meander.streams.StreamContext;

/**
 * Shewhart Moving Range control chart
 *
 * @author Will Faithfull
 */
public class MR {

    @Getter @AllArgsConstructor
    public static class MRState {
        double statistic, center, last;
        IncrementalStatistics statistics;
    }

    public static class MRReduction implements Pipe<Double, MRState> {

        protected double last      = 0;
        protected double statistic = 0;
        protected double sum;
        protected double center    = 0;

        protected IncrementalStatistics statistics = new IncrementalStatistics();

        @Setter
        protected boolean resetOnChangeDetected;

        public void update(Double input) {

            statistics.update(input);

            statistic = Math.abs(input - last);
            sum += statistic;

            if(statistics.getN() >= 2)
                center = sum / (statistics.getN() - 1);

            last = input;
        }

        @Override
        public void reset() {
            last = 0;
            statistic = 0;
            sum = 0;
            center = 0;
            statistics.reset();
        }

        @Override
        public MRState execute(Double value, StreamContext context) {
            update(value);
            return new MRState(statistic, center, last, statistics);
        }
    }

    public static class MRThreshold extends Threshold<MRState> {

        protected final static double D4_2 = 3.267;

        public MRThreshold() {
            super(Op.GT, MRThreshold::limit, (mr, ctx) -> mr.getStatistic());
        }

        private static Double limit(MRState mr, StreamContext ctx) {
            if (mr.getStatistics().getN() < 2)
                return Double.POSITIVE_INFINITY;

            return D4_2 * mr.getCenter();
        }
    }

    public static Pipe<Double, Boolean> detector(final boolean resetOnChangeDetected) {
        final MRReduction reduction = new MRReduction();

        return reduction
                .then(new MRThreshold())
                .then((value, context) -> {
                    if(resetOnChangeDetected && value) {
                        reduction.reset();
                    }

                    return value;
                });
    }

}
