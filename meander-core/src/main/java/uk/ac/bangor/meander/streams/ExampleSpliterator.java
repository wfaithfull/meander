package uk.ac.bangor.meander.streams;

import lombok.NonNull;

import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * @author Will Faithfull
 *
 * Spliterator of {@link Example} that can be used to stream an {@link ExampleProviderFactory} and advance a
 * {@link StreamContext}.
 */
class ExampleSpliterator implements Spliterator<Example> {

    private ExampleProviderFactory source;
    private StreamContext          context;

    /**
     * Create the spliterator.
     * @param source Factory for {@link ExampleProvider}s.
     * @param context Stream context component.
     */
    ExampleSpliterator(@NonNull final ExampleProviderFactory source, @NonNull final StreamContext context) {
        this.source = source;
        this.context = context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean tryAdvance(Consumer<? super Example> action) {
        action.accept(source.getProvider().apply(context));
        context.advance();
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Spliterator<Example> trySplit() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long estimateSize() {
        return -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int characteristics() {
        return ORDERED | NONNULL | IMMUTABLE;
    }
}
