package uk.ac.bangor.meander.detectors;

import uk.ac.bangor.meander.streams.StreamContext;

import java.util.function.Consumer;

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

            O output = execute(value, ctx);

            return source.execute(output, ctx);
        };
    }

    default <R> Pipe<I, R> then(Pipe<O, R> source, TriConsumer<R,Pipe<O,R>,StreamContext> fork) {
        return (value, ctx) -> {
            if(!source.ready()) {
                throw new NotReadyException(source);
            }

            O output = execute(value, ctx);

            R then = source.execute(output, ctx);
            fork.accept(then, source, ctx);
            return then;
        };
    }

    @FunctionalInterface
    interface TriConsumer<A,B,C> {
        void accept(A a, B b, C c);
    }

    default boolean ready() {
        return true;
    }

    class NotReadyException extends RuntimeException {
        public NotReadyException(Pipe source) {
            super(source.getClass().getSimpleName() + " is not ready.");
        }
    }

    static <I> Pipe<I,I> identity() {
        return (i,ctx) -> i;
    }
}
