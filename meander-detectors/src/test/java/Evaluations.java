import lombok.extern.java.Log;
import uk.ac.bangor.meander.detectors.*;
import uk.ac.bangor.meander.detectors.windowing.FixedWindowPair;
import uk.ac.bangor.meander.evaluators.BasicEvaluator;
import uk.ac.bangor.meander.evaluators.Evaluation;
import uk.ac.bangor.meander.evaluators.Evaluator;
import uk.ac.bangor.meander.streams.ChangeStreamBuilder;
import uk.ac.bangor.meander.transitions.AbruptTransition;

import java.io.IOException;

/**
 * @author Will Faithfull
 */
@Log
public class Evaluations {

    public static void main(String[] args) throws IOException {

        ChangeStreamBuilder arffStream = ChangeStreamBuilder
                .fromArff("abalone.arff")
                .withUniformClassMixture().fromStart()
                .withClassMixture(1.0, 0.0, 0.0).transition(new AbruptTransition(500))
                .withClassMixture(0.0, 1.0, 0.0).transition(new AbruptTransition(5000))
                .withClassMixture(0.0, 0.0, 1.0).transition(new AbruptTransition(7500));

        SPLL spll =  new SPLL(new FixedWindowPair<>(50,50, double[].class), 3);

        Detector<Double[]> detector = new FunctionalDetector(spll, spll, n -> n >= 100);

        Evaluator evaluator = new BasicEvaluator();
        Evaluation evaluation = evaluator.evaluate(detector, arffStream, 10000, 10);

        log.info(evaluation.toString());

        SPLL spll2 =  new SPLL(new FixedWindowPair<>(50,50, double[].class), 3);

        Detector<Double[]> detector2 = new FunctionalDetector(spll2, new DetectorDecisionFunctionAdapter(MoaDetectorAdapter.cusum()), n -> n >= 100);

        Evaluator evaluator2 = new BasicEvaluator();
        Evaluation evaluation2 = evaluator.evaluate(detector2, arffStream, 10000, 10);
        log.info(evaluation2.toString());

    }

}
