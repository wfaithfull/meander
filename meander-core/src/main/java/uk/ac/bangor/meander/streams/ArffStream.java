package uk.ac.bangor.meander.streams;

import uk.ac.bangor.meander.MeanderException;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Will Faithfull
 */
public class ArffStream {

    public Stream<Example> of(String arff) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            return StreamSupport.stream(new ArffSpliterator(classLoader, arff), false);
        } catch (IOException ex) {
            throw new MeanderException("Couldn't read .arff file", ex);
        }
    }

    public Stream<Example> of(String arff, Integer... changeClasses) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            return StreamSupport.stream(new ArffSpliterator(classLoader, arff, changeClasses), false);
        } catch (IOException ex) {
            throw new MeanderException("Couldn't read .arff file", ex);
        }
    }

    public Stream<Example> of(BufferedReader arff, Integer... changeClasses) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            return StreamSupport.stream(new ArffSpliterator(arff, changeClasses), false);
        } catch (IOException ex) {
            throw new MeanderException("Couldn't read .arff file", ex);
        }
    }

    public Stream<Example> of(BufferedReader arff) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            return StreamSupport.stream(new ArffSpliterator(arff), false);
        } catch (IOException ex) {
            throw new MeanderException("Couldn't read .arff file", ex);
        }
    }

}
