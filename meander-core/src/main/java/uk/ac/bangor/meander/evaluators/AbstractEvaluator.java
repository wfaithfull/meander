package uk.ac.bangor.meander.evaluators;

import uk.ac.bangor.meander.detectors.Detector;
import uk.ac.bangor.meander.streams.ChangeStreamBuilder;

import java.util.ArrayList;

/**
 * @author Will Faithfull
 */
public abstract class AbstractEvaluator implements Evaluator {

    @Override
    public Evaluation evaluate(Detector<Double[]> detector, ChangeStreamBuilder changeStream, long limit, long n) {
        ArrayList<Evaluation> evaluations= new ArrayList<>();

        for(int i=0; i<n; i++) {
            evaluations.add(evaluate(detector, changeStream.build().limit(limit)));
        }

        return new Evaluation(evaluations);
    }
}
