package uk.ac.bangor.meander.streams;

import lombok.NonNull;

import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * @author Will Faithfull
 */
public class DataSourceSpliterator implements Spliterator<Double[]> {

    private DataSource source;

    public DataSourceSpliterator(@NonNull final DataSource source) {
        this.source = source;
    }

    @Override
    public boolean tryAdvance(Consumer<? super Double[]> action) {
        action.accept(source.getSource().get());
        return true;
    }

    @Override
    public Spliterator<Double[]> trySplit() {
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
