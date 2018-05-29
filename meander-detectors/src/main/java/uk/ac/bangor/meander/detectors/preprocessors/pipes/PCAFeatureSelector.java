package uk.ac.bangor.meander.detectors.preprocessors.pipes;

import Jama.Matrix;
import uk.ac.bangor.meander.detectors.CollectionUtils;
import uk.ac.bangor.meander.detectors.Pipe;
import uk.ac.bangor.meander.detectors.preprocessors.support.PCAExtractionOptions;
import uk.ac.bangor.meander.detectors.stats.support.PrincipalComponents;
import uk.ac.bangor.meander.detectors.windowing.support.FixedWindow;
import uk.ac.bangor.meander.streams.StreamContext;

public class PCAFeatureSelector implements Pipe<Double[], Double[]> {

    public PCAFeatureSelector(boolean transform) {
        this(.1, PCAExtractionOptions.KEEP_LEAST_VARIANT, transform);
    }

    public PCAFeatureSelector(double percent, boolean transform) {
        this(percent, PCAExtractionOptions.KEEP_LEAST_VARIANT, transform);
    }

    public PCAFeatureSelector(double percent, PCAExtractionOptions options, boolean transform) {
        this.percent = percent;
        this.options = options;
        this.transform = transform;
    }

    private FixedWindow<Double[]> rawWindow;
    private double                percent;
    private PCAExtractionOptions  options;
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
        if (options == PCAExtractionOptions.KEEP_LEAST_VARIANT) {
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