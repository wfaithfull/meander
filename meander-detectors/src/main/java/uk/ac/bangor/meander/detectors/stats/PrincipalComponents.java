package uk.ac.bangor.meander.detectors.stats;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.ac.bangor.meander.detectors.CollectionUtils;

import java.util.Arrays;

/**
 * Computes a conventional PCA transformation on an m-by-n matrix of m examples and n features.
 * <p>
 * Uses eigenvalue decomposition of the computed covariance matrix.
 *
 * @author Will Faithfull
 */
public class PrincipalComponents {

    private Matrix data;
    @Getter
    private Matrix eigenvalues;
    @Getter
    private Matrix coeff;

    public PrincipalComponents(Matrix data) {
        this.data = data;
        data = new Matrix(CollectionUtils.subtractColMeans(data.getArray()));
        Matrix covariance = new MeanAndCovariance(data).getCovariance();
        EigenvalueDecomposition evd = covariance.eig();
        double[] diag = diag(evd.getD());
        Permutation permutation = sortDescending(diag);
        this.coeff = permuteColumns(evd.getV(), permutation.getPermutation());
        this.eigenvalues = createDiagonal(permutation.getValues());
    }

    public PrincipalComponents(double[][] data) {
        this(new Matrix(data));
    }

    public PrincipalComponents(Double[][] data) {
        this(CollectionUtils.unbox(data));
    }

    private static double[] diag(Matrix m) {
        double[] diag = new double[m.getRowDimension()];
        for (int i = 0; i < m.getRowDimension(); i++)
            diag[i] = m.get(i, i);
        return diag;
    }

    private static Matrix createDiagonal(double[] diagonal) {
        Matrix m = new Matrix(diagonal.length, diagonal.length);
        for (int i = 0; i < diagonal.length; i++)
            m.set(i, i, diagonal[i]);
        return m;
    }

    private static Permutation sortDescending(double[] array) {
        Position[] positions = new Position[array.length];
        for (int i = 0; i < array.length; i++) {
            positions[i] = new Position(i, array[i]);
        }
        Arrays.sort(positions);

        double[] sorted = new double[array.length];
        int[] permutation = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            sorted[i] = positions[i].value;
            permutation[i] = positions[i].position;
        }
        return new Permutation(permutation, sorted);
    }

    private static Matrix permuteColumns(Matrix m, int[] permutation) {
        Matrix permuted = new Matrix(m.getRowDimension(), m.getColumnDimension());
        for (int c = 0; c < permuted.getColumnDimension(); c++) {
            int copyFrom = permutation[c];
            for (int r = 0; r < permuted.getRowDimension(); r++) {
                permuted.set(r, c, m.get(r, copyFrom));
            }
        }
        return permuted;
    }

    public Matrix getScores() {
        return transform(data);
    }

    public Matrix transform(Matrix data) {
        return data.times(coeff);
    }

    @AllArgsConstructor
    @Getter
    static class Permutation {
        int[]    permutation;
        double[] values;
    }

    @AllArgsConstructor
    @Getter
    static class Position implements Comparable<Position> {
        int    position;
        double value;

        @Override
        public int compareTo(Position other) {
            if (this.value < other.value)
                return 1;
            if (this.value == other.value)
                return 0;
            return -1;
        }
    }

}
