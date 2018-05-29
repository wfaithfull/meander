package uk.ac.bangor.meander.detectors.stats.support;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.ac.bangor.meander.MeanderException;
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
    @Getter
    Permutation permutation;

    public PrincipalComponents(Matrix data) {
        this.data = data;
        data = new Matrix(CollectionUtils.subtractColMeans(data.getArray()));
        Matrix covariance = new MeanAndCovariance(data).getCovariance();
        EigenvalueDecomposition evd = covariance.eig();
        double[] diag = diag(evd.getD());
        permutation = sortDescending(diag);
        this.coeff = permuteColumns(evd.getV(), permutation.getPermutation());
        this.eigenvalues = createDiagonal(permutation.getValues());
    }

    public PrincipalComponents(double[][] data) {
        this(new Matrix(data));
    }

    public PrincipalComponents(Double[][] data) {
        this(CollectionUtils.unbox(data));
    }

    private PrincipalComponents(Matrix data, Matrix eigenvalues, Matrix coeff, Permutation permutation) {
        this.data = data;
        this.eigenvalues = eigenvalues;
        this.coeff = coeff;
    }

    private static double[] diag(Matrix m) {
        double[] diag = new double[m.getRowDimension()];
        for (int i = 0; i < m.getRowDimension(); i++)
            diag[i] = m.get(i, i);
        return diag;
    }

    public double[] getExplained() {
        double[] eig = diag(eigenvalues);

        double total = 0;
        for (int i = 0; i < eig.length; i++) {
            total += eig[i];
        }

        double[] explained = new double[eig.length];

        for (int i = 0; i < eig.length; i++) {
            explained[i] = eig[i] / total;
        }

        return explained;
    }

    public int[] getTop(double cutoff) {
        if (cutoff < 0 || cutoff > 1) {
            throw new MeanderException("Cutoff must be between 0 and 1.");
        }

        double[] explained = getExplained();

        double cumsum = 0;

        int cut = -1;

        for (int i = 0; i < explained.length; i++) {
            cumsum += explained[i];

            if (cumsum >= cutoff) {
                cut = i;
                break;
            }
        }

        return Arrays.copyOfRange(permutation.getPermutation(), 0, cut);
    }

    public boolean[] permutationToKeep(int[] permutation) {
        boolean[] keep = new boolean[data.getColumnDimension()];

        for (int i = 0; i < permutation.length; i++) {
            keep[permutation[i]] = true;
        }

        return keep;
    }

    public int[] getBottom(double cutoff) {
        if (cutoff < 0 || cutoff > 1) {
            throw new MeanderException("Cutoff must be between 0 and 1.");
        }

        double[] explained = getExplained();

        double cumsum = 0;

        int cut = -1;

        for (int i = explained.length - 1; i >= 0; i--) {
            cumsum += explained[i];

            if (cumsum >= cutoff) {
                cut = i;
                break;
            }
        }

        return Arrays.copyOfRange(permutation.getPermutation(), cut, explained.length);
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
