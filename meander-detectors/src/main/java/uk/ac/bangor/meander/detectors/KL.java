package uk.ac.bangor.meander.detectors;

import lombok.extern.java.Log;
import org.apache.commons.math3.util.FastMath;
import uk.ac.bangor.meander.detectors.clusterers.KMeansStreamClusterer;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Will Faithfull
 */
@Log
public class KL extends AbstractFunctionalDetector {

    private int K;

    private WindowPairClusteringQuantizer quantizer;
    private double threshold, statistic;

    public KL(int size, int K) {
        quantizer = new WindowPairClusteringQuantizer(size, () -> new KMeansStreamClusterer(K));
        this.K = K;
        this.logK = Math.log(K);
    }

    private double logK;
    private static double eps = 0.00001;

    private static ThreadLocalRandom random = ThreadLocalRandom.current();

    private static void addEps(double[] input) {
        for(int i=0;i<input.length;i++) {
            double epsilon = random.nextDouble() * eps;
            input[i] = epsilon+input[i];
        }
    }

    private static double klDivergence(double[] p1, double[] p2) {

        double divergence = 0.0;
        for (int i = 0; i < p1.length; ++i) {
            if(p1[i] == 0d || p2[i] == 0d)
                continue;

            divergence += p1[i] * FastMath.log(p2[i]/p1[i]);
        }
        return divergence;
    }

    @Override
    public boolean ready() {
        return quantizer.getW1().isAtFullCapacity();
    }

    @Override
    public double reduce(Double[] example) {
        quantizer.update(example);
        double[] p = quantizer.getP();
        double[] q = quantizer.getQ();

        if(!ready()) {
            return 0;
        }

        addEps(p);
        addEps(q);

        statistic = klDivergence(p,q);

        return statistic;
    }

    public double getThreshold() {

        if(!ready()) {
            return 0;
        }

        double sumlogP = 0;
        for(int i=0;i<quantizer.getP().length;i++) {
            sumlogP += Math.log(quantizer.getP()[i]);
        }

        return -logK - (sumlogP/K);
    }

    @Override
    public boolean decide(Double statistic) {
        threshold = getThreshold();
        return statistic > threshold;
    }

    @Override
    public State getState() {
        return State.statisticAndThreshold(statistic, threshold);
    }
}
