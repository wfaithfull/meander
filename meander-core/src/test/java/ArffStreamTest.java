import uk.ac.bangor.meander.streams.ChangeStreamBuilder;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * @author Will Faithfull
 */
public class ArffStreamTest {

    public static void main(String[] args) throws IOException {
        Stream<Double[]> arffStream = ChangeStreamBuilder
                .fromArff(new InputStreamReader(ArffStreamTest.class.getResourceAsStream("abalone.arff")))
                .withUniformMixture();

        arffStream.limit(1000).forEach(x -> System.out.println(Arrays.toString(x)));
    }
}
