package uk.ac.bangor.meander.evaluators;

import lombok.extern.java.Log;
import uk.ac.bangor.meander.detectors.Detector;
import uk.ac.bangor.meander.streams.ChangeStreamBuilder;
import uk.ac.bangor.meander.streams.Example;
import uk.ac.bangor.meander.streams.StreamContext;
import uk.ac.bangor.meander.transitions.Transition;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.stream.Stream;

/**
 * @author Will Faithfull
 *
 * Performs an evaluation of the primary change detection metrics:
 *      Average Run Length      (ARL)
 *      Time to Detection       (TTD)
 *      False Alarm Rate        (FAR)
 *      Missed Detection Ratio  (MDR)
 */
@Log
public class BasicEvaluator extends AbstractEvaluator {

    private static final long MAX_N = 3000000000L;
    private final int warmup;

    public BasicEvaluator(int warmup) {
        this.warmup = warmup;
    }

    public BasicEvaluator() {
        this(0);
    }

    @Override
    public Evaluation evaluate(Detector<Double[]> detector, Stream<Example> changeStream) {

        Iterator<Example> iterator = changeStream.iterator();

        long n = 0;

        ArrayList<Long> detections  = new ArrayList<>();
        LinkedList<Transition> transitions = new LinkedList<>();

        Transition transition = null;

        if(warmup > 0) {
            log.info(String.format("Warming up with %d examples...", warmup));
        }
        for(int i=warmup;i>0;i--) {
            Example example = iterator.next();
            detector.update(example.getData());
        }

        while(iterator.hasNext() && n < MAX_N) {
            Example example = iterator.next();
            StreamContext ctx = example.getContext();

            long index = ctx.getIndex();
            detector.update(example.getData());

            if(ctx.isChanging()) {
                transition = ctx.getCurrentTransition().get();
                if(transition != transitions.peekLast()) {
                    transitions.add(transition);
                }
            }

            if(detector.isChangeDetected()) {
                detections.add(index);
            }

            if(n > 100000) {
                log.warning("Did you forget to limit the stream? I will technically keep going until n=" + MAX_N);
            }

            n++;
        }

        return new Evaluation(n, transitions, detections);
    }

}
