package uk.ac.bangor.meander.detectors;

import lombok.Getter;
import org.apache.commons.math3.util.FastMath;
import uk.ac.bangor.meander.detectors.windowing.FixedWindowPair;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Will Faithfull
 */
public class KL extends AbstractKMeansQuantizingDetector {

    @Getter
    private double statistic;

    public KL(FixedWindowPair<double[]> windowPair, int K) {
        super(windowPair, K);
    }

    private double logK = Math.log(K);
    private static double eps = 0.00001;

    protected boolean change(double[] p1, double[] p2) {
        addEps(p1);
        addEps(p2);

        double st = klDivergence(p1,p2);
        this.statistic = st;

        double sumlogP1 = 0;
        for(int i=0;i<p1.length;i++) {
            sumlogP1 += Math.log(p1[i]);
        }

        return st > -logK - (sumlogP1/K);
    }

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

            divergence += p2[i] * FastMath.log(p2[i]/p1[i]);
        }
        return divergence;
    }
}
