package uk.ac.bangor.meander.streams;

/**
 * @author Will Faithfull
 *
 * Factory interface for {@link ExampleProvider}s.
 */
interface ExampleProviderFactory {

    /**
     * Create a new {@link ExampleProvider}.
     * @return An {@link ExampleProvider}.
     */
    ExampleProvider getProvider();

}
