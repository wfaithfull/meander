package uk.ac.bangor.meander.detectors.preprocessors;

import Jama.Matrix;
import Jama.SingularValueDecomposition;
import uk.ac.bangor.meander.detectors.CollectionUtils;
import uk.ac.bangor.meander.detectors.Pipe;
import uk.ac.bangor.meander.detectors.stats.MultivariateIncrementalStatistics;
import uk.ac.bangor.meander.detectors.stats.PrincipalComponents;
import uk.ac.bangor.meander.detectors.windowing.FixedWindow;
import uk.ac.bangor.meander.detectors.windowing.WindowPair;
import uk.ac.bangor.meander.detectors.windowing.WindowPairPipe;
import uk.ac.bangor.meander.streams.StreamContext;

/**
 * @author Will Faithfull
 */
public class PCA {

    /**
     * Pipe which computes a PCA transformation using W1, and then applies that transformation to W1 and W2.
     * <p>
     * The hypothesis is that assuming W1 and W2 are drawn from the same distribution, the transformation will not make
     * much of a difference. If they differ, those differences will be accentuated in the principal component space.
     *
     * @author Will Faithfull
     */
    public static class WindowPairTransform implements Pipe<WindowPair<Double[]>, WindowPair<Double[]>> {

        FixedWindow<Double[]> w1;
        FixedWindow<Double[]> w2;

        @Override
        public WindowPair<Double[]> execute(WindowPair<Double[]> windowPair, StreamContext context) {
            PrincipalComponents principalComponents = new PrincipalComponents(windowPair.getWindow1().getElements());

            w1 = new FixedWindow<>(windowPair.getWindow1().size(), Double[].class);
            w2 = new FixedWindow<>(windowPair.getWindow2().size(), Double[].class);

            // Transform W1, transform W2 with respect to W1's parameters.
            Matrix scoresW1 = principalComponents.getScores();
            Matrix scoresW2 = principalComponents.transform(new Matrix(CollectionUtils.unbox(windowPair.getWindow2().getElements())));

            for (int i = 0; i < scoresW1.getRowDimension(); i++) {
                w1.add(CollectionUtils.box(scoresW1.getArray()[i]));
                w2.add(CollectionUtils.box(scoresW2.getArray()[i]));
            }

            return new WindowPair<>(w1, w2);
        }

        public static Pipe<Double[], WindowPair<Double[]>> fromDouble(int size) {
            return new WindowPairPipe(size).then(new WindowPairTransform());
        }

    }

    public static class IPCA implements Pipe<Double[], Double[]> {

        private MultivariateIncrementalStatistics incrementalStatistics;

        public IPCA(Matrix X) {
            incrementalStatistics = new MultivariateIncrementalStatistics(X.getColumnDimension());
        }

        public IPCA(int features) {
            incrementalStatistics = new MultivariateIncrementalStatistics(features);
        }

        public void update(Matrix X) {

            for (double[] row : X.getArray()) {
                incrementalStatistics.update(row);
            }

            for (double[] row : X.getArray()) {
                for (int i = 0; i < row.length; i++) {
                    row[i] -= incrementalStatistics.getMean()[i];
                }
            }

            Math.sqrt(incrementalStatistics.getN() * X.getRowDimension());

            SingularValueDecomposition decomposition = new SingularValueDecomposition(X);
        }

        @Override
        public Double[] execute(Double[] value, StreamContext context) {
            return new Double[0];
        }
    }


}
