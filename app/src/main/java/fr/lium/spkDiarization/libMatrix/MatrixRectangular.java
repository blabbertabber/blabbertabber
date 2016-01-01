package fr.lium.spkDiarization.libMatrix;

import java.io.Serializable;
import java.util.logging.Logger;

import org.ejml.alg.generic.GenericMatrixOps;
import org.ejml.data.DenseMatrix64F;
import org.ejml.data.Matrix64F;

/**
 * The Class MatrixRectangular.
 */
public class MatrixRectangular extends MatrixBase<MatrixRectangular> implements Serializable, Cloneable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(MatrixRectangular.class.getName());

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
	public MatrixRectangular(double data[][]) {
		mat = new DenseMatrix64F(data);
	}

	/**
	 * Creates a new matrix that is initially set to zero with the specified dimensions.
	 * 
	 * @param numRows The number of rows in the matrix.
	 * @param numCols The number of columns in the matrix.
	 * @see org.ejml.data.DenseMatrix64F#DenseMatrix64F(int, int)
	 */
	public MatrixRectangular(int numRows, int numCols) {
		mat = new DenseMatrix64F(numRows, numCols);
	}

	/**
	 * Creats a new SimpleMatrix which is identical to the original.
	 * 
	 * @param orig The matrix which is to be copied. Not modified.
	 */
	public MatrixRectangular(MatrixRectangular orig) {
		this.mat = orig.mat.copy();
	}

	/**
	 * Creates a new SimpleMatrix which is a copy of the DenseMatrix64F.
	 * 
	 * @param orig The original matrix whose value is copied. Not modified.
	 */
	public MatrixRectangular(DenseMatrix64F orig) {
		this.mat = orig.copy();
	}

	/**
	 * Creates a new SimpleMatrix which is a copy of the Matrix64F.
	 * 
	 * @param orig The original matrix whose value is copied. Not modified.
	 */
	public MatrixRectangular(Matrix64F orig) {
		this.mat = new DenseMatrix64F(orig.numRows, orig.numCols);
		GenericMatrixOps.copy(orig, mat);
	}

	/**
	 * Instantiates a new matrix rectangular.
	 */
	protected MatrixRectangular() {
	}

	/**
	 * <p>
	 * Adds 'value' to the specified element in the matrix.<br>
	 * <br>
	 * a<sub>ij</sub> = a<sub>ij</sub> + value<br>
	 * </p>
	 * 
	 * @param row The row of the element.
	 * @param col The column of the element.
	 * @param value The value that is added to the element
	 */

	public void add(int row, int col, double value) {
		mat.set(row, col, mat.get(row, col) + value);
	}

	/**
	 * <p>
	 * Times 'value' to the specified element in the matrix.<br>
	 * <br>
	 * a<sub>ij</sub> = a<sub>ij</sub> x value<br>
	 * </p>
	 * 
	 * @param row The row of the element.
	 * @param col The column of the element.
	 * @param value The value that is timed to the element
	 */
	public void times(int row, int col, double value) {
		mat.set(row, col, mat.get(row, col) * value);
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.libMatrix.MatrixBase#debug()
	 */
	@Override
	public void debug() {
		logger.info("----------------------");
		logger.info(getClass().getSimpleName() + ": dimension row/col: " + numRows() + " / " + numCols());
		String ch = "";
		for (int i = 0; i < numRows(); i++) {
			ch = "";
			for (int j = 0; j < numCols(); j++) {
				ch += String.format("%e", mat.get(i, j)) + " ";
			}
			logger.info("line " + i + ": " + ch);
		}
		logger.info("----------------------");
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.libMatrix.MatrixBase#createMatrix(int, int)
	 */
	@Override
	protected MatrixRectangular createMatrix(int numRows, int numCols) {
		return new MatrixRectangular(numRows, numCols);
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.libMatrix.MatrixBase#copy()
	 */
	@Override
	public MatrixRectangular copy() {
		return super.copy();
	}

	@Override
	public MatrixRectangular clone() throws CloneNotSupportedException {
		logger.info("clone MatrixRectangular");
		return copy();
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

}
