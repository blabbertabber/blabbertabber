package fr.lium.spkDiarization.libMatrix;

import fr.lium.spkDiarization.lib.SpkDiarizationLogger;
import java.io.Serializable;
import java.util.logging.Logger;

import org.ejml.alg.dense.mult.MatrixDimensionException;
import org.ejml.data.DenseMatrix64F;
import org.ejml.data.Matrix64F;

/**
 * The Class MatrixDiagonal.
 */
public class MatrixDiagonal extends MatrixBase<MatrixDiagonal> implements Serializable, Cloneable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(MatrixDiagonal.class.getName());

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
	public MatrixDiagonal(double data[]) {
		mat = new DenseMatrix64F(data.length, 1);
		for (int i = 0; i < data.length; i++) {
			mat.data[i] = data[i];
			// logger.info("new mat Diag 0: "+mat.numRows+", "+mat.numCols+")");
		}
	}

	/**
	 * Creates a new matrix that is initially set to zero with the specified dimensions.
	 * 
	 * @param size The number of rows and columns in the matrix.
	 * @see org.ejml.data.DenseMatrix64F#DenseMatrix64F(int, int)
	 */
	public MatrixDiagonal(int size) {
		mat = new DenseMatrix64F(size, 1);
		// logger.info("new mat Diag 1: "+mat.numRows+", "+mat.numCols+")");
	}

	/**
	 * Creats a new SimpleMatrix which is identical to the original.
	 * 
	 * @param orig The matrix which is to be copied. Not modified.
	 */
	public MatrixDiagonal(MatrixRowVector orig) {
		this.mat = orig.mat.copy();
		// logger.info("new mat Diag 2: "+mat.numRows+", "+mat.numCols+")");
	}

	/**
	 * Creates a new SimpleMatrix which is a copy of the DenseMatrix64F.
	 * 
	 * @param orig The original matrix whose value is copied. Not modified.
	 */
	public MatrixDiagonal(DenseMatrix64F orig) {
		if (orig.numRows != orig.numCols) {
			throw new MatrixDimensionException("need to be a square matrix");
		}
		this.mat = new DenseMatrix64F(orig.numRows, 1);
		for (int i = 0; i < orig.numCols; i++) {
			mat.data[i] = orig.get(i, i);
		}
		// logger.info("new mat Diag 3: "+mat.numRows+", "+mat.numCols+")");
	}

	/**
	 * Creates a new SimpleMatrix which is a copy of the Matrix64F.
	 * 
	 * @param orig The original matrix whose value is copied. Not modified.
	 */
	public MatrixDiagonal(Matrix64F orig) {
		if (orig.numRows != orig.numCols) {
			throw new MatrixDimensionException("need to be a square matrix");
		}
		this.mat = new DenseMatrix64F(orig.numRows, 1);
		for (int i = 0; i < orig.numCols; i++) {
			mat.data[i] = orig.get(i, i);
		}
		// logger.info("new mat Diag 4: "+mat.numRows+", "+mat.numCols+")");
	}

	/**
	 * Instantiates a new matrix diagonal.
	 */
	public MatrixDiagonal() {
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

	/**
	 * Sets the.
	 * 
	 * @param i the i
	 * @param j the j
	 * @param k the k
	 */
	public void set(int i, int j, double k) {
		if ((i != j) && (k != 0)) {
			throw new MatrixDimensionException("out of the diagonal");
		}
		mat.set(i, k);
	}

	/**
	 * Adds the.
	 * 
	 * @param i the i
	 * @param j the j
	 * @param k the k
	 */
	public void add(int i, int j, double k) {
		if ((i != j) && (k != 0)) {
			throw new MatrixDimensionException("out of the diagonal not equal 0");
		}
		set(i, k + get(i));
	}

	/**
	 * Times.
	 * 
	 * @param i the i
	 * @param j the j
	 * @param k the k
	 */
	public void times(int i, int j, double k) {
		if ((i != j) && (k != 0)) {
			throw new MatrixDimensionException("out of the diagonal not equal 0");
		}
		set(i, k * get(i));
	}

	/**
	 * Gets the.
	 * 
	 * @param i the i
	 * @param j the j
	 * @return the double
	 */
	public double get(int i, int j) {
		if (i != j) {
			return 0;
		}
		return mat.get(i);
	}

	/**
	 * Unsafe_set.
	 * 
	 * @param i the i
	 * @param j the j
	 * @param k the k
	 */
	public void unsafe_set(int i, int j, double k) {
		mat.data[i] = k;
	}

	/**
	 * Unsafe_get.
	 * 
	 * @param i the i
	 * @param j the j
	 * @return the double
	 */
	public double unsafe_get(int i, int j) {
		if (i != j) {
			return 0;
		}
		return mat.data[i];
	}
	/**
	 * Unsafe_set.
	 * 
	 * @param i the i
	 * @param j the j
	 * @param k the k
	 */
	public void unsafe_set(int i, double k) {
		mat.data[i] = k;
	}

	/**
	 * Unsafe_get.
	 * 
	 * @param i the i
	 * @param j the j
	 * @return the double
	 */
	public double unsafe_get(int i) {
		return mat.data[i];
	}

	/**
	 * Invert.
	 * 
	 * @param inverse the m
	 * 
	 * @return true, if successful
	 */
	public boolean invert(MatrixDiagonal inverse) {
		for (int i = 0; i < getSize(); i++) {
			inverse.set(i, 1.0 / mat.data[i]);
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.libMatrix.MatrixBase#determinant()
	 */
	@Override
	public double determinant() {
		double det = 1.0;
		for (int i = 0; i < getSize(); i++) {
			det *= mat.data[i];
		}
		return det;
	}

	/**
	 * Log determinant.
	 * 
	 * @return the double
	 */
	public double logDeterminant() {
		double det = 0.0;
		for (int i = 0; i < getSize(); i++) {
			det += Math.log(mat.data[i]);
		}
		return det;
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.libMatrix.MatrixBase#copy()
	 */
	@Override
	public MatrixDiagonal copy() {
		return super.copy();
	}

	@Override
	public MatrixDiagonal clone() throws CloneNotSupportedException {
		if (SpkDiarizationLogger.DEBUG) logger.info("clone MatrixDiagonal");
		return copy();
	}

	/**
	 * Creates the matrix.
	 * 
	 * @param size the size
	 * @return the matrix diagonal
	 */
	protected MatrixDiagonal createMatrix(int size) {
		return new MatrixDiagonal(size);
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.libMatrix.MatrixBase#createMatrix(int, int)
	 */
	@Override
	protected MatrixDiagonal createMatrix(int numRows, int numCols) {
		if (!((numRows == numCols) || (numCols == 1))) {
			throw new MatrixDimensionException("need to be a vector numRows or bumCols nedd to be equal at 1 ("
					+ numRows + ", " + numCols + ")");
		}
		return createMatrix(numRows);
	}
}
