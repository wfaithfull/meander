package uk.ac.bangor.meander.detectors.pipes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.math3.distribution.FDistribution;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.apache.commons.math3.stat.correlation.Covariance;
import uk.ac.bangor.meander.detectors.CollectionUtils;
import uk.ac.bangor.meander.detectors.Pipe;
import uk.ac.bangor.meander.detectors.pipes.Distributions;
import uk.ac.bangor.meander.detectors.windowing.WindowPair;
import uk.ac.bangor.meander.streams.StreamContext;

/**
 * @author Will Faithfull
 */
public class Hotelling {

    public static class TsqReduction implements Pipe<Double[], Distributions.TsqState> {

        private static final int MAX_CONDITION = 10000;
        private WindowPair<double[]> windowPair;
        private FDistribution        fDistribution;

        private int df1,df2;

        public TsqReduction(WindowPair<double[]> windowPair) {
            this.windowPair = windowPair;
        }

        private double[] getDiagonal(RealMatrix matrix) {
            if(!matrix.isSquare())
                throw new RuntimeException("Matrix must be square to retrieve diagonal.");
            double[][] data = matrix.getData();
            double[] diagonal = new double[data.length];
            int col = 0;
            for(int i=0;i<data.length;i++) {
                diagonal[col] = data[i][col++];
            }
            return diagonal;
        }

        public boolean decide(Double tsq) {

            if(df1 <= 0 || df2 <= 0) {
                return false;
            }

            fDistribution = new FDistribution(df1, df2);

            double pst = 1-fDistribution.cumulativeProbability(tsq);

            return pst < 0.05;
        }

        @Override
        public boolean ready() {
            return windowPair.size() == windowPair.capacity();
        }

        @Override
        public Distributions.TsqState execute(Double[] value, StreamContext context) {

            windowPair.update(CollectionUtils.unbox(value));

            double[][] w1 = windowPair.getWindow1().getElements();
            double[][] w2 = windowPair.getWindow2().getElements();

            double m1 = w1.length;
            double m2 = w2.length;
            double n = w1[0].length;

            RealMatrix covW1 = new Covariance(w1).getCovarianceMatrix().scalarMultiply(m1);
            RealMatrix covW2 = new Covariance(w2).getCovarianceMatrix().scalarMultiply(m2);

            RealMatrix pooledCovariance = covW1.add(covW2).scalarMultiply(1/(m1 + m2 - 2));
            double tsq = (m1+m2-n-1)*m1*m2 / ((m1+m2)*n*(m1+m2-n-1));

            RealMatrix meanW1 = MatrixUtils.createRowRealMatrix(CollectionUtils.colMean(w1));
            RealMatrix meanW2 = MatrixUtils.createRowRealMatrix(CollectionUtils.colMean(w2));

            SingularValueDecomposition svd = new SingularValueDecomposition(pooledCovariance);
            double[] diag = getDiagonal(pooledCovariance);
            if(svd.getConditionNumber() > MAX_CONDITION) {
                // Regularisation
                pooledCovariance = MatrixUtils.createRealDiagonalMatrix(diag);
                svd = new SingularValueDecomposition(pooledCovariance);
            }

            RealMatrix inverseCovariance;
            if(CollectionUtils.min(diag) > 0.000001)
                inverseCovariance = svd.getSolver().getInverse();
            else
                inverseCovariance = MatrixUtils.createRealIdentityMatrix((int)n);

            double dist = meanW1.subtract(meanW2)
                    .multiply(inverseCovariance)
                    .multiply(meanW1.subtract(meanW2).transpose()).getEntry(0,0);

            tsq = tsq * dist;

            df1 = (int)n;
            df2 = (int)(m1 + m2 - n - 1);

            return new Distributions.TsqState(df1, df2, tsq);
        }
    }

    public static class Threshold implements Pipe<Double, Boolean> {

        private double threshold;

        public Threshold(double threshold) {
            this.threshold = threshold;
        }

        public Threshold() {
            this(0.05);
        }

        @Override
        public Boolean execute(Double value, StreamContext context) {
            return value < threshold;
        }
    }

    public static Pipe<Double[], Boolean> detector(int size) {
        return new TsqReduction(new WindowPair<>(size, size, double[].class))
                .then(new Distributions.FDistributedProbability())
                .then(new Threshold());
    }

    public static Pipe<Double[], Double> tsqReduction(int size) {
        return new TsqReduction(new WindowPair<>(size, size, double[].class))
                .then((value, context) -> value.getTsq());
    }

    public static Pipe<Double[], Double> fCumulativeProbability(int size) {
        return new TsqReduction(new WindowPair<>(size, size, double[].class))
                .then(new Distributions.FDistributedProbability());
    }

}