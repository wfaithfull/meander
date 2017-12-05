package uk.ac.bangor.meander.detectors.windowing;

import lombok.Getter;

import java.lang.reflect.Array;

/**
 * @author Will Faithfull
 *
 * Represents a pair of sliding windows, with W1 holding the oldest observations, and W2 holding the newest.
 */
@Getter
public class FixedWindowPair<T> implements Window<T> {

    private Window<T> window1;
    private Window<T> window2;
    private Class<T> tClass;

    public FixedWindowPair(int size1, int size2, Class<T> tClass) {
        this.tClass = tClass;
        window1 = new FixedWindow<>(size1, tClass);
        window2 = new FixedWindow<>(size2, tClass);
    }

    @Override
    public T[] getElements() {
        Class<?> componentType = window1.getElements().getClass().getComponentType();
        @SuppressWarnings("unchecked")
        T[] combined = (T[]) Array.newInstance(componentType, window1.size() + window2.size());
        System.arraycopy(window1.getElements(), 0, combined, 0, window1.size());
        System.arraycopy(window2.getElements(), 0, combined, window1.size(), window2.size());

        return combined;
    }

    @Override
    public T getNewest() {
        return window2.getNewest();
    }

    @Override
    public T getOldest() {
        return window1.getOldest();
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
        if(window2.size() == window2.capacity())
            window1.update(window2.getOldest());

        window2.update(observation);
    }

    @Override
    public int size() {
        return window1.size()+window2.size();
    }

    @Override
    public int capacity() {
        return window1.capacity()+window2.capacity();
    }

}