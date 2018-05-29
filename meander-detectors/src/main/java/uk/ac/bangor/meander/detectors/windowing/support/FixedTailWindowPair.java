package uk.ac.bangor.meander.detectors.windowing.support;

/**
 * @author Will Faithfull
 */
public class FixedTailWindowPair<T> extends WindowPair<T> {
    public FixedTailWindowPair(int w1sz, int w2sz, Class<T> tClass) {
        super(w1sz, w2sz, tClass);
    }

    public FixedTailWindowPair(Window<T> tail, Window<T> head) {
        super(tail, head);
    }

    @Override
    public void update(T observation) {
        if (head.size() == head.capacity() && tail.size() != tail.capacity()) {
            tail.update(head.getOldest());
        }

        head.update(observation);
    }
}
