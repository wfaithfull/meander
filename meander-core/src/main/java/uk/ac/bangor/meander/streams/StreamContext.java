package uk.ac.bangor.meander.streams;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import uk.ac.bangor.meander.transitions.Transition;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Will Faithfull
 * Context class which holds information about the current place in the stream, as well as the labels
 * for the source and class of the last example.
 */
@Getter
public class StreamContext {

    private long             index;
    @Setter
            int              sequence;
    @Setter
            int              label;
    @Setter
            List<Transition> transitions;
    private Transition       mostRecent;
    @Setter(AccessLevel.PACKAGE)
    private double[]         sourcePriors;
    private List<double[]>   classPriors;
    @Setter
    private int              dimensionality;
    @Setter(AccessLevel.PACKAGE)
    private boolean          finished;
    @Setter
    private Integer[]        changeLabels;
    private long             lastDetection;
    private boolean[]        keep;

    @Getter
    private Map<Class<?>, Object> cache = new ConcurrentHashMap<>();

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

    public void setKeep(boolean[] keep) {
        this.keep = keep;
    }

    public boolean isTrimmed() {
        for (boolean keepFeature : keep) {
            if (!keepFeature)
                return true;
        }
        return false;
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
