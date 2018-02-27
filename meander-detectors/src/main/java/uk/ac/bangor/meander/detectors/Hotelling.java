package uk.ac.bangor.meander.detectors;

import org.apache.commons.math3.distribution.FDistribution;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.apache.commons.math3.stat.correlation.Covariance;
import uk.ac.bangor.meander.detectors.windowing.WindowPair;

/**
 * @author Will Faithfull
 */
public class Hotelling extends AbstractMultivariateDetector {

    private static final int MAX_CONDITION = 10000;
    private WindowPair<double[]> windowPair;
    private FDistribution        fDistribution;
    private boolean              change;

    public Hotelling(WindowPair<double[]> windowPair) {
        this.windowPair = windowPair;
    }

    @Override
    public void update(Double[] input) {
        double[] unboxed = new double[input.length];
        for(int i=0;i<input.length;i++) {
            unboxed[i] = input[i];
        }
        update(unboxed);
    }
    
    public void update(double[] input) {

        windowPair.update(input);

        if(windowPair.size() != windowPair.capacity())
            return;

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

        double dist = meanW1.multiply(inverseCovariance).multiply(meanW2.transpose()).getEntry(0,0);

        tsq = tsq * dist;

        int df1 = (int)n;
        int df2 = (int)(m1 + m2 - n - 1);

        if(fDistribution == null)
            fDistribution = new FDistribution(df1, df2);

        double pst = 1-fDistribution.cumulativeProbability(tsq);

        this.change = pst < 0.05;
    }

    @Override
    public boolean isChangeDetected() {
        return change;
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
}