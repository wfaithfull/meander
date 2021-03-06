package uk.ac.bangor.meander.streams;

import uk.ac.bangor.meander.transitions.Transition;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Will Faithfull
 *
 * Mixing function that provides a sequential distribution based on the provided transitions, e.g. for changes
 * at indices 0, 1 and 2, we would get the following distribution:
 *
 * t=0 [ 1.0, 0.0, 0.0 ]
 * t=1 [ 0.0, 1.0, 0.0 ]
 * t=2 [ 0.0, 0.0, 1.0 ]
 */
class SequentialMixingFunction implements MixingFunction {

    private double[] p1;
    private double[] p2;
    private LinkedList<Transition> transitions = new LinkedList<>();
    private Transition  pointer;
    private int         sequence;

    /**
     * Create a sequential mixing function from the provided transitions. This will have {@code transitions + 1}
     * entries in it's distribution.
     * @param transitions Transitions to modify the mixing proportions over time.
     */
    SequentialMixingFunction(List<Transition> transitions) {

        for (Transition transition : transitions) {
            this.transitions.add(transition);
        }

        p1 = new double[transitions.size() + 1];
        p2 = new double[transitions.size() + 1];
        p1[0] = 1.0;

        if(!transitions.isEmpty()) {
            p2[1] = 1.0;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double[] getDistribution(StreamContext context) {

        if(pointer == null) {
            advanceTransition(context);
        }

        if(pointer != null) {

            long index = context.getIndex();

            if (index > pointer.getEnd()) {
                advanceTransition(context);
                if(sequence < p1.length - 1) {
                    advanceSequence();
                }
            }

            if(pointer.isValidFor(index)) {
                if(!pointer.isPrepared()) {
                    pointer.prepare(p1, p2);
                }

                return pointer.getDistribution(context);
            }
        }

        return p1;
    }

    private void advanceTransition(StreamContext context) {
        if(!transitions.isEmpty()) {
            this.pointer = transitions.pop();
            context.transition(pointer);
        }
    }

    private void advanceSequence() {

        p1[sequence] = 0.0;
        if(sequence+1 < p1.length) {
            p2[sequence + 1] = 0.0;
            p1[sequence + 1] = 1.0;
        }
        if(sequence+2 < p1.length) {
            p2[sequence + 2] = 1.0;
        }
        sequence++;
    }

}
