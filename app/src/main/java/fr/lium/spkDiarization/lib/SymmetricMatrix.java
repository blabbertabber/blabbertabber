/**
 * <p>
 * SymmetricMatrix
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
 * Symmetric matrix class
 * Need Lapack to invert the matrix (see http://www.netlib.org/java/f2j/).
 */

//
package fr.lium.spkDiarization.lib;

import org.netlib.lapack.Dpptrf;
import org.netlib.lapack.Dpptri;
import org.netlib.util.intW;


/**
 * The Class SymmetricMatrix.
 */
public class SymmetricMatrix extends SquareMatrix {

    /**
     * Instantiates a new symmetric matrix.
     */
    public SymmetricMatrix() {
        super();
    }

    /**
     * Instantiates a new symmetric matrix.
     *
     * @param _dimension the dimension
     */
    public SymmetricMatrix(int _dimension) {
        super();
        dimension = _dimension;
        dimension2 = ((dimension * (dimension + 1)) / 2);
        logDeterminant = -1.0;
        data = new double[dimension2];
        for (int i = 0; i < dimension2; i++) {
            data[i] = 0;
        }
    }

    /**
     * Check if the value at row #i and column #j is a positive value.
     *
     * @param i the row #i
     * @param j the column #j
     *
     * @return true, if successful
     */
    public boolean checkPositifValue(int i, int j) {
        double value = get(i, j);
        if (value <= 0.0) {
//			set(i, j, 1.0e-8);
            set(i, j, Double.MIN_VALUE);
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.SquareMatrix#get(int, int)
     */
    @Override
    public double get(int i, int j) {
        if (i <= j) {
            return data[((j * (j + 1)) / 2) + i];
        } else {
            return data[((i * (i + 1)) / 2) + j];
        }
    }

    /**
     * Increment value at row #i and column #j.
     *
     * @param i the row #i
     * @param j the column #j
     * @param k the value
     */
    public void increment(int i, int j, double k) {
        if (i <= j) {
            data[((j * (j + 1)) / 2) + i] += k;
        } else {
            data[((i * (i + 1)) / 2) + j] += k;
        }
    }

    /**
     * Invert function based on Cholesky.
     *
     * @param m the m
     *
     * @return the symmetric matrix
     *
     * @throws DiarizationException the diarization exception
     */
    public SymmetricMatrix invert(SymmetricMatrix m) throws DiarizationException {
        String uplo = "U";
        int n = dimension;
        int data_offset = 0;
        intW info = new intW(0);

        // m = new SCSymMatrix(this);
        m = (SymmetricMatrix) clone();

        Dpptrf.dpptrf(uplo, n, m.data, data_offset, info);

        m.logDeterminant = 0.0;
        if (info.val != 0) {
            System.out.println("Warning \t symMatrix is not positive definite");
            throw new DiarizationException("false");
        }

        for (int i = 0; i < dimension; i++) {
            double v = m.get(i, i);
            if (v <= 0) {
                System.out.println("Warning \t variance is null");
                throw new DiarizationException("false");
            }

            double lv = Math.log(v);
            logDeterminant += lv;
            m.logDeterminant -= lv;

        }
        logDeterminant *= 2.0;
        m.logDeterminant *= 2.0;

        Dpptri.dpptri(uplo, n, m.data, data_offset, info);

        if (info.val != 0) {
            System.out.println("warning \t can't invert, singular matrix");
            throw new DiarizationException("false");
        }
        return m;
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.SquareMatrix#set(int, int, double)
     */
    @Override
    public void set(int i, int j, double k) {
        if (i <= j) {
            data[((j * (j + 1)) / 2) + i] = k;
        } else {
            data[((i * (i + 1)) / 2) + j] = k;
        }
    }

}
