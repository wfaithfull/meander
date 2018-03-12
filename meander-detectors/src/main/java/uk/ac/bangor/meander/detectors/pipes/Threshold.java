package uk.ac.bangor.meander.detectors.pipes;

import uk.ac.bangor.meander.detectors.Pipe;
import uk.ac.bangor.meander.streams.StreamContext;

import java.util.function.BiFunction;

/**
 * @author Will Faithfull
 */
public class Threshold<T> implements Pipe<T, Boolean> {

    private final Op op;
    private final BiFunction<Double, Double, Boolean> fn;
    private final Pipe<T, Double> threshold;
    private final Pipe<T, Double> doubleMapper;
    private TriConsumer<Double, Pipe, StreamContext> consumer;

    public enum Op {
        GT,
        GEQ,
        LT,
        LEQ
    }

    private final static BiFunction<Double, Double, Boolean> GT = (x, t) -> x > t;
    private final static BiFunction<Double, Double, Boolean> GEQ = (x, t) -> x >= t;
    private final static BiFunction<Double, Double, Boolean> LT = (x, t) -> x < t;
    private final static BiFunction<Double, Double, Boolean> LEQ = (x, t) -> x <= t;

    public Threshold(Op op, Pipe<T, Double> threshold, Pipe<T, Double> doubleMapper) {
        this.op = op;
        this.threshold = threshold;
        this.doubleMapper = doubleMapper;

        switch (op) {
            case GT: this.fn = GT; break;
            case GEQ: this.fn = GEQ; break;
            case LT: this.fn = LT; break;
            case LEQ: this.fn = LEQ; break;
            default:
                fn = GT;
        }
    }

    public Threshold(Op op, double threshold, Pipe<T, Double> doubleMapper) {
        this(op, (value, context) -> threshold, doubleMapper);
    }

    @Override
    public Boolean execute(T value, StreamContext context) {
        double threshold = this.threshold.execute(value, context);
        if(consumer != null) {
            consumer.accept(threshold, this, context);
        }
        return this.fn.apply(doubleMapper.execute(value, context), threshold);
    }

    public static Threshold<Double> greaterThan(double threshold) {
        return new Threshold(Op.GT, threshold, Pipe.identity());
    }

    public static Threshold<Double> lessThan(double threshold) {
        return new Threshold(Op.LT, threshold, Pipe.identity());
    }

    public static Threshold<Double> greaterThanOrEqualTo(double threshold) {
        return new Threshold(Op.GEQ, threshold, Pipe.identity());
    }

    public static Threshold<Double> lessThanOrEqualTo(double threshold) {
        return new Threshold(Op.LEQ, threshold, Pipe.identity());
    }

    /**
     * A threshold is a special case for reporting. It may generate another dynamic double valued statistic
     * which we want to report, but the pipe maps from T -> Boolean, so under normal circumstances we wouldn't be able
     * to see it. This allows us to attach a reporter to consume the threshold value as well for plotting.
     */
    public Threshold<T> report(TriConsumer<Double, Pipe, StreamContext> consumer) {
        this.consumer = consumer;
        return this;
    }
}
