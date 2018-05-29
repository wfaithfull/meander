package uk.ac.bangor.meander.detectors.m2d.pipes;

import org.apache.commons.math3.util.FastMath;
import uk.ac.bangor.meander.detectors.Pipe;
import uk.ac.bangor.meander.detectors.m2d.support.KLState;
import uk.ac.bangor.meander.detectors.windowing.support.DistributionPair;
import uk.ac.bangor.meander.streams.StreamContext;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Will Faithfull
 */
public class KL implements Pipe<DistributionPair, KLState> {

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

            divergence += p1[i] * FastMath.log(p1[i] / p2[i]);
        }
        return divergence;
    }

    public KLState execute(DistributionPair value, StreamContext context) {
        double[] p = value.getP();
        double[] q = value.getQ();
        int K = p.length;

        addEps(p);
        addEps(q);

        double statistic = Math.abs(klDivergence(p, q));

        return new KLState(statistic, K, p, q);
    }
}
