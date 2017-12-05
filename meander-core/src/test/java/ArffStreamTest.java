import uk.ac.bangor.meander.streams.ChangeStreamBuilder;
import uk.ac.bangor.meander.streams.Example;
import uk.ac.bangor.meander.streams.StreamContext;
import uk.ac.bangor.meander.transitions.AbruptTransition;
import uk.ac.bangor.meander.transitions.LinearTransition;

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
                .withClassMixture(1.0, 0.0, 0.0).transition(new LinearTransition(20,33))
                .withClassMixture(0.0, 0.0, 1.0).transition(new AbruptTransition(50))
                .withUniformClassMixture().transition(new LinearTransition(60,70))
                .build();

        arffStream.limit(100).forEach(x -> {
            StreamContext ctx = x.getContext();
            System.out.println(String.format(
                    "i=%-5d|S=%-2d|ω=%-2d| %-35s| %-35s| %-35s| %s",
                    ctx.getIndex(),
                    ctx.getSequence(),
                    ctx.getLabel(),
                    ctx.getCurrentTransition().isPresent() ? ctx.getCurrentTransition().get() : "",
                    doubleArrayToString(ctx.getSourcePriors(), "%.2f"),
                    doubleArrayToString(ctx.getClassPriors(), "%.2f"),
                    Arrays.toString(x.getData())));
        });
    }

    private static String doubleArrayToString(double[] array, String fmt) {
        StringBuilder builder = new StringBuilder("[");
        for(int i=0;i<array.length;i++) {
            builder.append(String.format(fmt + (i < array.length-1 ? "," : ""), array[i]));
        }
        builder.append("]");
        return builder.toString();
    }
}
