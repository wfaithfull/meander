package uk.ac.bangor.meander.evaluators;

import lombok.Getter;
import lombok.Setter;
import uk.ac.bangor.meander.detectors.Pipe;
import uk.ac.bangor.meander.streams.ChangeStreamBuilder;
import uk.ac.bangor.meander.streams.StreamContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Will Faithfull
 */
public abstract class AbstractEvaluator implements Evaluator {

    @Getter @Setter int allowEarly = 0;
    @Setter protected ProgressReporter progressReporter;
    Map<Integer, Long> classDistribution = new HashMap<>();

    @Override
    public Evaluation evaluate(Pipe<Double[],Boolean> detector, ChangeStreamBuilder changeStream, long limit, long n) {
        ArrayList<Evaluation> evaluations= new ArrayList<>();

        for(int i=0; i<n; i++) {
            evaluations.add(evaluate(detector, changeStream.build().limit(limit)));
        }

        return new Evaluation(evaluations);
    }

    protected int getCurrentClass(StreamContext context) {
        int currentClass = context.getLabel();

        if(!classDistribution.containsKey(currentClass)) {
            classDistribution.put(currentClass, 1L);
        }

        long classCount = classDistribution.get(currentClass);
        classCount++;
        classDistribution.put(currentClass, classCount);

        return currentClass;
    }
}
