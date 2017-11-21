package uk.ac.bangor.meander.streams;

/**
 * Enumeration indicating whether this mixture distribution is over a class
 * or a sequence within the stream - i.e. whether this is a dirichlet process
 * or one component of a dirichlet process.
 */
enum StreamConcept {

    CLASS,
    SEQUENCE

}
