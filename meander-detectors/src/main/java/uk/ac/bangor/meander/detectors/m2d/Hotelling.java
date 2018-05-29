package uk.ac.bangor.meander.detectors.m2d;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.apache.commons.math3.stat.correlation.Covariance;
import uk.ac.bangor.meander.detectors.CollectionUtils;
import uk.ac.bangor.meander.detectors.Pipe;
import uk.ac.bangor.meander.detectors.stats.cdf.support.FStatisticAndDegreesFreedom;
import uk.ac.bangor.meander.detectors.windowing.support.WindowPair;
import uk.ac.bangor.meander.streams.StreamContext;

/**
 * @author Will Faithfull
 */
public class Hotelling {

    public static class TsqReduction implements Pipe<WindowPair<Double[]>, FStatisticAndDegreesFreedom> {

        private static final int MAX_CONDITION = 10000;
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

        @Override
        public FStatisticAndDegreesFreedom execute(WindowPair<Double[]> windowPair, StreamContext context) {

            double[][] w1 = CollectionUtils.unbox(windowPair.getTail().getElements());
            double[][] w2 = CollectionUtils.unbox(windowPair.getHead().getElements());

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

            return new FStatisticAndDegreesFreedom(df1, df2, tsq);
        }
    }



}