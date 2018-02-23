package uk.ac.bangor.meander.streams;

import weka.core.Instances;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Will Faithfull
 */
public class ArffSpliterator extends ExampleSpliterator {

    public ArffSpliterator(BufferedReader file) throws IOException {
        super(new ArffExampleProviderFactory(new Instances(file)), new StreamContext());
    }

    public ArffSpliterator(BufferedReader file, Integer... changeClasses) throws IOException {
        super(new ArffExampleProviderFactory(new Instances(file), changeClasses), new StreamContext());
    }

    public ArffSpliterator(ClassLoader classLoader, String file) throws IOException {
        this(new BufferedReader(new InputStreamReader(classLoader.getResourceAsStream(file))));
    }

    public ArffSpliterator(ClassLoader classLoader, String file, Integer... changeClasses) throws IOException {
        this(new BufferedReader(new InputStreamReader(classLoader.getResourceAsStream(file))), changeClasses);
    }

}
