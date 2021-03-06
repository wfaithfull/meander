import lombok.extern.java.Log;
import uk.ac.bangor.meander.detectors.JFreeChartReporter;
import uk.ac.bangor.meander.detectors.Pipe;
import uk.ac.bangor.meander.detectors.ReportPipe;
import uk.ac.bangor.meander.detectors.Threshold;
import uk.ac.bangor.meander.detectors.clusterers.KMeansStreamClusterer;
import uk.ac.bangor.meander.detectors.controlchart.pipes.MovingRange;
import uk.ac.bangor.meander.detectors.controlchart.pipes.MovingRangeThreshold;
import uk.ac.bangor.meander.detectors.ensemble.pipes.DecayingMajority;
import uk.ac.bangor.meander.detectors.ensemble.pipes.SubspaceEnsemble;
import uk.ac.bangor.meander.detectors.ensemble.support.LogisticDecayFunction;
import uk.ac.bangor.meander.detectors.m2d.pipes.*;
import uk.ac.bangor.meander.detectors.m2d.support.KLState;
import uk.ac.bangor.meander.detectors.preprocessors.pipes.PCAFeatureSelector;
import uk.ac.bangor.meander.detectors.preprocessors.support.PCAExtractionOptions;
import uk.ac.bangor.meander.detectors.stats.cdf.pipes.ChiSquared;
import uk.ac.bangor.meander.detectors.stats.cdf.pipes.FWithDF;
import uk.ac.bangor.meander.detectors.windowing.pipes.WindowPairClustering;
import uk.ac.bangor.meander.detectors.windowing.pipes.WindowPairPipe;
import uk.ac.bangor.meander.detectors.windowing.support.ClusteringWindowPair;
import uk.ac.bangor.meander.detectors.windowing.support.WindowPair;
import uk.ac.bangor.meander.evaluators.Evaluation;
import uk.ac.bangor.meander.evaluators.Evaluator;
import uk.ac.bangor.meander.evaluators.SequenceEvaluator;
import uk.ac.bangor.meander.streams.ChangeStreamBuilder;
import uk.ac.bangor.meander.transitions.AbruptTransition;
import uk.ac.bangor.meander.transitions.LogisticTransition;

import java.io.IOException;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Will Faithfull
 */
@Log
public class Evaluations {

    public static void main(String[] args) throws IOException {

        ChangeStreamBuilder arffStream = ChangeStreamBuilder
                .fromArff("abalone.arff")
                .withUniformPriors().fromStart()
                .withPriors(1.0, 0.0, 0.0).transition(new AbruptTransition(2500))
                .withPriors(0.0, 1.0, 0.0).transition(new LogisticTransition(5000, 5100))
                .withPriors(0.0, 0.0, 1.0).transition(new AbruptTransition(7500));

        int W = 25;

        JFreeChartReporter reporter = new JFreeChartReporter("Subspace");
        Pipe<Double[], Boolean> spll = new SPLLDetector(new WindowPair<double[]>(W, W, double[].class), 5)
                .then(new ChiSquared(), reporter::statistic)
                .then(Threshold.lessThan(0.05).reportThreshold(reporter::lcl));

        Pipe<Double[], Boolean> detector = SPLL2.detector(W, 3);

        Pipe kl = new WindowPairClustering(W, () -> new KMeansStreamClusterer(3))
                .then(new ClusteringWindowPair.Distribution())
                .then(new KL())
                .then(new ReportPipe<>(reporter::statistic, KLState::getStatistic))
                .then(new Threshold<>(Threshold.Op.GT, new KLLikelihoodRatio(), new KLStateStatistic())
                        .reportThreshold(reporter::ucl));

        Supplier<Pipe<Double, Boolean>> mrSupplier = () -> new MovingRange().then(new MovingRangeThreshold());
        Pipe<Double[], Boolean> subspace =
                new PCAFeatureSelector(true)
                        .then(new SubspaceEnsemble(mrSupplier)
                .then(new DecayingMajority(new LogisticDecayFunction()))
                .then(new ReportPipe<>(reporter::statistic, Function.identity()))
                                .then(Threshold.greaterThan(.25).reportThreshold(reporter::ucl)));

        Pipe<Double[], Boolean> hotelling =
                new PCAFeatureSelector(.2, PCAExtractionOptions.KEEP_LEAST_VARIANT, true).then(
                        new WindowPairPipe(100)
                                .then(new TSquared().then(new ReportPipe<>(reporter::statistic, f -> f.getStatistic())))
                                .then(new FWithDF().complementary())
                                .then(Threshold.lessThan(0.05)));

//        evaluate(new ClusteringWindowPairPipe(50,
//                        () -> new SlowApacheKMeansClusterer(50, 3),
//                        (t,h) -> new FixedTailWindowPair<>(t, h))
//                .then(new PCA.WindowPairTransform(PCA.ExtractionOptions.KEEP_LEAST_VARIANT, 0.1))
//                .then(new SPLL2.SPLLReduction())
//                        .then(new CDF.ChiSquared(10).then(new CDF.Folded()))
//                // Threshold inverts the cumulative probability
//                .then(Threshold.lessThan(0.05)
//                        .report(reporter::lcl, reporter::statistic)),
//                arffStream);

        evaluate(hotelling, arffStream);
        /*evaluate(new SubspaceEnsemble(() -> Detectors.Univariate.movingRangeChart())
                .then(new DecayingMajority(new LinearDecayFunction(50)))
                .then(Threshold.greaterThan(.25).report(reporter::ucl, reporter::statistic))
                .then(new ResetOnChangeDetected()), arffStream);*/
    }

    private static void evaluate(Pipe<Double[],Boolean> detector, ChangeStreamBuilder arffStream) {

        Evaluator evaluator = new SequenceEvaluator();
        Evaluation evaluation = evaluator.evaluate(detector, arffStream, 10000, 1);
        log.info(evaluation.toString());
    }

}
