package uk.ac.bangor.meander.detectors;

/**
 * @author Will Faithfull
 */
public class DetectorDecisionFunctionAdapter implements DecisionFunction {

    private Detector<Double> univariateDetector;

    public DetectorDecisionFunctionAdapter(Detector<Double> univariateDetector) {
        this.univariateDetector = univariateDetector;
    }

    @Override
    public boolean decide(Double statistic) {
        univariateDetector.update(statistic);
        return univariateDetector.isChangeDetected();
    }
}
