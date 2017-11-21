import uk.ac.bangor.meander.streams.ChangeStreamBuilder;
import uk.ac.bangor.meander.streams.Example;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * @author Will Faithfull
 */
public class ArffStreamTest {

    public static void main(String[] args) throws IOException {
        Stream<Example> arffStream = ChangeStreamBuilder
                .fromArff(new InputStreamReader(ArffStreamTest.class.getResourceAsStream("abalone.arff")))
                .withClassMixture(0, 0.5, 0.5).fromStart()
                .withClassMixture(1.0, 0.0, 0.0).at(33)
                .withUniformClassMixture().at(66)
                .build();

        arffStream.limit(100).forEach(x -> System.out.println(String.format(
                "i=%-5d|S=%-2d|ω=%-2d| %s",
                x.getContext().getIndex(),
                x.getContext().getSequence(),
                x.getContext().getLabel(),
                Arrays.toString(x.getData()))));
    }
}
