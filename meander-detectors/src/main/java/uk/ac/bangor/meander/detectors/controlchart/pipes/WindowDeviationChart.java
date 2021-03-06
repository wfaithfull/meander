package uk.ac.bangor.meander.detectors.controlchart.pipes;

import uk.ac.bangor.meander.detectors.Pipe;
import uk.ac.bangor.meander.detectors.windowing.support.FixedWindow;
import uk.ac.bangor.meander.detectors.windowing.support.Window;
import uk.ac.bangor.meander.streams.StreamContext;

/**
 * The control-chart inspired method from
 *
 * Faithfull, William J., and Ludmila I. Kuncheva. "On Optimum Thresholding of Multivariate Change Detectors."
 * Joint IAPR International Workshops on Statistical Techniques in Pattern Recognition (SPR) and Structural
 * and Syntactic Pattern Recognition (SSPR). Springer, Berlin, Heidelberg, 2014.
 *
 * @author Will Faithfull
 */
public class WindowDeviationChart implements Pipe<Double, Boolean> {

    private Window<Double> window;
    private double         sqrt_T;
    private final double   alpha;

    public WindowDeviationChart(int wsz, double alpha) {
        window = new FixedWindow<>(wsz, Double.class);
        sqrt_T = Math.sqrt(wsz);
        this.alpha = alpha;
    }

    public WindowDeviationChart(int wsz) {
        this(wsz, 1.96);
    }

    public void update(Double input) {
        window.update(input);
    }

    public boolean isChangeDetected() {
        double mean = mean();
        double lim = alpha*(std(mean)/sqrt_T);
        return mean > lim || mean < mean-lim;
    }

    private double mean() {
        if(window.size() < window.capacity())
            return 0;

        Double[] data = window.getElements();

        double sum = 0;

        for(int i = 0; i < data.length; i++) {
            sum += data[i];
        }

        return sum / data.length;
    }

    private double std(double mean) {
        if(window.size() < window.capacity())
            return 0;

        Double[] data = window.getElements();

        double var = 0;

        for(int i = 0; i < data.length; ++i) {
            var += data[i] - mean;
        }

        return Math.sqrt(var / (data.length-1));
    }

    @Override
    public Boolean execute(Double value, StreamContext context) {
        update(value);
        return isChangeDetected();
    }
}
