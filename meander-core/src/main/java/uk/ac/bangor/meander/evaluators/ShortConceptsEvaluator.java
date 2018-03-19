package uk.ac.bangor.meander.evaluators;

import lombok.extern.java.Log;
import uk.ac.bangor.meander.detectors.Pipe;
import uk.ac.bangor.meander.streams.Example;
import uk.ac.bangor.meander.streams.StreamContext;
import uk.ac.bangor.meander.transitions.Transition;

import java.util.*;
import java.util.stream.Stream;

/**
 * An evaluator which makes a special assumption based on concepts being short lived.
 *
 * Consider a ground truth stream like so (transition points marked):
 *
 * 000000011100000000000000000000000000000000000000000001110000000000000000000000000
 *        t  t                                          t  t
 *
 * Our objective may be to detect these short sequences, rather than the to and from transition points. Under a normal
 * evaluation as in {@link SequenceEvaluator}, the detectors would only have two observations to make a detection before
 * it is recorded as a false positive. In this interpretation, we are much more flexible, only interpreting transitions
 * from 0->1 as change points, not transitions from 1->0.
 *
 * This is a better match for problems such as blink detection, where it isn't important that the blink is detected
 * while it is occurring, but only that it is detected as soon afterwards as possible.
 *
 * @author Will Faithfull
 */
@Log
public class ShortConceptsEvaluator extends AbstractEvaluator {

    private static final long MAX_N = 3000000000L;

    @Override
    public Evaluation evaluate(Pipe<Double[],Boolean> detector, Stream<Example> changeStream) {

        Iterator<Example> iterator = changeStream.iterator();

        long n = 0;

        ArrayList<Long> detections  = new ArrayList<>();
        LinkedList<Transition> transitions = new LinkedList<>();

        Transition transition = null;

        if(progressReporter != null) {
            progressReporter.update(n, "Evaluating " + detector.toString());
        }

        int currentClass = 0;
        Set<Integer> changeClasses = new HashSet<>();

        while(iterator.hasNext() && n < MAX_N) {

            if(progressReporter != null) {
                progressReporter.update(n);
            }

            Example example = iterator.next();
            StreamContext ctx = example.getContext();

            if(changeClasses.isEmpty()) {
                changeClasses.addAll(Arrays.asList(ctx.getChangeLabels()));
            }

            currentClass = getCurrentClass(ctx);

            long index = ctx.getIndex();
            boolean detection = false;
            try {
                detection = detector.execute(example.getData(), ctx);
            } catch (Pipe.NotReadyException notReady) {
                // Shrug. We just have to continue.
            }

            if(ctx.isChanging()) {
                transition = ctx.getCurrentTransition().get();

                // If we are transitioning *to* a change class..
                if(changeClasses.contains(currentClass)) {

                    // Then it's a change as normal.
                    if (transition != transitions.peekLast()) {
                        transitions.add(transition);
                    }
                }
            }

            if(detection) {
                detections.add(index);
            }

            if(n > 100000) {
                log.warning("Did you forget to limit the stream? I will technically keep going until n=" + MAX_N);
            }

            n++;
        }

        return new Evaluation(n, transitions, detections, getAllowEarly());
    }

}
