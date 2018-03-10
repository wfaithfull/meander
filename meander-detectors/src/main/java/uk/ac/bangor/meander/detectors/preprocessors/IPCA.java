package uk.ac.bangor.meander.detectors.preprocessors;

import Jama.Matrix;
import Jama.SingularValueDecomposition;
import uk.ac.bangor.meander.detectors.stats.MultivariateIncrementalStatistics;

/**
 * @author Will Faithfull
 */
public class IPCA {

    private MultivariateIncrementalStatistics incrementalStatistics;

    public IPCA(Matrix X) {
        incrementalStatistics = new MultivariateIncrementalStatistics(X.getColumnDimension());
    }

    public IPCA(int features) {
        incrementalStatistics = new MultivariateIncrementalStatistics(features);
    }

    public void update(Matrix X) {

        for(double[] row : X.getArray()) {
            incrementalStatistics.update(row);
        }

        for(double[] row : X.getArray()) {
            for(int i=0;i<row.length;i++) {
                row[i] -= incrementalStatistics.getMean()[i];
            }
        }

        Math.sqrt(incrementalStatistics.getN() * X.getRowDimension());

        SingularValueDecomposition decomposition = new SingularValueDecomposition(X);
    }

}
