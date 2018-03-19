package uk.ac.bangor.meander.detectors;

import uk.ac.bangor.meander.streams.StreamContext;

/**
 * Created by wfaithfull on 11/03/18.
 */
public interface Pipe<I, O> {

    O execute(I value, StreamContext context);

    default <R> Pipe<I, R> then(Pipe<O, R> source) {
        return (value, ctx) -> {
            if (ctx.detectorsNeedReset()) {
                reset();
                source.reset();
            }

            O output = execute(value, ctx);

            if(!ready()) {
                throw new NotReadyException(this);
            } else if(!source.ready()) {
                throw new NotReadyException(source);
            }

            return source.execute(output, ctx);
        };
    }

    default <R> Pipe<I, R> then(Pipe<O, R> source, TriConsumer<R,Pipe<O,R>,StreamContext> fork) {
        return (value, ctx) -> {
            if (ctx.detectorsNeedReset()) {
                reset();
                source.reset();
            }
            O output = execute(value, ctx);

            if(!ready()) {
                throw new NotReadyException(this);
            } else if(!source.ready()) {
                throw new NotReadyException(source);
            }

            R then = source.execute(output, ctx);
            fork.accept(then, source, ctx);
            return then;
        };
    }

    default void reset() {
    }

    default boolean needReset() {
        return false;
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
