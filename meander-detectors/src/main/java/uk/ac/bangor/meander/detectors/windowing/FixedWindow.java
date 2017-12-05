package uk.ac.bangor.meander.detectors.windowing;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.lang.reflect.Array;
import java.util.Iterator;

/**
 * @author Will Faithfull
 * @param <T>
 *      The observation type.
 */
public class FixedWindow<T> extends CircularFifoQueue<T> implements Window<T> {

    private final Class<T> tClass;

    public FixedWindow(int size, Class<T> tClass) {
        super(size);
        this.tClass = tClass;
    }

    public T[] getElements() {
        Iterator<T> iterator = iterator();
        T[] array = (T[]) Array.newInstance(tClass, super.size());

        int idx = 0;
        while(iterator.hasNext()) {
            array[idx++] = iterator.next();
        }

        return array;
    }

    public T getNewest() {
        return get(size());
    }

    public T getOldest() {
        return get(0);
    }

    public void update(T observation) {
        offer(observation);
    }

    public int capacity() {
        return maxSize();
    }
}
