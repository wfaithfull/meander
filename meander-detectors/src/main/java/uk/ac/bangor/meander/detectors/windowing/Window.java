package uk.ac.bangor.meander.detectors.windowing;

/**
 * @author Will Faithfull
 * The basic abstraction for a sliding window.
 * @param <T>
 *              The example type for the window.
 */
public interface Window<T> {

    /**
     * Get all the elements in this window.
     * @return
     *          The elements of the window.
     */
    T[] getElements();

    /**
     * Get the most recent observation in the window, i.e. the last one passed to {@link #update(Object)}
     * @return
     *          The most recent observation
     */
    T getNewest();

    /**
     * Get the oldest observation in the window.
     *
     * If the window is full, this is the observation that will be removed when {@link #update(Object)} is next called.
     *
     * Otherwise, it will be the first observation passed to {@link #update(Object)}
     * @return
     *          The oldest observation
     */
    T getOldest();

    /**
     * Update the window with the new observation specified.
     *
     * If the window is full, i.e. if {@link #size()} == {@link #capacity()}, then this will trigger the removal and
     * deletion of the oldest observation.
     * @param observation
     *                      The new observation
     */
    void update(T observation);

    /**
     * The current number of elements in the window. May be less than or equal to {@link #capacity()}
     * @return
     *          The current number of elements in the window.
     */
    int size();

    /**
     * The maximum number of elements in the window.
     * @return
     *          The maximum number of elements in the window.
     */
    int capacity();

    void clear();

}
