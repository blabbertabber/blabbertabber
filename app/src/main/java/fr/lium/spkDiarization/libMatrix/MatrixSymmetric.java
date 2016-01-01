/**
 * 
 * <p>
 * SymmetricMatrix
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
 *          Symmetric matrix class Need Lapack to invert the matrix (see http://www.netlib.org/java/f2j/).
 */

//
package fr.lium.spkDiarization.libMatrix;

import java.io.Serializable;
import java.util.logging.Logger;

import org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionInner;
import org.ejml.alg.dense.mult.MatrixDimensionException;
import org.ejml.alg.generic.GenericMatrixOps;
import org.ejml.data.DenseMatrix64F;
import org.ejml.data.Matrix64F;
import org.ejml.ops.CovarianceOps;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.SpkDiarizationLogger;

// import org.netlib.lapack.Dpptrf;
// import org.netlib.lapack.Dpptri;
// import org.netlib.util.intW;

// import Jama.Matrix;
// import Jama.CholeskyDecomposition;

/**
 * The Class SymmetricMatrix.
 */
public class MatrixSymmetric extends MatrixBase<MatrixSymmetric> implements Serializable, Cloneable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(MatrixSymmetric.class.getName());

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
	public MatrixSymmetric(double data[][]) {
		mat = new DenseMatrix64F(data);
		if (numCols() != numRows()) {
			throw new MatrixDimensionException("need to be a square numCols == numRows");
		}
		if (data[0][1] != data[1][0]) {
			throw new MatrixDimensionException("need to be a symetric array data[0][1] != data[1][0]");
		}
	}

	/**
	 * Creates a new matrix that is initially set to zero with the specified dimensions.
	 * 
	 * @param size The number of rows and columns in the matrix.
	 * @see org.ejml.data.DenseMatrix64F#DenseMatrix64F(int, int)
	 */
	public MatrixSymmetric(int size) {
		mat = new DenseMatrix64F(size, size);
	}

	/**
	 * Creats a new SimpleMatrix which is identical to the original.
	 * 
	 * @param orig The matrix which is to be copied. Not modified.
	 */
	public MatrixSymmetric(MatrixSymmetric orig) {
		this.mat = orig.mat.copy();
	}

	/**
	 * Creates a new SimpleMatrix which is a copy of the DenseMatrix64F.
	 * 
	 * @param orig The original matrix whose value is copied. Not modified.
	 */
	public MatrixSymmetric(DenseMatrix64F orig) {
		if (orig.numCols != orig.numRows) {
			throw new MatrixDimensionException("need to be a square numCols == numRows");
		}
		if (orig.get(0, 1) != orig.get(1, 0)) {
			throw new MatrixDimensionException("need to be a symetric array data[0][1] != data[1][0]");
		}
		this.mat = orig.copy();
	}

	/**
	 * Creates a new SimpleMatrix which is a copy of the Matrix64F.
	 * 
	 * @param orig The original matrix whose value is copied. Not modified.
	 */
	public MatrixSymmetric(Matrix64F orig) {
		if (orig.numCols != orig.numRows) {
			throw new MatrixDimensionException("need to be a square numCols == numRows");
		}
		if (orig.get(0, 1) != orig.get(1, 0)) {
			throw new MatrixDimensionException("need to be a symetric array data[0][1] != data[1][0]");
		}
		this.mat = new DenseMatrix64F(orig.numRows, orig.numCols);
		GenericMatrixOps.copy(orig, mat);
	}

	/**
	 * Instantiates a new matrix symmetric.
	 */
	public MatrixSymmetric() {
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
	 * Check if the value at row #i and column #j is a positive value.
	 * 
	 * @param i the row #i
	 * @param j the column #j
	 * 
	 * @return true, if successful
	 */
	public boolean checkPositifValue(int i, int j) {
		double value = mat.get(i, j);
		if (value <= 0.0) {
			mat.set(i, j, Double.MIN_VALUE);
			mat.set(j, i, Double.MIN_VALUE);
			return false;
		}
		return true;
	}

	/**
	 * Invert.
	 * 
	 * @param matInv the mat inv
	 * @return true, if successful
	 */
	public boolean invert(MatrixSymmetric matInv) {
		return CovarianceOps.invert(mat, matInv.mat);
	}

	/**
	 * Cholesky det manual.
	 * 
	 * @return the double
	 * @throws DiarizationException the diarization exception
	 */
	public double choleskyDetManual() throws DiarizationException {
		MatrixSymmetric m = this.copy();
		double[] x = new double[numCols()];
		double logDet = 0;
		for (int i = 0; i < numCols(); i++) {
			for (int j = i; j < numCols(); j++) {
				double sum = get(i, j);
				for (int k = i - 1; k >= 0; k--) {
					sum -= (m.get(k, i) * m.get(k, j));
				}
				if (i == j) {
					if (sum <= 0.0) {
						throw new DiarizationException("Matrix is not positive definite ");
					}
					x[i] = Math.sqrt(sum);
				} else {
					m.set(i, j, sum / x[i]);
				}
			}
		}
		for (int k = 0; k < numCols(); k++) {
			logDet += Math.log(x[k]);
		}
		logDet *= 2.0;
		return logDet;
	}

	/*
	 * public double choleskyLLtJamaManual() throws DiarizationException { Matrix jm, l; CholeskyDecomposition chol; jm = new Matrix(numCols(), numCols()); for (int i = 0; i < numCols(); i++) { for (int j = 0; j < numCols(); j++) { jm.set(i, j,
	 * mat.get(i, j)); } } /*for (int i = 0; i < numCols(); i++) { String line = ""; for (int j = 0; j < numCols(); j++) { line += jm.get(i, j)+"/"+mat.get(i, j)+"/"+mat.data[i*numCols()+j]+" "; } logger.info(line); }
	 * logger.info("det jama: "+Math.log(jm.det())); chol = jm.chol(); if (! chol.isSPD()) { System.out.println("Warning \t symMatrix is not positive definite"); throw new DiarizationException("false"); } double logDeterminant = 0; l = chol.getL();
	 * // Compute determinant and log-determinant from it. for (int i = 0; i < numCols(); i++) { double v = l.get(i, i); if (v <= 0) { System.out.println("Warning \t variance is null"); throw new DiarizationException("false"); } double lv =
	 * Math.log(v); logDeterminant += lv; } logDeterminant *= 2.0; return logDeterminant; }
	 */

	/**
	 * Cholesky det l lt.
	 * 
	 * @return the double
	 * @throws DiarizationException the diarization exception
	 */
	public double choleskyDetLLt() throws DiarizationException {
		CholeskyDecompositionInner cholesky = new CholeskyDecompositionInner(true);
		cholesky.decompose(mat.copy());
		DenseMatrix64F t = cholesky.getT();
		double logDet = 0.0;
		for (int k = 0; k < t.numCols; k++) {
			logDet += Math.log(t.unsafe_get(k, k));
		}
		logDet *= 2;
		return logDet;
	}

	/*
	 * public double choleskyDetLLt2() throws DiarizationException { CholeskyDecompositionInner cholesky = new CholeskyDecompositionInner(false); cholesky.decompose(mat.copy()); DenseMatrix64F t = cholesky.getT(); double logDet = 0.0; for (int k = 0;
	 * k < t.numCols; k++) { logDet += Math.log(t.get(k, k)); } logDet *= 2; return logDet; }
	 */

/*
 * public double choleskyDetLDLt() throws DiarizationException { CholeskyDecompositionLDL cholesky = new CholeskyDecompositionLDL(); cholesky.decompose(mat.copy()); double[] d = cholesky.getD(); double logDet = 0.0; for (int k = 0; k < getSize();
 * k++) { logDet += Math.log(d[k]); } logDet *= 2.0; return logDet; }
 */

	/**
	 * Log determinant.
	 * 
	 * @return the double
	 * @throws DiarizationException the diarization exception
	 */
	public double logDeterminant() throws DiarizationException {
		return choleskyDetLLt();

		/*
		 * CholeskyDecompositionInner cholesky = new CholeskyDecompositionInner(); cholesky.decompose(mat); DenseMatrix64F t = cholesky.getT(); double logDet = 1.0; for (int k = 0; k < t.numCols; k++) { logDet += Math.log(t.get(k, k)); } logDet *= 2;
		 * return logDet;
		 */

		/*
		 * CholeskyDecompositionLDL cholesky = new CholeskyDecompositionLDL(); cholesky.decompose(mat); double[] d = cholesky.getD(); double logDet = 1.0; for (int k = 0; k < getSize(); k++) { logDet += Math.log(d[k]); } logDet *= 2.0; return logDet;
		 */
	}

	/*
	 * public double determinant() { CholeskyDecompositionLDL cholesky = new CholeskyDecompositionLDL(); cholesky.decompose(mat); double[] d = cholesky.getD(); double det = 1.0; for (int k = 0; k < getSize(); k++) { det *= d[k]; } det = det * det;
	 * return det; }
	 */

	/**
	 * Cholesky at.
	 * 
	 * @return the matrix square
	 * @throws DiarizationException the diarization exception
	 */
	public MatrixSquare choleskyAt() throws DiarizationException {
		CholeskyDecompositionInner choleskyDecomposition = new CholeskyDecompositionInner(true);
		choleskyDecomposition.decompose(mat.copy());
		MatrixSquare T = new MatrixSquare(choleskyDecomposition.getT());
		return T.transpose();
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
		mat.set(j, i, k);
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
		mat.unsafe_set(j, i, k);
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

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.libMatrix.MatrixBase#copy()
	 */
	@Override
	public MatrixSymmetric copy() {
		return super.copy();
	}

	@Override
	public MatrixSymmetric clone() throws CloneNotSupportedException {
		if (SpkDiarizationLogger.DEBUG) logger.info("clone MatrixSymmetric");
		return copy();
	}

	/**
	 * Creates the matrix.
	 * 
	 * @param size the size
	 * @return the matrix symmetric
	 */
	protected MatrixSymmetric createMatrix(int size) {
		return new MatrixSymmetric(size);
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.libMatrix.MatrixBase#createMatrix(int, int)
	 */
	@Override
	protected MatrixSymmetric createMatrix(int numRows, int numCols) {
		if (numCols() != numRows()) {
			throw new MatrixDimensionException("need to be a square numCols == numRows");
		}
		return createMatrix(numRows);
	}

}
