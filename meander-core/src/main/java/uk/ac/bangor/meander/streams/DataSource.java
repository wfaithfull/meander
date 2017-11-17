package uk.ac.bangor.meander.streams;

import java.util.function.Supplier;

/**
 * @author Will Faithfull
 */
public interface DataSource {

    Supplier<Double[]> getSource();

}
