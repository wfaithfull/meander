package uk.ac.bangor.meander.detectors;

import uk.ac.bangor.meander.streams.StreamContext;

import java.util.function.Function;

/**
 * @author Will Faithfull
 */
public class ReportPipe<I> implements Pipe<I,I> {

    private final TriConsumer<Double, Pipe, StreamContext> reporter;
    private final Function<I, Double>                      mapper;

    public ReportPipe(TriConsumer<Double, Pipe, StreamContext> reporter, Function<I,Double> mapper) {
        this.reporter = reporter;
        this.mapper = mapper;
    }

    @Override
    public I execute(I value, StreamContext context) {
        reporter.accept(mapper.apply(value), this, context);
        return value;
    }
}