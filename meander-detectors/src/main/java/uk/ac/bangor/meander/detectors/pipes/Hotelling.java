package uk.ac.bangor.meander.detectors.pipes;

import org.apache.commons.math3.distribution.FDistribution;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.apache.commons.math3.stat.correlation.Covariance;
import uk.ac.bangor.meander.detectors.CollectionUtils;
import uk.ac.bangor.meander.detectors.Pipe;
import uk.ac.bangor.meander.detectors.windowing.WindowPair;
import uk.ac.bangor.meander.streams.StreamContext;

/**
 * @author Will Faithfull
 */
public class Hotelling {

    public static class TsqReduction implements Pipe<WindowPair<Double[]>, CDF.FStatisticAndDegreesFreedom> {

        private static final int MAX_CONDITION = 10000;
        private FDistribution        fDistribution;

        private int df1,df2;

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
        public CDF.FStatisticAndDegreesFreedom execute(WindowPair<Double[]> windowPair, StreamContext context) {

            double[][] w1 = CollectionUtils.unbox(windowPair.getWindow1().getElements());
            double[][] w2 = CollectionUtils.unbox(windowPair.getWindow2().getElements());

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

            return new CDF.FStatisticAndDegreesFreedom(df1, df2, tsq);
        }
    }



}