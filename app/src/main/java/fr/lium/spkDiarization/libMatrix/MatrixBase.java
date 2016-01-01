package fr.lium.spkDiarization.libMatrix;

import fr.lium.spkDiarization.lib.SpkDiarizationLogger;
import java.io.Serializable;
import java.util.logging.Logger;

import org.ejml.alg.dense.mult.VectorVectorMult;
import org.ejml.data.DenseMatrix64F;
import org.ejml.data.MatrixIterator;
import org.ejml.factory.SingularMatrixException;
import org.ejml.ops.CommonOps;
import org.ejml.ops.MatrixFeatures;
import org.ejml.ops.NormOps;
import org.ejml.ops.SpecializedOps;
import org.ejml.simple.SimpleEVD;
import org.ejml.simple.SimpleSVD;

/**
 * Parent of MatrixXXX implements all the standard matrix operations and uses generics to allow the returned matrix type to be changed. This class should be extended instead of SimpleMatrix.
 * 
 * @param <T> the generic type
 * @author Peter Abeles
 */
@SuppressWarnings("rawtypes")
public abstract class MatrixBase<T extends MatrixBase> implements Serializable, Cloneable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(MatrixSymmetric.class.getName());

	/**
	 * A simplified way to reference the last row or column in the matrix for some functions.
	 */
	public static final int END = Integer.MAX_VALUE;
	/**
	 * Internal matrix which this is a wrapper around.
	 */
	protected DenseMatrix64F mat;

	/**
	 * Instantiates a new matrix base.
	 * 
	 * @param numRows the num rows
	 * @param numCols the num cols
	 */
	public MatrixBase(int numRows, int numCols) {
		mat = new DenseMatrix64F(numRows, numCols);
	}

	/**
	 * Instantiates a new matrix base.
	 */
	protected MatrixBase() {
	}

	/**
	 * Used internally for creating new instances of SimpleMatrix. If SimpleMatrix is extended by another class this function should be overridden so that the returned matrices are of the correct type.
	 * 
	 * @param numRows number of rows in the new matrix.
	 * @param numCols number of columns in the new matrix.
	 * @return A new matrix.
	 */
	protected abstract T createMatrix(int numRows, int numCols);

	/**
	 * <p>
	 * Returns a reference to the matrix that it uses internally. This is useful when an operation is needed that is not provided by this class.
	 * </p>
	 * 
	 * @return Reference to the internal DenseMatrix64F.
	 */
	public DenseMatrix64F getMatrix() {
		return mat;
	}

	/**
	 * <p>
	 * Returns the transpose of this matrix.<br>
	 * a<sup>T</sup>
	 * </p>
	 * 
	 * @return A matrix that is n by m.
	 * @see org.ejml.ops.CommonOps#transpose(DenseMatrix64F,DenseMatrix64F)
	 */
	public T transpose() {
		T ret = createMatrix(mat.numCols, mat.numRows);

		CommonOps.transpose(mat, ret.getMatrix());

		return ret;
	}

	/**
	 * <p>
	 * Returns a matrix which is the result of matrix multiplication:<br>
	 * <br>
	 * c = a * b <br>
	 * <br>
	 * where c is the returned matrix, a is this matrix, and b is the passed in matrix.
	 * </p>
	 * 
	 * @param b A matrix that is n by bn. Not modified.
	 * @return The results of this operation.
	 * @see CommonOps#mult(org.ejml.data.RowD1Matrix64F , org.ejml.data.RowD1Matrix64F , org.ejml.data.RowD1Matrix64F)
	 */
	public T mult(T b) {
		T ret = createMatrix(mat.numRows, b.getMatrix().numCols);

		CommonOps.mult(mat, b.getMatrix(), ret.getMatrix());

		return ret;
	}

	/**
	 * <p>
	 * Computes the Kronecker product between this matrix and the provided B matrix:<br>
	 * <br>
	 * C = kron(A,B)
	 * </p>
	 * .
	 * 
	 * @param B The right matrix in the operation. Not modified.
	 * @return Kronecker product between this matrix and B.
	 * @see CommonOps#kron(DenseMatrix64F, DenseMatrix64F, DenseMatrix64F)
	 */
	public T kron(T B) {
		T ret = createMatrix(mat.numRows * B.numRows(), mat.numCols * B.numCols());
		CommonOps.kron(mat, B.getMatrix(), ret.getMatrix());

		return ret;
	}

	/**
	 * <p>
	 * Returns the result of matrix addition:<br>
	 * <br>
	 * c = a + b <br>
	 * <br>
	 * where c is the returned matrix, a is this matrix, and b is the passed in matrix.
	 * </p>
	 * 
	 * @param b m by n matrix. Not modified.
	 * @return The results of this operation.
	 * @see CommonOps#mult(org.ejml.data.RowD1Matrix64F , org.ejml.data.RowD1Matrix64F , org.ejml.data.RowD1Matrix64F)
	 */
	public T plus(T b) {
		T ret = copy();

		CommonOps.addEquals(ret.getMatrix(), b.getMatrix());

		return ret;
	}

	/**
	 * <p>
	 * Returns the result of matrix subtraction:<br>
	 * <br>
	 * c = a - b <br>
	 * <br>
	 * where c is the returned matrix, a is this matrix, and b is the passed in matrix.
	 * </p>
	 * 
	 * @param b m by n matrix. Not modified.
	 * @return The results of this operation.
	 * @see CommonOps#sub(org.ejml.data.D1Matrix64F , org.ejml.data.D1Matrix64F , org.ejml.data.D1Matrix64F)
	 */
	public T minus(T b) {
		T ret = copy();

		CommonOps.subEquals(ret.getMatrix(), b.getMatrix());

		return ret;
	}

	/**
	 * <p>
	 * Performs a matrix addition and scale operation.<br>
	 * <br>
	 * c = a + &beta;*b <br>
	 * <br>
	 * where c is the returned matrix, a is this matrix, and b is the passed in matrix.
	 * </p>
	 * 
	 * @param beta the beta
	 * @param b m by n matrix. Not modified.
	 * @return A matrix that contains the results.
	 * @see CommonOps#add(org.ejml.data.D1Matrix64F , double , org.ejml.data.D1Matrix64F , org.ejml.data.D1Matrix64F)
	 */
	public T plus(double beta, T b) {
		T ret = copy();

		CommonOps.addEquals(ret.getMatrix(), beta, b.getMatrix());

		return ret;
	}

	/**
	 * Computes the dot product (a.k.a. inner product) between this vector and vector 'v'.
	 * 
	 * @param v The second vector in the dot product. Not modified.
	 * @return dot product
	 */
	public double dot(T v) {
		if (!isVector()) {
			throw new IllegalArgumentException("'this' matrix is not a vector.");
		} else if (!v.isVector()) {
			throw new IllegalArgumentException("'v' matrix is not a vector.");
		}

		return VectorVectorMult.innerProd(mat, v.getMatrix());
	}

	/**
	 * Returns true if this matrix is a vector. A vector is defined as a matrix that has either one row or column.
	 * 
	 * @return Returns true for vectors and false otherwise.
	 */
	public boolean isVector() {
		return (mat.numRows == 1) || (mat.numCols == 1);
	}

	/**
	 * <p>
	 * Returns the result of scaling each element by 'val':<br>
	 * b<sub>i,j</sub> = val*a<sub>i,j</sub>
	 * </p>
	 * .
	 * 
	 * @param val The multiplication factor.
	 * @return The scaled matrix.
	 * @see CommonOps#scale(double, org.ejml.data.D1Matrix64F)
	 */
	public T scale(double val) {
		T ret = copy();

		CommonOps.scale(val, ret.getMatrix());

		return ret;
	}

	/**
	 * <p>
	 * Returns the result of dividing each element by 'val': b<sub>i,j</sub> = a<sub>i,j</sub>/val
	 * </p>
	 * .
	 * 
	 * @param val Divisor.
	 * @return Matrix with its elements divided by the specified value.
	 * @see CommonOps#divide(double, org.ejml.data.D1Matrix64F)
	 */
	public T divide(double val) {
		T ret = copy();

		CommonOps.divide(val, ret.getMatrix());

		return ret;
	}

	/**
	 * <p>
	 * Returns the inverse of this matrix.<br>
	 * <br>
	 * b = a<sup>-1<sup><br>
	 * </p>
	 * 
	 * <p>
	 * If the matrix could not be inverted then SingularMatrixException is thrown. Even if no exception is thrown the matrix could still be singular or nearly singular.
	 * </p>
	 * 
	 * @return The inverse of this matrix.
	 * @see CommonOps#invert(DenseMatrix64F, DenseMatrix64F)
	 */
	public T invert() {
		T ret = createMatrix(mat.numRows, mat.numCols);
		if (!CommonOps.invert(mat, ret.getMatrix())) {
			throw new SingularMatrixException();
		}
		return ret;
	}

	/**
	 * <p>
	 * Computes the Moore-Penrose pseudo-inverse
	 * </p>
	 * .
	 * 
	 * @return inverse computed using the pseudo inverse.
	 */
	public T pseudoInverse() {
		T ret = createMatrix(mat.numCols, mat.numRows);
		CommonOps.pinv(mat, ret.getMatrix());
		return ret;
	}

	/**
	 * <p>
	 * Solves for X in the following equation:<br>
	 * <br>
	 * x = a<sup>-1</sup>b<br>
	 * <br>
	 * where 'a' is this matrix and 'b' is an n by p matrix.
	 * </p>
	 * 
	 * <p>
	 * If the system could not be solved then SingularMatrixException is thrown. Even if no exception is thrown 'a' could still be singular or nearly singular.
	 * </p>
	 * 
	 * @param b n by p matrix. Not modified.
	 * @return The solution for 'x' that is n by p.
	 * @see CommonOps#solve(DenseMatrix64F, DenseMatrix64F, DenseMatrix64F)
	 */
	public T solve(T b) {
		T x = createMatrix(mat.numCols, b.getMatrix().numCols);

		if (!CommonOps.solve(mat, b.getMatrix(), x.getMatrix())) {
			throw new SingularMatrixException();
		}

		return x;
	}

	/**
	 * Sets the elements in this matrix to be equal to the elements in the passed in matrix. Both matrix must have the same dimension.
	 * 
	 * @param a The matrix whose value this matrix is being set to.
	 */
	public void setInternalMatrix(T a) {
		mat.set(a.getMatrix());
	}

	/**
	 * Sets all the elements in the matrix equal to zero.
	 * 
	 * @see CommonOps#fill(org.ejml.data.D1Matrix64F , double)
	 */
	public void zero() {
		mat.zero();
	}

	/**
	 * <p>
	 * Computes the Frobenius normal of the matrix:<br>
	 * <br>
	 * normF = Sqrt{ &sum;<sub>i=1:m</sub> &sum;<sub>j=1:n</sub> { a<sub>ij</sub><sup>2</sup>} }
	 * </p>
	 * .
	 * 
	 * @return The matrix's Frobenius normal.
	 * @see org.ejml.ops.NormOps#normF(org.ejml.data.D1Matrix64F)
	 */
	public double normF() {
		return NormOps.normF(mat);
	}

	/**
	 * <p>
	 * The condition p = 2 number of a matrix is used to measure the sensitivity of the linear system <b>Ax=b</b>. A value near one indicates that it is a well conditioned matrix.
	 * </p>
	 * 
	 * @return The condition number.
	 * @see NormOps#conditionP2(DenseMatrix64F)
	 */
	public double conditionP2() {
		return NormOps.conditionP2(mat);
	}

	/**
	 * Computes the determinant of the matrix.
	 * 
	 * @return The determinant.
	 * @see CommonOps#det(DenseMatrix64F)
	 */
	public double determinant() {
		return CommonOps.det(mat);
	}

	/**
	 * <p>
	 * Computes the trace of the matrix.
	 * </p>
	 * 
	 * @return The trace of the matrix.
	 * @see CommonOps#trace(org.ejml.data.RowD1Matrix64F)
	 */
	public double trace() {
		return CommonOps.trace(mat);
	}

	/**
	 * <p>
	 * Reshapes the matrix to the specified number of rows and columns. If the total number of elements is <= number of elements it had before the data is saved. Otherwise a new internal array is declared and the old data lost.
	 * </p>
	 * 
	 * <p>
	 * This is equivalent to calling A.getMatrix().reshape(numRows,numCols,false).
	 * </p>
	 * 
	 * @param numRows The new number of rows in the matrix.
	 * @param numCols The new number of columns in the matrix.
	 * @see org.ejml.data.Matrix64F#reshape(int,int,boolean)
	 */
	public void reshape(int numRows, int numCols) {
		mat.reshape(numRows, numCols, false);
	}

	/**
	 * Assigns an element a value based on its index in the internal array..
	 * 
	 * @param index The matrix element that is being assigned a value.
	 * @param value The element's new value.
	 */
	public void set(int index, double value) {
		mat.set(index, value);
	}

	/**
	 * <p>
	 * Assigns consecutive elements inside a row to the provided array.<br>
	 * <br>
	 * A(row,offset:(offset + values.length)) = values
	 * </p>
	 * 
	 * @param row The row that the array is to be written to.
	 * @param offset The initial column that the array is written to.
	 * @param values Values which are to be written to the row in a matrix.
	 */
	public void setRow(int row, int offset, double... values) {
		for (int i = 0; i < values.length; i++) {
			mat.set(row, offset + i, values[i]);
		}
	}

	/**
	 * <p>
	 * Assigns consecutive elements inside a column to the provided array.<br>
	 * <br>
	 * A(offset:(offset + values.length),column) = values
	 * </p>
	 * 
	 * @param column The column that the array is to be written to.
	 * @param offset The initial column that the array is written to.
	 * @param values Values which are to be written to the row in a matrix.
	 */
	public void setColumn(int column, int offset, double... values) {
		for (int i = 0; i < values.length; i++) {
			mat.set(offset + i, column, values[i]);
		}
	}

	/**
	 * Returns the value of the matrix at the specified index of the 1D row major array.
	 * 
	 * @param index The element's index whose value is to be returned
	 * @return The value of the specified element.
	 * @see org.ejml.data.DenseMatrix64F#get(int)
	 */
	public double get(int index) {
		return mat.data[index];
	}

	/**
	 * Returns the index in the matrix's array.
	 * 
	 * @param row The row number.
	 * @param col The column number.
	 * @return The index of the specified element.
	 * @see org.ejml.data.DenseMatrix64F#getIndex(int, int)
	 */
	public int getIndex(int row, int col) {
		return (row * mat.numCols) + col;
	}

	/**
	 * Creates a new iterator for traversing through a submatrix inside this matrix. It can be traversed by row or by column. Range of elements is inclusive, e.g. minRow = 0 and maxRow = 1 will include rows 0 and 1. The iteration starts at
	 * (minRow,minCol) and ends at (maxRow,maxCol)
	 * 
	 * @param rowMajor true means it will traverse through the submatrix by row first, false by columns.
	 * @param minRow first row it will start at.
	 * @param minCol first column it will start at.
	 * @param maxRow last row it will stop at.
	 * @param maxCol last column it will stop at.
	 * @return A new MatrixIterator
	 */
	public MatrixIterator iterator(boolean rowMajor, int minRow, int minCol, int maxRow, int maxCol) {
		return new MatrixIterator(mat, rowMajor, minRow, minCol, maxRow, maxCol);
	}

	/**
	 * Creates and returns a matrix which is idential to this one.
	 * 
	 * @return A new identical matrix.
	 */
	public T copy() {
		// logger.info("copy matrix base");
		if (mat == null) {
			return null;
		}
		T ret = createMatrix(mat.numRows, mat.numCols);
		ret.getMatrix().set(this.getMatrix());
// ret.determinant = determinant;
// ret.logDeterminant = logDeterminant;
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public T clone() throws CloneNotSupportedException {
		if (SpkDiarizationLogger.DEBUG) logger.info("clone matrix base");
		return copy();
	}

	/**
	 * Returns the number of rows in this matrix.
	 * 
	 * @return number of rows.
	 */
	public int numRows() {
		return mat.numRows;
	}

	/**
	 * Returns the number of columns in this matrix.
	 * 
	 * @return number of columns.
	 */
	public int numCols() {
		return mat.numCols;
	}

	/**
	 * Returns the number of elements in this matrix, which is equal to the number of rows times the number of columns.
	 * 
	 * @return The number of elements in the matrix.
	 */
	public int getNumElements() {
		return mat.getNumElements();
	}

	/**
	 * <p>
	 * Creates a new SimpleMatrix which is a submatrix of this matrix.
	 * </p>
	 * <p>
	 * s<sub>i-y0 , j-x0</sub> = o<sub>ij</sub> for all y0 &le; i < y1 and x0 &le; j < x1<br>
	 * <br>
	 * where 's<sub>ij</sub>' is an element in the submatrix and 'o<sub>ij</sub>' is an element in the original matrix.
	 * </p>
	 * 
	 * <p>
	 * If any of the inputs are set to T.END then it will be set to the last row or column in the matrix.
	 * </p>
	 * 
	 * @param y0 Start row.
	 * @param y1 Stop row.
	 * @param x0 Start column.
	 * @param x1 Stop column.
	 * @return The submatrix.
	 */
	public T extractMatrix(int y0, int y1, int x0, int x1) {
		if (y0 == END) {
			y0 = mat.numRows;
		}
		if (y1 == END) {
			y1 = mat.numRows;
		}
		if (x0 == END) {
			x0 = mat.numCols;
		}
		if (x1 == END) {
			x1 = mat.numCols;
		}

		T ret = createMatrix(y1 - y0, x1 - x0);

		CommonOps.extract(mat, y0, y1, x0, x1, ret.getMatrix(), 0, 0);

		return ret;
	}

	/**
	 * <p>
	 * Extracts a row or column from this matrix. The returned vector will either be a row or column vector depending on the input type.
	 * </p>
	 * 
	 * @param extractRow If true a row will be extracted.
	 * @param element The row or column the vector is contained in.
	 * @return Extracted vector.
	 */
	public T extractVector(boolean extractRow, int element) {
		int length = extractRow ? mat.numCols : mat.numRows;

		T ret = extractRow ? createMatrix(1, length) : createMatrix(length, 1);

		if (extractRow) {
			SpecializedOps.subvector(mat, element, 0, length, true, 0, ret.getMatrix());
		} else {
			SpecializedOps.subvector(mat, 0, element, length, false, 0, ret.getMatrix());
		}

		return ret;
	}

	/**
	 * <p>
	 * Extracts the diagonal from this matrix and returns them inside a column vector.
	 * </p>
	 * 
	 * @return Diagonal elements inside a column vector.
	 * @see org.ejml.ops.CommonOps#extractDiag(DenseMatrix64F, DenseMatrix64F)
	 */
	public T extractDiag() {
		int N = Math.min(mat.numCols, mat.numRows);

		T diag = createMatrix(N, 1);

		CommonOps.extractDiag(mat, diag.getMatrix());

		return diag;
	}

	/**
	 * Checks to see if matrix 'a' is the same as this matrix within the specified tolerance.
	 * 
	 * @param a The matrix it is being compared against.
	 * @param tol How similar they must be to be equals.
	 * @return If they are equal within tolerance of each other.
	 */
	public boolean isIdentical(T a, double tol) {
		return MatrixFeatures.isIdentical(mat, a.getMatrix(), tol);
	}

	/**
	 * Checks to see if any of the elements in this matrix are either NaN or infinite.
	 * 
	 * @return True of an element is NaN or infinite. False otherwise.
	 */
	public boolean hasUncountable() {
		return MatrixFeatures.hasUncountable(mat);
	}

	/**
	 * Computes a full Singular Value Decomposition (SVD) of this matrix with the eigenvalues ordered from largest to smallest.
	 * 
	 * @return SVD
	 */
	public SimpleSVD svd() {
		return new SimpleSVD(mat, false);
	}

	/**
	 * Computes the SVD in either compact format or full format.
	 * 
	 * @param compact the compact
	 * @return SVD of this matrix.
	 */
	public SimpleSVD svd(boolean compact) {
		return new SimpleSVD(mat, compact);
	}

	/**
	 * Returns the Eigen Value Decomposition (EVD) of this matrix.
	 * 
	 * @return the simple evd
	 */
	public SimpleEVD eig() {
		return new SimpleEVD(mat);
	}

	/**
	 * Copy matrix B into this matrix at location (insertRow, insertCol).
	 * 
	 * @param insertRow First row the matrix is to be inserted into.
	 * @param insertCol First column the matrix is to be inserted into.
	 * @param B The matrix that is being inserted.
	 */
	public void insertIntoThis(int insertRow, int insertCol, T B) {
		CommonOps.insert(B.getMatrix(), mat, insertRow, insertCol);
	}

	/**
	 * <p>
	 * Creates a new matrix that is a combination of this matrix and matrix B. B is written into A at the specified location if needed the size of A is increased by growing it. A is grown by padding the new area with zeros.
	 * </p>
	 * 
	 * <p>
	 * While useful when adding data to a matrix which will be solved for it is also much less efficient than predeclaring a matrix and inserting data into it.
	 * </p>
	 * 
	 * <p>
	 * If insertRow or insertCol is set to SimpleMatrix.END then it will be combined at the last row or column respectively.
	 * <p>
	 * 
	 * @param insertRow Row where matrix B is written in to.
	 * @param insertCol Column where matrix B is written in to.
	 * @param B The matrix that is written into A.
	 * @return A new combined matrix.
	 */
	@SuppressWarnings("unchecked")
	public T combine(int insertRow, int insertCol, T B) {

		if (insertRow == END) {
			insertRow = mat.numRows;
		}

		if (insertCol == END) {
			insertCol = mat.numCols;
		}

		int maxRow = insertRow + B.numRows();
		int maxCol = insertCol + B.numCols();

		T ret;

		if ((maxRow > mat.numRows) || (maxCol > mat.numCols)) {
			int M = Math.max(maxRow, mat.numRows);
			int N = Math.max(maxCol, mat.numCols);

			ret = createMatrix(M, N);
			ret.insertIntoThis(0, 0, this);
		} else {
			ret = copy();
		}

		ret.insertIntoThis(insertRow, insertCol, B);

		return ret;
	}

	/**
	 * Returns the maximum absolute value of all the elements in this matrix. This is equivalent the the infinite p-norm of the matrix.
	 * 
	 * @return Largest absolute value of any element.
	 */
	public double elementMaxAbs() {
		return CommonOps.elementMaxAbs(mat);
	}

	/**
	 * Computes the sum of all the elements in the matrix.
	 * 
	 * @return Sum of all the elements.
	 */
	public double elementSum() {
		return CommonOps.elementSum(mat);
	}

	/**
	 * <p>
	 * Returns a matrix which is the result of an element by element multiplication of 'this' and 'b': c<sub>i,j</sub> = a<sub>i,j</sub>*b<sub>i,j</sub>
	 * </p>
	 * .
	 * 
	 * @param b A simple matrix.
	 * @return The element by element multiplication of 'this' and 'b'.
	 */
	public T elementMult(T b) {
		T c = createMatrix(mat.numRows, mat.numCols);

		CommonOps.elementMult(mat, b.getMatrix(), c.getMatrix());

		return c;
	}

	/**
	 * <p>
	 * Returns a new matrix whose elements are the negative of 'this' matrix's elements.<br>
	 * <br>
	 * b<sub>ij</sub> = -a<sub>ij</sub>
	 * </p>
	 * 
	 * @return A matrix that is the negative of the original.
	 */
	public T negative() {
		T A = copy();
		CommonOps.changeSign(A.getMatrix());
		return A;
	}

	/**
	 * Returns true of the specified matrix element is valid element inside this matrix.
	 * 
	 * @param row Row index.
	 * @param col Column index.
	 * @return true if it is a valid element in the matrix.
	 */
	public boolean isInBounds(int row, int col) {
		return (row >= 0) && (col >= 0) && (row < mat.numRows) && (col < mat.numCols);
	}

	/**
	 * Fill.
	 * 
	 * @param v the v
	 */
	public void fill(double v) {
		CommonOps.fill(mat, v);
	}

	/**
	 * Debug.
	 */
	public void debug() {
		logger.info("numRows: " + numRows() + " numCols: " + numCols() + " type:" + getClass().getSimpleName());
		for (int i = 0; i < numRows(); i++) {
			String line = "";
			for (int j = 0; j < numCols(); j++) {
				line += String.format("%8f", mat.get(i, j)) + " ";
			}
			logger.info("idx: " + i + " values: " + line);
		}
	}

}
