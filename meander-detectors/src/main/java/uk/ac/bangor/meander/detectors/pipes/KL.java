package uk.ac.bangor.meander.detectors.pipes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.java.Log;
import org.apache.commons.math3.util.FastMath;
import uk.ac.bangor.meander.detectors.Pipe;
import uk.ac.bangor.meander.detectors.windowing.WindowPairClusteringQuantizer;
import uk.ac.bangor.meander.detectors.clusterers.KMeansStreamClusterer;
import uk.ac.bangor.meander.streams.StreamContext;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Will Faithfull
 */
@Log
public class KL  {

    @Getter @AllArgsConstructor
    public static class KLState {
        private double statistic;
        private int K;
        private double[] p;
        private double[] q;
    }

    public static class KLReduction implements Pipe<Double[], KLState> {

        private int K;

        private WindowPairClusteringQuantizer quantizer;

        public KLReduction(int size, int K) {
            quantizer = new WindowPairClusteringQuantizer(size, () -> new KMeansStreamClusterer(K));
            this.K = K;
        }

        private static double eps = 0.00001;

        private static ThreadLocalRandom random = ThreadLocalRandom.current();

        private static void addEps(double[] input) {
            for (int i = 0; i < input.length; i++) {
                double epsilon = random.nextDouble() * eps;
                input[i] = epsilon + input[i];
            }
        }

        private static double klDivergence(double[] p1, double[] p2) {

            double divergence = 0.0;
            for (int i = 0; i < p1.length; ++i) {
                if (p1[i] == 0d || p2[i] == 0d)
                    continue;

                divergence += p1[i] * FastMath.log(p2[i] / p1[i]);
            }
            return divergence;
        }

        public boolean ready() {
            return quantizer.getTail().isAtFullCapacity();
        }

        public KLState execute(Double[] value, StreamContext context) {
            quantizer.update(value);
            double[] p = quantizer.getP();
            double[] q = quantizer.getQ();

            addEps(p);
            addEps(q);

            double statistic = klDivergence(p, q);

            return new KLState(statistic, K, p, q);
        }
    }

    public static class LikelihoodRatioThreshold implements Pipe<KLState, Double> {
        @Override
        public Double execute(KLState value, StreamContext context) {

            double sumlogP = 0;
            for (int i = 0; i < value.getP().length; i++) {
                sumlogP += Math.log(value.getP()[i]);
            }

            return -Math.log(value.getK()) - (sumlogP / value.getK());
        }
    }

    public static class KLStateStatistic implements Pipe<KLState, Double> {
        @Override
        public Double execute(KLState value, StreamContext context) {
            return value.getStatistic();
        }
    }

    public static Pipe<Double[], Boolean> detector(int size, int K) {
        return new KLReduction(size, K)
                .then(new Threshold<>(Threshold.Op.GT, new LikelihoodRatioThreshold(), new KLStateStatistic()));
    }

    public static Pipe<Double[], Double> reduction(int size, int K) {
        return new KLReduction(size, K)
                .then((value, context) -> value.getStatistic());
    }

}
