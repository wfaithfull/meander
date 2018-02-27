import lombok.extern.java.Log;
import uk.ac.bangor.meander.detectors.*;
import uk.ac.bangor.meander.detectors.windowing.WindowPair;
import uk.ac.bangor.meander.evaluators.BasicEvaluator;
import uk.ac.bangor.meander.evaluators.Evaluation;
import uk.ac.bangor.meander.evaluators.Evaluator;
import uk.ac.bangor.meander.streams.ChangeStreamBuilder;
import uk.ac.bangor.meander.streams.Example;
import uk.ac.bangor.meander.transitions.AbruptTransition;

import java.io.IOException;
import java.util.stream.Stream;

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

        Detector<Double[]> kl = new UnboxingDetectorAdapter(new KL(W, 3));

        evaluate(kl, arffStream);

        /*
        SPLL spll =  new SPLL(new WindowPair<>(W,W, double[].class), 3);
        Detector<Double[]> detector = new FunctionalDetector(spll, spll, n -> n >= 100);
        evaluate(detector, arffStream);

        spll =  new SPLL(new WindowPair<>(W,W, double[].class), 3);
        Detector<Double[]> detector2 = new FunctionalDetector(spll, new DetectorDecisionFunctionAdapter(MoaDetectorAdapter.cusum()), n -> n >= 100);
        evaluate(detector2, arffStream);

        spll =  new SPLL(new WindowPair<>(W,W, double[].class), 3);
        Detector<Double[]> detector3 = new FunctionalDetector(spll, new DetectorDecisionFunctionAdapter(MoaDetectorAdapter.pageHinkley()), n -> n >= 100);
        evaluate(detector3, arffStream);*/
    }

    private static void evaluate(Detector<Double[]> detector, ChangeStreamBuilder arffStream) {

        Evaluator evaluator = new BasicEvaluator(50);
        Evaluation evaluation = evaluator.evaluate(detector, arffStream, 10000, 1);
        log.info(evaluation.toString());
    }



}
