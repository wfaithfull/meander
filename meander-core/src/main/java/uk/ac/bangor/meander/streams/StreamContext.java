package uk.ac.bangor.meander.streams;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import uk.ac.bangor.meander.transitions.Transition;

import java.util.*;

/**
 * @author Will Faithfull
 * Context class which holds information about the current place in the stream, as well as the labels
 * for the underlying sequence and class of the last example.
 */
public class StreamContext {

    @Getter private                                      long             index;
    @Getter @Setter                                      int              sequence;
    @Getter @Setter                                      int              label;
    @Getter @Setter                                      List<Transition> transitions;
    private                                              Transition       mostRecent;
    @Getter @Setter(AccessLevel.PACKAGE)         private double[]         sourcePriors;
    private                                              List<double[]>  classPriors;

    private StreamContext() {
        index = 0;
        this.transitions = new ArrayList<>();
        this.classPriors = new LinkedList<>();
    }

    void advance() {
        index++;
        classPriors = new LinkedList<>();
    }

    static StreamContext START = new StreamContext();

    void transition(Transition transition) {
        transitions.add(transition);
        mostRecent = transition;
    }

    public Optional<Transition> getCurrentTransition() {
        if(mostRecent.isValidFor(index)) {
            return Optional.of(mostRecent);
        } else {
            return Optional.empty();
        }
    }

    public double[] getClassPriors() {
        double[] jointClassPriors = new double[classPriors.get(0).length];

        for(int i=0;i<sourcePriors.length;i++) {
            double sourceProb = sourcePriors[i];

            if(sourceProb == 0.0) {
                continue;
            }

            double[] sourceClassPriors = classPriors.get(i);

            for (int j = 0; j < jointClassPriors.length; j++) {
                jointClassPriors[j] += sourceProb * sourceClassPriors[j];
            }
        }

        return jointClassPriors;
    }

    void setClassPriors(double[] classPriors) {
        this.classPriors.add(classPriors);
    }

}
