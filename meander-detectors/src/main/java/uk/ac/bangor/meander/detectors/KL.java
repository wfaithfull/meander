package uk.ac.bangor.meander.detectors;

import lombok.Getter;
import lombok.extern.java.Log;
import org.apache.commons.math3.util.FastMath;
import uk.ac.bangor.meander.detectors.clusterers.KMeansStreamClusterer;
import uk.ac.bangor.meander.detectors.windowing.WindowPair;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Will Faithfull
 */
@Log
public class KL extends AbstractClusteringQuantizingDetector implements ReductionFunction, DecisionFunction {

    @Getter
    private double statistic;

    private int K;

    private PrintWriter file;

    public KL(int size, int K) {
        super(size, () -> new KMeansStreamClusterer(K));
        this.K = K;
        this.logK = Math.log(K);

        try {
            file = new PrintWriter("kl.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private double logK;
    private static double eps = 0.00001;
    private boolean change;

    protected boolean change(double[] p1, double[] p2) {
        addEps(p1);
        addEps(p2);

        double st = klDivergence(p1,p2);
        this.statistic = st;


        double sumlogP1 = 0;
        for(int i=0;i<p1.length;i++) {
            sumlogP1 += Math.log(p1[i]);
        }

        file.write("" + statistic + ", " + (-logK - (sumlogP1/K)) + ";\n");
        return st > -logK - (sumlogP1/K);
    }

    @Override
    public void update(double[] input) {
        super.update(input);
        statistic = reduce(input);
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

            divergence += p1[i] * FastMath.log(p2[i]/p1[i]);
        }
        return divergence;
    }

    @Override
    public boolean isChangeDetected() {
        return change;
    }

    @Override
    public boolean decide(Double statistic) {
        return ;
    }

    @Override
    public double reduce(Double[] example) {
        update(example);
        addEps(p1);
        addEps(p2);

        double st = klDivergence(p1,p2);
        this.statistic = st;
    }
}
