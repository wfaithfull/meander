package uk.ac.bangor.meander.detectors;

/**
 * @author Will Faithfull
 */
public class UnboxingDetectorAdapter extends AbstractMultivariateDetector {

    private Detector<double[]> detector;

    public UnboxingDetectorAdapter(Detector<double[]> detector) {
        this.detector = detector;
    }

    public void update(Double[] input) {
        double[] unboxed = new double[input.length];
        for(int i=0;i<input.length;i++) {
            unboxed[i] = input[i];
        }
        detector.update(unboxed);
    }

    @Override
    public boolean isChangeDetected() {
        return detector.isChangeDetected();
    }
}
