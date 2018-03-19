package uk.ac.bangor.meander.detectors.ensemble;

import uk.ac.bangor.meander.MeanderException;
import uk.ac.bangor.meander.detectors.Pipe;
import uk.ac.bangor.meander.streams.StreamContext;

import java.util.function.Supplier;

/**
 * @author Will Faithfull
 */
public class SubspaceEnsemble implements Pipe<Double[], Boolean[]> {

    private Pipe<Double, Boolean>[] detectors;
    private Supplier<Pipe<Double, Boolean>> detectorSupplier;
    private Boolean[] votes;

    public SubspaceEnsemble(Pipe<Double, Boolean>... detectors) {
        this.detectors = detectors;
        this.votes = new Boolean[detectors.length];
    }

    public SubspaceEnsemble(Supplier<Pipe<Double, Boolean>> detectorSupplier) {
        this.detectorSupplier = detectorSupplier;
    }

    private static class SupplierToPipes {
        Pipe<Double,Boolean>[] detectors;

        public SupplierToPipes(Supplier<Pipe<Double, Boolean>> detectorSupplier, int features) {
            detectors = new Pipe[features];
            for(int i=0;i<features;i++) {
                detectors[i] = detectorSupplier.get();
            }
        }

        public Pipe<Double,Boolean>[] get() {
            return detectors;
        }
    }

    @Override
    public Boolean[] execute(Double[] value, StreamContext context)  {
        if(detectors == null || votes == null) {
            this.detectors = new SupplierToPipes(detectorSupplier, context.getDimensionality()).get();
            this.votes = new Boolean[context.getDimensionality()];
        }

        if(value.length != detectors.length) {
            throw new MeanderException("Number of detectors must equal number of features!");
        }

        for(int i=0;i<detectors.length;i++) {
            votes[i] = detectors[i].execute(value[i], context);
        }

        return votes;
    }

    @Override
    public void reset() {
        if (detectors == null) {
            return;
        }
        for (Pipe pipe : detectors) {
            if (pipe.needReset()) {
                pipe.reset();
            }
        }
    }

    @Override
    public boolean needReset() {
        return true;
    }
}
