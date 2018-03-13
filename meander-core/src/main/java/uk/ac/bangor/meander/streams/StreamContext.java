package uk.ac.bangor.meander.streams;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import uk.ac.bangor.meander.transitions.Transition;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * @author Will Faithfull
 * Context class which holds information about the current place in the stream, as well as the labels
 * for the source and class of the last example.
 */
public class StreamContext {

    @Getter private                              long             index;
    @Getter @Setter                              int              sequence;
    @Getter @Setter                              int              label;
    @Getter @Setter                              List<Transition> transitions;
    private                                      Transition       mostRecent;
    @Getter @Setter(AccessLevel.PACKAGE) private double[]         sourcePriors;
    private                                      List<double[]>   classPriors;
    @Getter private                              int              dimensionality;
    @Getter @Setter(AccessLevel.PACKAGE) private boolean          finished;
    @Getter @Setter private                      Integer[]        changeLabels;
    @Getter
    private                                      long             lastDetection;

    StreamContext() {
        index = 0;
        this.transitions = new ArrayList<>();
        this.classPriors = new LinkedList<>();
    }

    StreamContext(Integer[] changeLabels) {
        index = 0;
        this.transitions = new ArrayList<>();
        this.classPriors = new LinkedList<>();
        setChangeLabels(changeLabels);
    }

    public Optional<Transition> getCurrentTransition() {
        if(mostRecent != null && mostRecent.isValidFor(index)) {
            return Optional.of(mostRecent);
        } else {
            return Optional.empty();
        }
    }

    public boolean isChanging() {
        return getCurrentTransition().isPresent();
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

    void advance(Example example) {
        index++;
        this.dimensionality = example.getData().length;
        classPriors = new LinkedList<>();
    }

    void transition(Transition transition) {
        transitions.add(transition);
        mostRecent = transition;
    }

    public void detection() {
        lastDetection = index;
    }

    public boolean detectorsNeedReset() {
        return lastDetection == index - 1;
    }

    void setClassPriors(double[] classPriors) {
        this.classPriors.add(classPriors);
    }

}
