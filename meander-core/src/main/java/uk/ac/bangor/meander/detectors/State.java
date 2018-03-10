package uk.ac.bangor.meander.detectors;

import lombok.*;

import java.util.Optional;

/**
 * @author Will Faithfull
 */
@Data
@NoArgsConstructor
public class State {

    private State(String name) {
        this(name, null, null);
    }

    private State(String name, Double statistic) {
        this(name, statistic, null);
    }

    private State(String name, Double statistic, Double threshold) {
        this.name = name;
        this.statistic = Optional.of(statistic);
        this.threshold = Optional.of(statistic);
    }

    public static State empty() {
        return new State("empty");
    }

    public static State statistic(double statistic) {
        return new State("statistic", statistic);
    }

    public static State threshold(double threshold) {
        return new State("threshold", null, threshold);
    }

    public static State statisticAndThreshold(double statistic, double threshold) {
        return new State("statisticAndThreshold", statistic, threshold);
    }

    String name;
    Optional<Double> statistic;
    Optional<Double> threshold;
}
