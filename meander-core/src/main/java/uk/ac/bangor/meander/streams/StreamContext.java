package uk.ac.bangor.meander.streams;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Will Faithfull
 * Context class which holds information about the current place in the stream, as well as the labels
 * for the underlying sequence and class of the last example.
 */
public class StreamContext {

    @Getter private long index;
    @Getter @Setter int sequence;
    @Getter @Setter int label;

    private StreamContext() {
        index = 0;
    }

    void advance() {
        index++;
    }

    static StreamContext START = new StreamContext();

}
