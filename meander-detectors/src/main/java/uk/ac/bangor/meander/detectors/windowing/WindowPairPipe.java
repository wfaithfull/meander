package uk.ac.bangor.meander.detectors.windowing;

import uk.ac.bangor.meander.detectors.Pipe;
import uk.ac.bangor.meander.detectors.windowing.support.WindowPair;
import uk.ac.bangor.meander.streams.StreamContext;

/**
 * @author Will Faithfull
 */
public class WindowPairPipe implements Pipe<Double[], WindowPair<Double[]>> {

    private WindowPair<Double[]> windowPair;

    public WindowPairPipe(WindowPair<Double[]> windowPair) {
        this.windowPair = windowPair;
    }

    public WindowPairPipe(int size) {
        this.windowPair = new WindowPair<>(size, size, Double[].class);
    }


    @Override
    public WindowPair<Double[]> execute(Double[] value, StreamContext context) {
        windowPair.update(value);
        return windowPair;
    }

    @Override
    public boolean ready() {
        return windowPair.size() == windowPair.capacity();
    }

    @Override
    public void reset() {
        windowPair.clear();
    }
}
