package uk.ac.bangor.meander.detectors;

import lombok.Getter;

import java.util.function.Function;

/**
 * @author Will Faithfull
 */
public class FunctionalDetector implements Detector<Double[]> {

    @Getter double statistic;
    boolean change;

    final ReductionFunction reduction;
    final DecisionFunction decision;
    private Function<Long, Boolean> ready;

    private long n;

    public FunctionalDetector(ReductionFunction reduction, DecisionFunction decision, Function<Long, Boolean> ready) {
        this.reduction = reduction;
        this.decision = decision;
        this.ready = ready;
    }

    @Override
    public void update(Double[] input) {
        this.statistic = reduction.reduce(input);
        if(!ready.apply(n)) {
            n++;
            return;
        }
        this.change = decision.decide(statistic);
    }

    @Override
    public boolean isChangeDetected() {
        return change;
    }

    @Override
    public void reset() {

    }
}
