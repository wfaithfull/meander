package uk.ac.bangor.meander.streams;

import uk.ac.bangor.meander.transitions.AbruptTransition;
import weka.core.Instance;
import weka.core.Instances;

import java.util.Iterator;

/**
 * @author Will Faithfull
 */
public class ArffExampleProviderFactory implements ExampleProviderFactory {

    private Iterator<Instance> instances;
    private Integer lastClass;

    public ArffExampleProviderFactory(Instances instances) {
        instances.setClassIndex(instances.numAttributes()-1);
        this.instances = instances.iterator();
    }

    @Override
    public ExampleProvider getProvider() {
        return ctx -> {
            Example example = instanceToExample(instances.next(), ctx);
            ctx.setFinished(!instances.hasNext());
            return example;
        };
    }

    private Example instanceToExample(Instance instance, StreamContext context) {
        double[] data = instance.toDoubleArray();
        Double[] boxed = new Double[data.length];
        for(int i=0;i<data.length;i++) {
            boxed[i] = data[i];
        }

        int label = (int) instance.classValue();

        if(lastClass != null && lastClass != label) {
            context.transition(new AbruptTransition(context.getIndex()));
        }

        context.setLabel(label);

        return new Example(boxed, context);
    }
}
