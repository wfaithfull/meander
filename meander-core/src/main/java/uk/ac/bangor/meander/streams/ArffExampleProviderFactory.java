package uk.ac.bangor.meander.streams;

import uk.ac.bangor.meander.transitions.AbruptTransition;
import weka.core.Instance;
import weka.core.Instances;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Will Faithfull
 */
public class ArffExampleProviderFactory implements ExampleProviderFactory {

    private Iterator<Instance> instances;
    private Set<Integer>       changeLabels = new HashSet<>();
    private Integer            lastClass;

    public ArffExampleProviderFactory(Instances instances) {
        instances.setClassIndex(instances.numAttributes()-1);
        this.instances = instances.iterator();
    }

    public ArffExampleProviderFactory(Instances instances, Integer... changeLabels) {
        this(instances);
        this.changeLabels.addAll(Arrays.asList(changeLabels));
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

        boolean isChange = lastClass != null && lastClass != label;
        if(isChange && !changeLabels.isEmpty()) {
            boolean lastClassWasChange = changeLabels.contains(lastClass);
            boolean thisClassIsChange = changeLabels.contains(label);

            isChange = lastClassWasChange ^ thisClassIsChange;
        }

        if(isChange) {
            context.transition(new AbruptTransition(context.getIndex()+1));
        }

        context.setLabel(label);

        lastClass = label;

        return new Example(boxed, context);
    }
}
