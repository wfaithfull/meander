package uk.ac.bangor.meander.streams;

import lombok.NonNull;

import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * @author Will Faithfull
 */
class ExampleSpliterator implements Spliterator<Example> {

    private ExampleProviderFactory source;
    private StreamContext          context;

    ExampleSpliterator(@NonNull final ExampleProviderFactory source, @NonNull final StreamContext context) {
        this.source = source;
        this.context = context;
    }

    @Override
    public boolean tryAdvance(Consumer<? super Example> action) {
        action.accept(source.getProvider().apply(context));
        context.advance();
        return true;
    }

    @Override
    public Spliterator<Example> trySplit() {
        return null;
    }

    @Override
    public long estimateSize() {
        return -1;
    }

    @Override
    public int characteristics() {
        return ORDERED | NONNULL | IMMUTABLE;
    }
}
