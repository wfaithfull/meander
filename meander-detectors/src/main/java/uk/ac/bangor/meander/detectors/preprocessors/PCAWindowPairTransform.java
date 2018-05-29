package uk.ac.bangor.meander.detectors.preprocessors;

import Jama.Matrix;
import uk.ac.bangor.meander.detectors.CollectionUtils;
import uk.ac.bangor.meander.detectors.Pipe;
import uk.ac.bangor.meander.detectors.stats.PrincipalComponents;
import uk.ac.bangor.meander.detectors.windowing.WindowPairPipe;
import uk.ac.bangor.meander.detectors.windowing.support.FixedWindow;
import uk.ac.bangor.meander.detectors.windowing.support.WindowPair;
import uk.ac.bangor.meander.streams.StreamContext;

/**
 * Pipe which computes a PCA transformation using W1, and then applies that transformation to W1 and W2.
 * <p>
 * The hypothesis is that assuming W1 and W2 are drawn from the same distribution, the transformation will not make
 * much of a difference. If they differ, those differences will be accentuated in the principal component space.
 *
 * @author Will Faithfull
 */
public class PCAWindowPairTransform<T extends WindowPair<Double[]>> implements Pipe<T, T> {

    FixedWindow<Double[]> w1;
    FixedWindow<Double[]> w2;

    public PCAWindowPairTransform() {
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
        return new WindowPairPipe(size).then(new PCAWindowPairTransform());
    }

}