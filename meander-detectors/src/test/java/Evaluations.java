import lombok.extern.java.Log;
import uk.ac.bangor.meander.detectors.JFreeChartReporter;
import uk.ac.bangor.meander.detectors.Pipe;
import uk.ac.bangor.meander.detectors.ReportPipe;
import uk.ac.bangor.meander.detectors.clusterers.KMeansStreamClusterer;
import uk.ac.bangor.meander.detectors.controlchart.MR;
import uk.ac.bangor.meander.detectors.ensemble.DecayingMajority;
import uk.ac.bangor.meander.detectors.ensemble.LogisticDecayFunction;
import uk.ac.bangor.meander.detectors.ensemble.SubspaceEnsemble;
import uk.ac.bangor.meander.detectors.pipes.*;
import uk.ac.bangor.meander.detectors.preprocessors.WindowPairPCATransform;
import uk.ac.bangor.meander.detectors.windowing.ClusteringPair;
import uk.ac.bangor.meander.detectors.windowing.WindowPair;
import uk.ac.bangor.meander.detectors.windowing.WindowPairClusteringQuantizer;
import uk.ac.bangor.meander.detectors.windowing.WindowPairPipe;
import uk.ac.bangor.meander.evaluators.BasicEvaluator;
import uk.ac.bangor.meander.evaluators.Evaluation;
import uk.ac.bangor.meander.evaluators.Evaluator;
import uk.ac.bangor.meander.streams.ChangeStreamBuilder;
import uk.ac.bangor.meander.transitions.AbruptTransition;

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
                .withUniformClassMixture().fromStart()
                .withClassMixture(1.0, 0.0, 0.0).transition(new AbruptTransition(2500))
                .withClassMixture(0.0, 1.0, 0.0).transition(new AbruptTransition(5000))
                .withClassMixture(0.0, 0.0, 1.0).transition(new AbruptTransition(7500));

        int W = 25;

        JFreeChartReporter reporter = new JFreeChartReporter("Subspace");
        Pipe<Double[], Boolean> spll = new SPLL(new WindowPair<double[]>(W, W, double[].class),5)
                .then(new CDF.ChiSquared(), reporter::statistic)
                .then(Threshold.lessThan(0.05).reportThreshold(reporter::lcl));

        Pipe<Double[], Boolean> detector = SPLL2.detector(W, 3);

        Pipe kl = new WindowPairClusteringQuantizer(W, () -> new KMeansStreamClusterer(3))
                .then(new ClusteringPair.Distribution())
                .then(new KL.KLReduction())
                .then(new ReportPipe<>(reporter::statistic, KL.KLState::getStatistic))
                .then(new Threshold<>(Threshold.Op.GT, new KL.LikelihoodRatioThreshold(), new KL.KLStateStatistic())
                        .reportThreshold(reporter::ucl));

        Supplier<Pipe<Double, Boolean>> mrSupplier = () -> new MR.MRReduction().then(new MR.MRThreshold());
        Pipe<Double[], Boolean> subspace = new SubspaceEnsemble(mrSupplier)
                .then(new DecayingMajority(new LogisticDecayFunction()))
                .then(new ReportPipe<>(reporter::statistic, Function.identity()))
                .then(Threshold.greaterThan(.25).reportThreshold(reporter::ucl));

        Pipe<Double[], Boolean> hotelling = new WindowPairPipe(100)
                .then(new WindowPairPCATransform())
                .then(new Hotelling.TsqReduction())
                .then(new CDF.FWithDF().then(new CDF.Inverse()))
                .then(Threshold.lessThan(0.05));

        evaluate(new WindowPairPipe(100)
                .then(new WindowPairPCATransform())
                .then(new Hotelling.TsqReduction())
                .then(new CDF.FWithDF().then(new CDF.Inverse()))
                // Threshold inverts the cumulative probability
                .then(Threshold.lessThan(0.05)
                        .report(reporter::lcl, reporter::statistic))
                .then(new ResetOnChangeDetected()), arffStream);
        /*evaluate(new SubspaceEnsemble(() -> Detectors.Univariate.movingRangeChart())
                .then(new DecayingMajority(new LinearDecayFunction(50)))
                .then(Threshold.greaterThan(.25).report(reporter::ucl, reporter::statistic))
                .then(new ResetOnChangeDetected()), arffStream);*/
    }

    private static void evaluate(Pipe<Double[],Boolean> detector, ChangeStreamBuilder arffStream) {

        Evaluator evaluator = new BasicEvaluator();
        Evaluation evaluation = evaluator.evaluate(detector, arffStream, 10000, 1);
        log.info(evaluation.toString());
    }

}
