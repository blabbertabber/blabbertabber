/**
 * 
 * <p>
 * SCMatrix
 * </p>
 * 
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
 * @author <a href="mailto:gael.salaun@univ-lemans.fr">Gael Salaun</a>
 * @author <a href="mailto:teva.merlin@lium.univ-lemans.fr">Teva Merlin</a>
 * @version v2.0
 * 
 *          Copyright (c) 2007-2009 Universite du Maine. All Rights Reserved. Use is subject to license terms.
 * 
 *          THIS SOFTWARE IS PROVIDED BY THE "UNIVERSITE DU MAINE" AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *          DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *          USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *          ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 *          General square matrix class
 * 
 */

package fr.lium.spkDiarization.libMatrix;

import fr.lium.spkDiarization.lib.SpkDiarizationLogger;
import java.io.Serializable;
import java.util.logging.Logger;

import org.ejml.alg.dense.mult.MatrixDimensionException;
import org.ejml.alg.generic.GenericMatrixOps;
import org.ejml.data.DenseMatrix64F;
import org.ejml.data.Matrix64F;

/**
 * The Class SCMatrix.
 */
public class MatrixSquare extends MatrixBase<MatrixSquare> implements Serializable, Cloneable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(MatrixSquare.class.getName());

	/**
	 * <p>
	 * Creates a matrix with the values and shape defined by the 2D array 'data'. It is assumed that 'data' has a row-major formatting:<br>
	 * <br>
	 * data[ row ][ column ]
	 * </p>
	 * 
	 * @param data 2D array representation of the matrix. Not modified.
	 * @see org.ejml.data.DenseMatrix64F#DenseMatrix64F(double[][])
	 */
	public MatrixSquare(double data[][]) {
		mat = new DenseMatrix64F(data);
		if (numCols() != numRows()) {
			throw new MatrixDimensionException("need to be a square numCols == numRows");
		}
	}

	/**
	 * Creates a new matrix that is initially set to zero with the specified dimensions.
	 * 
	 * @param size The number of rows and columns in the matrix.
	 * @see org.ejml.data.DenseMatrix64F#DenseMatrix64F(int, int)
	 */
	public MatrixSquare(int size) {
		mat = new DenseMatrix64F(size, size);
	}

	/**
	 * Creats a new SimpleMatrix which is identical to the original.
	 * 
	 * @param orig The matrix which is to be copied. Not modified.
	 */
	public MatrixSquare(MatrixSquare orig) {
		this.mat = orig.mat.copy();
	}

	/**
	 * Creates a new SimpleMatrix which is a copy of the DenseMatrix64F.
	 * 
	 * @param orig The original matrix whose value is copied. Not modified.
	 */
	public MatrixSquare(DenseMatrix64F orig) {
		if (orig.numCols != orig.numRows) {
			throw new MatrixDimensionException("need to be a square numCols == numRows");
		}
		this.mat = orig.copy();
	}

	/**
	 * Creates a new SimpleMatrix which is a copy of the Matrix64F.
	 * 
	 * @param orig The original matrix whose value is copied. Not modified.
	 */
	public MatrixSquare(Matrix64F orig) {
		if (orig.numCols != orig.numRows) {
			throw new MatrixDimensionException("need to be a square numCols == numRows");
		}
		this.mat = new DenseMatrix64F(orig.numRows, orig.numCols);
		GenericMatrixOps.copy(orig, mat);
	}

	/**
	 * Instantiates a new matrix square.
	 */
	public MatrixSquare() {
	}

	/**
	 * Gets the size.
	 * 
	 * @return the size
	 */
	public int getSize() {
		return numCols();
	}

	/**
	 * Resize the matrix <b>warning if the matrix is bigger, the new value is 0.0</b>
	 * 
	 * @param size the new dimension
	 */
	public void resize(int size) {
		DenseMatrix64F old = mat.copy();
		mat = new DenseMatrix64F(size, size);

		int min = Math.min(old.numRows, size);
		for (int i = 0; i < min; i++) {
			mat.set(i * size, old.get(i * old.numRows));
		}
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.libMatrix.MatrixBase#copy()
	 */
	@Override
	public MatrixSquare copy() {
		return super.copy();
	}

	@Override
	public MatrixSquare clone() throws CloneNotSupportedException {
		if (SpkDiarizationLogger.DEBUG) logger.info("clone MatrixSquare");
		return copy();
	}

	/**
	 * Creates the matrix.
	 * 
	 * @param size the size
	 * @return the matrix square
	 */
	protected MatrixSquare createMatrix(int size) {
		return new MatrixSquare(size);
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.libMatrix.MatrixBase#createMatrix(int, int)
	 */
	@Override
	protected MatrixSquare createMatrix(int numRows, int numCols) {
		if (numCols() != numRows()) {
			throw new MatrixDimensionException("need to be a square numCols == numRows");
		}
		return createMatrix(numRows);
	}

	/**
	 * Sets the.
	 * 
	 * @param i the i
	 * @param j the j
	 * @param k the k
	 */
	public void set(int i, int j, double k) {
		mat.set(i, j, k);
	}

	/**
	 * Gets the.
	 * 
	 * @param i the i
	 * @param j the j
	 * @return the double
	 */
	public double get(int i, int j) {
		return mat.get(i, j);
	}

	/**
	 * Unsafe_set.
	 * 
	 * @param i the i
	 * @param j the j
	 * @param k the k
	 */
	public void unsafe_set(int i, int j, double k) {
		mat.unsafe_set(i, j, k);
	}

	/**
	 * Unsafe_get.
	 * 
	 * @param i the i
	 * @param j the j
	 * @return the double
	 */
	public double unsafe_get(int i, int j) {
		return mat.unsafe_get(i, j);
	}

	/**
	 * Adds the.
	 * 
	 * @param i the i
	 * @param j the j
	 * @param k the k
	 */
	public void add(int i, int j, double k) {
		set(i, j, k + get(i, j));
	}

	/**
	 * Times.
	 * 
	 * @param i the i
	 * @param j the j
	 * @param k the k
	 */
	public void times(int i, int j, double k) {
		set(i, j, k * get(i, j));
	}

}
