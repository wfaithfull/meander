package uk.ac.bangor.meander.streams;

import java.util.function.Function;

/**
 * @author Will Faithfull
 *
 * Interface for anything that provides stream examples, given a context.
 */
public interface ExampleProvider extends Function<StreamContext, Example> {}
