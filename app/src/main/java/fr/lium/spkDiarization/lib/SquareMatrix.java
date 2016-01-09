/**
 * <p>
 * SCMatrix
 * </p>
 *
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
 * @author <a href="mailto:gael.salaun@univ-lemans.fr">Gael Salaun</a>
 * @author <a href="mailto:teva.merlin@lium.univ-lemans.fr">Teva Merlin</a>
 * @version v2.0
 * <p/>
 * Copyright (c) 2007-2009 Universite du Maine. All Rights Reserved. Use is subject to license terms.
 * <p/>
 * THIS SOFTWARE IS PROVIDED BY THE "UNIVERSITE DU MAINE" AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 * <p/>
 * General square matrix class
 */

package fr.lium.spkDiarization.lib;

/**
 * The Class SCMatrix.
 */
public class SquareMatrix implements Cloneable {

    /** The dimension of the matrix. */
    protected int dimension;

    /** The 2 times #dim. */
    protected int dimension2;

    /** The log determinant. */
    protected double logDeterminant;

    /** The data: a vector of double. */
    protected double[] data;

    /**
     * Instantiates a new square matrix.
     */
    public SquareMatrix() {
        dimension = 0;
        dimension2 = 0;
        logDeterminant = -1;
        data = null;
    }

    /**
     * Instantiates a new square matrix.
     *
     * @param _dim the _dim
     */
    public SquareMatrix(int _dim) {
        dimension = _dim;
        dimension2 = _dim * _dim;
        logDeterminant = -1.0;
        data = new double[dimension2];
        for (int i = 0; i < dimension2; i++) {
            data[i] = 0;
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() {
        SquareMatrix result = null;
        try {
            result = (SquareMatrix) (super.clone());
        } catch (CloneNotSupportedException e) {
        }
        if (data != null) {
            result.data = data.clone();
        }
        return result;
    }

    /**
     * Get a value.
     *
     * @param i the row
     * @param j the column
     *
     * @return the double
     */
    public double get(int i, int j) {
        return data[i * dimension + j];
    }

    /**
     * Get the dimension of the matrix.
     *
     * @return the dimension
     */
    public int getDimension() {
        return dimension;
    }

    /**
     * Get the log determinant.
     *
     * @return the log determinant
     */
    public double getLogDetminant() {
        return logDeterminant;
    }

    /**
     * Initialize all the value to #v.
     *
     * @param v the initialization value
     */
    public void init(double v) {
        for (int i = 0; i < dimension2; i++) {
            data[i] = v;
        }
    }

    /**
     * Invert the matrix.
     *
     * @param m is the result
     *
     * @return true, if invert
     */
    public boolean invert(SquareMatrix m) {
        System.out.println("Matrix: invert() not implemented");
        return false;
    }

    /**
     * Resize the matrix <b>warning if the matrix is bigger, the new value is 0.0</b>
     *
     * @param _dimension the new dimension
     */
    public void resize(int _dimension) {
        double[] old = new double[dimension2];
        for (int i = 0; i < dimension2; i++) {
            old[i] = data[i];
        }
        int old_dim = dimension;
        dimension = _dimension;
        dimension2 = dimension * dimension;
        logDeterminant = -1.0;
        data = new double[dimension2];
        for (int i = 0; i < dimension2; i++) {
            data[i] = 0;
        }
        int min = Math.min(old_dim, dimension);
        for (int i = 0; i < min; i++) {
            int idim = i * dimension;
            data[idim] = old[i * old_dim];
        }
    }

    /**
     * Sets the value at row #i and column #j.
     *
     * @param i the i
     * @param j the j
     * @param value the value to set
     */
    public void set(int i, int j, double value) {
        data[i * dimension + j] = value;
    }

}
