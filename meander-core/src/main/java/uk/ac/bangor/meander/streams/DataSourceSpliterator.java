package uk.ac.bangor.meander.streams;

import lombok.NonNull;

import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * @author Will Faithfull
 */
class DataSourceSpliterator implements Spliterator<Example> {

    private DataSource source;
    private StreamContext context;

    DataSourceSpliterator(@NonNull final DataSource source) {
        this.source = source;
        this.context = StreamContext.START;
    }

    @Override
    public boolean tryAdvance(Consumer<? super Example> action) {
        action.accept(source.getSource().apply(context));
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
