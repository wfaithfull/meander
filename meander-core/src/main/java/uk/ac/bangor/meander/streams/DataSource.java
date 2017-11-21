package uk.ac.bangor.meander.streams;

import java.util.function.Function;

/**
 * @author Will Faithfull
 */
interface DataSource {

    Function<StreamContext, Example> getSource();

}
