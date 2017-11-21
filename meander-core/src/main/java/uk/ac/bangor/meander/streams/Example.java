package uk.ac.bangor.meander.streams;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Will Faithfull
 */
@Data
@AllArgsConstructor
public class Example {
    Double[] data;
    StreamContext context;
}
