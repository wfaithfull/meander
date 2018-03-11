package uk.ac.bangor.meander.detectors;

import uk.ac.bangor.meander.streams.StreamContext;

/**
 * Created by wfaithfull on 11/03/18.
 */
public interface Pipe<I, O> {

    O execute(I value, StreamContext context);

    default <R> Pipe<I, R> then(Pipe<O, R> source) {
        return (value, ctx) -> {
            if(!source.ready()) {
                throw new NotReadyException(source);
            }
            return source.execute(execute(value, ctx), ctx);
        };
    }

    default boolean ready() {
        return true;
    }

    static <I, O> Pipe<I, O> of(Pipe<I, O> source) {
        return source;
    }

    public static class NotReadyException extends RuntimeException {
        public NotReadyException(Pipe source) {
            super(source.getClass().getSimpleName() + " is not ready.");
        }
    }
}
