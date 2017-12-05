package uk.ac.bangor.meander.streams;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Will Faithfull
 *
 * Example from the change stream, paired with reference to current stream context.
 */
@Data
@AllArgsConstructor
public class Example {
    Double[] data;
    StreamContext context;
}
