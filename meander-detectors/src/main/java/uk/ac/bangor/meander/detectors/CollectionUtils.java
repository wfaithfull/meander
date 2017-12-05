package uk.ac.bangor.meander.detectors;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Will Faithfull
 *
 * Utility class for collection operations
 */
public class CollectionUtils {

    private CollectionUtils() {}

    public static double[][] toArray(List<double[]> list) {
        double[][] array = new double[list.size()][list.get(0).length];
        for(int i=0;i<list.size();i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    public static double[] to1DArray(List<Double> doubleList) {
        double[] array = new double[doubleList.size()];

        for(int i=0;i<doubleList.size();i++) {
            array[i] = doubleList.get(i);
        }

        return array;
    }

    public static double[] colMean(double[][] matrix) {
        double[] mean = new double[matrix[0].length];
        for(int column=0;column<matrix[0].length;column++) {
            double colTotal = 0;
            for(int row=0;row<matrix.length;row++) {
                colTotal += matrix[row][column];
            }
            mean[column] = colTotal / matrix.length;
        }
        return mean;
    }

    public static double[][] subtractRowMeans(double[][] input) {
        for(double[] row : input) {
            double mean = mean(row);
            for(int i=0;i<row.length;i++) {
                row[i] = row[i] - mean;
            }
        }
        return input;
    }

    public static double[][] subtractColMeans(double[][] input) {
        double[] colMeans = colMean(input);
        for(int column=0;column<input[0].length;column++) {
            double colTotal = 0;
            for(int row=0;row<input.length;row++) {
                input[row][column] = input[row][column] - colMeans[column];
            }
        }
        return input;
    }

    public static double mean(double[] array) {
        double sum = 0;
        for(double d : array)
            sum += d;

        return sum / array.length;
    }

    public static double median(double[] array) {
        Arrays.sort(array);
        double median;
        if (array.length % 2 == 0)
            median = ((double)array[array.length/2] + (double)array[array.length/2 - 1])/2;
        else
            median = (double) array[array.length/2];
        return median;
    }

    public static double min(double[] array) {
        double min = Double.POSITIVE_INFINITY;
        for(int i=0;i<array.length;i++) {
            if(array[i] < min)
                min = array[i];
        }
        return min;
    }

    public static void iterate(double[] array, Consumer<Integer> visitor) {

    }

    public static String toMatlabFormat(double[][] data) {
        StringBuilder sb = new StringBuilder();
        sb.append("[ ");
        for(int i=0;i<data.length;i++) {
            for(int j=0;j<data[i].length;j++) {
                sb.append(data[i][j] + " ");
            }
            if(i<data.length)
                sb.append(";");
            sb.append("\n");
        }
        sb.append("]");
        return sb.toString();
    }

    public static String toMatlabFormat(double[] data) {
        StringBuilder sb = new StringBuilder();
        sb.append("[ ");
        for(int i=0;i<data.length;i++) {
            sb.append(data[i] + " ");
        }
        sb.append("]");
        return sb.toString();
    }
}
