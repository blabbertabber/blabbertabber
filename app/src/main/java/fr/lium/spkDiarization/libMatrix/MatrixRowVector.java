/**
 * 
 * <p>
 * DoubleVector
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
 *          Vector matrix class. Used to store mean model.
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

// TODO: Auto-generated Javadoc
/**
 * The Class DoubleVector.
 */
public class MatrixRowVector extends MatrixBase<MatrixRowVector> implements Serializable, Cloneable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(MatrixRowVector.class.getName());

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
	public MatrixRowVector(double data[]) {
		mat = new DenseMatrix64F(data.length);
		for (int i = 0; i < data.length; i++) {
			mat.data[i] = data[i];
		}
	}

	/**
	 * Creates a new matrix that is initially set to zero with the specified dimensions.
	 * 
	 * @param size The number of rows and columns in the matrix.
	 * @see org.ejml.data.DenseMatrix64F#DenseMatrix64F(int, int)
	 */
	public MatrixRowVector(int size) {
		mat = new DenseMatrix64F(size, 1);
	}

	/**
	 * Creats a new SimpleMatrix which is identical to the original.
	 * 
	 * @param orig The matrix which is to be copied. Not modified.
	 */
	public MatrixRowVector(MatrixRowVector orig) {
		this.mat = orig.mat.copy();
	}

	/**
	 * Creates a new SimpleMatrix which is a copy of the DenseMatrix64F.
	 * 
	 * @param orig The original matrix whose value is copied. Not modified.
	 */
	public MatrixRowVector(DenseMatrix64F orig) {
		if (orig.numCols != 1) {
			throw new MatrixDimensionException("need to be a vector with numCols equal to 1");
		}
		this.mat = orig.copy();
	}

	/**
	 * Creates a new SimpleMatrix which is a copy of the Matrix64F.
	 * 
	 * @param orig The original matrix whose value is copied. Not modified.
	 */
	public MatrixRowVector(Matrix64F orig) {
		if (orig.numCols != 1) {
			throw new MatrixDimensionException("need to be a vector with numCols equal to 1");
		}
		this.mat = new DenseMatrix64F(orig.numRows, orig.numCols);
		GenericMatrixOps.copy(orig, mat);
	}

	/**
	 * Instantiates a new matrix row vector.
	 */
	public MatrixRowVector() {
	}

	/**
	 * Gets the size.
	 * 
	 * @return the size
	 */
	public int getSize() {
		return getNumElements();
	}

	/**
	 * Check positive value.
	 * 
	 * @param i the element #i
	 * 
	 * @return true, if successful
	 */
	public boolean checkPositifValue(int i) {
		double value = get(i);
		if (value <= 0.0) {
			set(i, Double.MIN_VALUE);
			return false;
		}
		return true;
	}

// /**
// * Gets the element #i.
// *
// * @param i the element #i
// *
// * @return the double
// */
// public double get(int i) {
// return mat.get(i);
// }

// /*
// * (non-Javadoc)
// * @see fr.lium.spkDiarization.lib.SquareMatrix#get(int, int)
// */
// @Override
// public double get(int i, int j) {
// try {
// throw new DiarizationException("Vector, only get(i) available");
// } catch (DiarizationException e) {
// // TODO Auto-generated catch block
// e.printStackTrace();
// }
// return 0;
// }

// /**
// * Increment element #i of #step.
// *
// * @param i the element #i
// * @param step the step
// */
// public void increment(int i, double step) {
// mat.data[i] = mat.data[i] + step;
// }

	/**
	 * Unsafe Gets the element #i.
	 * 
	 * @param i the element #i
	 * 
	 * @return the double
	 */
	public double unsafe_get(int i) {
		return mat.data[i];
	}

	/**
	 * UnSafe Sets #value in the element #i.
	 * 
	 * @param i the i
	 * @param value the value
	 */
	public void unsafe_set(int i, double value) {
		mat.data[i] = value;
	}

// /**
// * UnSafe Sets #value in the element #i.
// *
// * @param i the i
// * @param value the value
// */
// public void set(int i, double value) {
// mat.set(0, i, value);
// }

// /*
// * (non-Javadoc)
// * @see fr.lium.spkDiarization.lib.SquareMatrix#set(int, int, double)
// */
// @Override
// public void set(int i, int j, double k) {
// set(i, k);
// try {
// throw new DiarizationException("Vector, only get(i) available");
// } catch (DiarizationException e) {
// // TODO Auto-generated catch block
// e.printStackTrace();
// }
// }
//

	/**
	 * Adds the.
	 * 
	 * @param i the i
	 * @param value the value
	 */
	public void add(int i, double value) {
		set(i, get(i) + value);
	}

	/**
	 * Times.
	 * 
	 * @param i the i
	 * @param value the value
	 */
	public void times(int i, double value) {
		set(i, get(i) * value);
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.libMatrix.MatrixBase#copy()
	 */
	@Override
	public MatrixRowVector copy() {
		return super.copy();
	}

	@Override
	public MatrixRowVector clone() throws CloneNotSupportedException {
		if (SpkDiarizationLogger.DEBUG) logger.info("clone MatrixRowVector");
		return copy();
	}

	/**
	 * Creates the matrix.
	 * 
	 * @param size the size
	 * @return the matrix row vector
	 */
	protected MatrixRowVector createMatrix(int size) {
		return new MatrixRowVector(size);
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.libMatrix.MatrixBase#createMatrix(int, int)
	 */
	@Override
	protected MatrixRowVector createMatrix(int numRows, int numCols) {
		if (numCols != 1) {
			throw new MatrixDimensionException("need to be a vector with numCols equal to 1");
		}
		return createMatrix(numRows);
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.Matrix.MatrixBase#invert()
	 */
	@Override
	public MatrixRowVector invert() {
		logger.warning("compte the inverse of a vector, not a MatrixDiagonal");
		return super.invert();
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.libMatrix.MatrixBase#determinant()
	 */
	@Override
	public double determinant() {
		logger.warning("compte the determinant of a vector, not a MatrixDiagonal");
		return super.determinant();
	}

}
