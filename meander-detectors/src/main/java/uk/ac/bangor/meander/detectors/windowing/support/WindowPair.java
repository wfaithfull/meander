package uk.ac.bangor.meander.detectors.windowing.support;

import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Array;

/**
 * @author Will Faithfull
 *
 * Represents a pair of sliding windows, with W1 holding the oldest observations, and W2 holding the newest.
 */
@Getter
@Setter
public class WindowPair<T> implements Window<T> {

    protected Window<T> tail;
    protected Window<T> head;

    public WindowPair(int w1sz, int w2sz, Class<T> tClass) {
        this.tail = new FixedWindow<T>(w1sz, tClass);
        this.head = new FixedWindow<T>(w2sz, tClass);
    }

    public WindowPair(Window<T> tail, Window<T> head) {
        this.tail = tail;
        this.head = head;
    }

    @Override
    public T[] getElements() {
        Class<?> componentType = tail.getElements().getClass().getComponentType();
        @SuppressWarnings("unchecked")
        T[] combined = (T[]) Array.newInstance(componentType, tail.size() + head.size());
        System.arraycopy(tail.getElements(), 0, combined, 0, tail.size());
        System.arraycopy(head.getElements(), 0, combined, tail.size(), head.size());

        return combined;
    }

    @Override
    public T getNewest() {
        return head.getNewest();
    }

    @Override
    public T getOldest() {
        return tail.getOldest();
    }

    /**
     * |W1| = 5
     * |W2| = 5
     *
     *   W1          W2
     *  [*****] <- [*****]
     * ----------------->
     *         t
     *
     * W2 is updated with the latest observations. When W2 is full, it's oldest element is used to update W1, and is
     * then replaced in W2 with a new observation.
     *
     * W1 contains the older observations.
     * W2 contains the newer observations.
     *
     * @param observation
     *          A new observation.
     */
    public void update(T observation) {
        if (head.size() == head.capacity())
            tail.update(head.getOldest());

        head.update(observation);
    }

    @Override
    public int size() {
        return tail.size() + head.size();
    }

    @Override
    public int capacity() {
        return tail.capacity() + head.capacity();
    }

    @Override
    public void clear() {
        tail.clear();
        head.clear();
    }

}