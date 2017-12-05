import lombok.extern.java.Log;
import uk.ac.bangor.meander.detectors.Detector;
import uk.ac.bangor.meander.detectors.SPLL;
import uk.ac.bangor.meander.detectors.windowing.FixedWindowPair;
import uk.ac.bangor.meander.evaluators.Evaluation;
import uk.ac.bangor.meander.evaluators.BasicEvaluator;
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
public class EvaluateSPLL {

    public static void main(String[] args) throws IOException {

        ChangeStreamBuilder arffStream = ChangeStreamBuilder
                .fromArff("abalone.arff")
                .withUniformClassMixture().fromStart()
                .withClassMixture(1.0, 0.0, 0.0).transition(new AbruptTransition(500))
                .withClassMixture(0.0, 1.0, 0.0).transition(new AbruptTransition(5000))
                .withClassMixture(0.0, 0.0, 1.0).transition(new AbruptTransition(7500));

        Detector<Double[]> spll = new SPLL(new FixedWindowPair<>(50,50, double[].class), 3);
        Evaluator evaluator = new BasicEvaluator();
        Evaluation evaluation = evaluator.evaluate(spll, arffStream, 10000, 10);


        log.info(evaluation.toString());

    }

}
