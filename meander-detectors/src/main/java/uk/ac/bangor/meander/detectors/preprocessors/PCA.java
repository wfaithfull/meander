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
    public static class WindowPairTransform<T extends WindowPair<Double[]>> implements Pipe<T, T> {

        FixedWindow<Double[]> w1;
        FixedWindow<Double[]> w2;

        public WindowPairTransform() {
        }

        @Override
        public T execute(T windowPair, StreamContext context) {
            if (!context.getCache().containsKey(PrincipalComponents.class)) {
                context.getCache().put(PrincipalComponents.class, new PrincipalComponents(windowPair.getTail().getElements()));
            }

            PrincipalComponents principalComponents = (PrincipalComponents) context.getCache().get(PrincipalComponents.class);

            // Transform W1, transform W2 with respect to W1's parameters.
            Matrix scoresW1 = principalComponents.getScores();
            Matrix scoresW2 = principalComponents.transform(new Matrix(CollectionUtils.unbox(windowPair.getHead().getElements())));

            w1 = new FixedWindow<>(windowPair.getTail().size(), Double[].class);
            w2 = new FixedWindow<>(windowPair.getHead().size(), Double[].class);

            for (int i = 0; i < scoresW1.getRowDimension(); i++) {
                w1.add(CollectionUtils.box(scoresW1.getArray()[i]));
                w2.add(CollectionUtils.box(scoresW2.getArray()[i]));
            }

            windowPair.setTail(w1);
            windowPair.setHead(w2);

            return windowPair;
        }

        public static Pipe<Double[], WindowPair<Double[]>> fromDouble(int size) {
            return new WindowPairPipe(size).then(new WindowPairTransform());
        }

    }

    public static class PCAFeatureSelector implements Pipe<Double[], Double[]> {

        public PCAFeatureSelector(boolean transform) {
            this(.1, ExtractionOptions.KEEP_LEAST_VARIANT, transform);
        }

        public PCAFeatureSelector(double percent, boolean transform) {
            this(percent, ExtractionOptions.KEEP_LEAST_VARIANT, transform);
        }

        public PCAFeatureSelector(double percent, ExtractionOptions options, boolean transform) {
            this.percent = percent;
            this.options = options;
            this.transform = transform;
        }

        private FixedWindow<Double[]> rawWindow;
        private double                percent;
        private ExtractionOptions     options;
        private boolean               transform;

        @Override
        public Double[] execute(Double[] value, StreamContext context) {

            if (rawWindow == null) {
                rawWindow = new FixedWindow<>(value.length, Double[].class);
            }

            if (!rawWindow.isAtFullCapacity()) {
                rawWindow.update(value);
                if (!rawWindow.isAtFullCapacity()) {
                    throw new NotReadyException(this);
                }
            }

            if (!context.getCache().containsKey(PrincipalComponents.class)) {
                context.getCache().put(PrincipalComponents.class, new PrincipalComponents(rawWindow.getElements()));
            }

            PrincipalComponents principalComponents = (PrincipalComponents) context.getCache().get(PrincipalComponents.class);

            int[] permutation;
            if (options == ExtractionOptions.KEEP_LEAST_VARIANT) {
                permutation = principalComponents.getBottom(percent);
            } else {
                permutation = principalComponents.getTop(percent);
            }

            boolean[] keep = principalComponents.permutationToKeep(permutation);
            Double[] trimmed = new Double[permutation.length];

            Matrix transformed = null;

            int j = 0;
            for (int i = 0; i < keep.length; i++) {
                if (keep[i]) {
                    if (transform) {
                        if (transformed == null) {
                            transformed = principalComponents.transform(new Matrix(CollectionUtils.unbox(value), 1));
                        }
                        trimmed[j] = transformed.get(0, j);
                    } else {
                        trimmed[j] = value[i];
                    }
                    j++;
                }
            }

            context.setDimensionality(permutation.length);

            return trimmed;
        }

        @Override
        public boolean ready() {
            return rawWindow.isAtFullCapacity();
        }
    }

    public enum ExtractionOptions {
        KEEP_MOST_VARIANT,
        KEEP_LEAST_VARIANT
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
