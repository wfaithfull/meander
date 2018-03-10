package uk.ac.bangor.meander.detectors.controlchart;

import uk.ac.bangor.meander.detectors.AbstractUnivariateDetector;
import uk.ac.bangor.meander.detectors.windowing.FixedWindow;
import uk.ac.bangor.meander.detectors.windowing.Window;

/**
 * The control-chart inspired method from
 *
 * Faithfull, William J., and Ludmila I. Kuncheva. "On Optimum Thresholding of Multivariate Change Detectors."
 * Joint IAPR International Workshops on Statistical Techniques in Pattern Recognition (SPR) and Structural
 * and Syntactic Pattern Recognition (SSPR). Springer, Berlin, Heidelberg, 2014.
 *
 * @author Will Faithfull
 */
public class WindowDeviationChart extends AbstractUnivariateDetector {

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

    @Override
    public void update(Double input) {
        window.update(input);
    }

    @Override
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
}
