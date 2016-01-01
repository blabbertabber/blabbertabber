package fr.lium.spkDiarization.libMatrix;

import fr.lium.spkDiarization.lib.SpkDiarizationLogger;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.ejml.alg.dense.mult.MatrixDimensionException;

/**
 * The Class MatrixIO.
 */
public class MatrixIO {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(MatrixIO.class.getName());

	/**
	 * Creates the gaussian random.
	 * 
	 * @param rows the rows
	 * @param cols the cols
	 * @param mean the mean
	 * @param var the var
	 * @return the matrix rectangular
	 */
	static public MatrixRectangular createGaussianRandom(int rows, int cols, double mean, double var) {
		Random rand = new Random(rows * cols);
		MatrixRectangular matrix = new MatrixRectangular(rows, cols);

		for (int i = 0; i < matrix.getNumElements(); i++) {
			matrix.set(i, (rand.nextGaussian() - mean) / var);
		}
		return matrix;
	}

	/**
	 * Read data matrix.
	 * 
	 * @param matrix the matrix
	 * @param bufferedReader the buffered reader
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@SuppressWarnings("rawtypes")
	protected static void readDataMatrix(MatrixBase matrix, BufferedReader bufferedReader) throws IOException {
		int k = 0;
		for (int i = 0; i < matrix.numRows(); i++) {
			String line = bufferedReader.readLine();
			String[] value = line.split(" ");
			for (int j = 0; j < matrix.numCols(); j++) {
				matrix.set(k, Double.parseDouble(value[j]));
				k++;
			}
		}
	}

	/**
	 * Open buffered reader.
	 * 
	 * @param filename the filename
	 * @param gzip the gzip
	 * @return the buffered reader
	 * @throws FileNotFoundException the file not found exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected static BufferedReader openBufferedReader(String filename, boolean gzip) throws FileNotFoundException, IOException {
		BufferedReader bufferedReader;
		if (gzip == true) {
			bufferedReader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(filename))));
		} else {
			bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
		}
		return bufferedReader;
	}

	/**
	 * Read matrix square.
	 * 
	 * @param filename the filename
	 * @param gzip the gzip
	 * @return the matrix square
	 * @throws FileNotFoundException the file not found exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static MatrixSquare readMatrixSquare(String filename, boolean gzip) throws FileNotFoundException, IOException {
		BufferedReader bufferedReader = openBufferedReader(filename, gzip);
		String[] sizes = bufferedReader.readLine().split(" ");
		int row = Integer.parseInt(sizes[1]);
		int column = Integer.parseInt(sizes[0]);
		if (row != column) {
			throw new MatrixDimensionException("need to be a square numCols == numRows");
		}
		MatrixSquare matrix = new MatrixSquare(row);
		logger.info("read " + filename + ": row : " + row + " col:" + column);
		readDataMatrix(matrix, bufferedReader);
		bufferedReader.close();
		return matrix;
	}

	/**
	 * Read matrix symmetric.
	 * 
	 * @param filename the filename
	 * @param gzip the gzip
	 * @return the matrix symmetric
	 * @throws FileNotFoundException the file not found exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static MatrixSymmetric readMatrixSymmetric(String filename, boolean gzip) throws FileNotFoundException, IOException {
		BufferedReader bufferedReader = openBufferedReader(filename, gzip);
		String[] sizes = bufferedReader.readLine().split(" ");
		int row = Integer.parseInt(sizes[1]);
		int column = Integer.parseInt(sizes[0]);
		if (row != column) {
			throw new MatrixDimensionException("need to be a symmetric numCols == numRows");
		}
		MatrixSymmetric matrix = new MatrixSymmetric(row);
		logger.info("read " + filename + ": row : " + row + " col:" + column);
		readDataMatrix(matrix, bufferedReader);
		bufferedReader.close();
		return matrix;
	}

	/**
	 * Read matrix vector.
	 * 
	 * @param filename the filename
	 * @param gzip the gzip
	 * @return the matrix row vector
	 * @throws FileNotFoundException the file not found exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static MatrixRowVector readMatrixVector(String filename, boolean gzip) throws FileNotFoundException, IOException {
		BufferedReader bufferedReader = openBufferedReader(filename, gzip);
		String[] sizes = bufferedReader.readLine().split(" ");
		int row = Integer.parseInt(sizes[1]);
		int column = Integer.parseInt(sizes[0]);
		if (row != 1) {
			throw new MatrixDimensionException("need to have 1 row");
		}
		MatrixRowVector matrix = new MatrixRowVector(column);
		logger.info("read " + filename + ": row : " + row + " col:" + column);
		readDataMatrix(matrix, bufferedReader);
		bufferedReader.close();
		return matrix;
	}

	/**
	 * Read rect matrix.
	 * 
	 * @param filename the filename
	 * @param gzip the gzip
	 * @return the matrix rectangular
	 * @throws FileNotFoundException the file not found exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static MatrixRectangular readRectMatrix(String filename, boolean gzip) throws FileNotFoundException, IOException {
		BufferedReader bufferedReader = openBufferedReader(filename, gzip);
		String[] sizes = bufferedReader.readLine().split(" ");
		int row = Integer.parseInt(sizes[1]);
		int column = Integer.parseInt(sizes[0]);
		MatrixRectangular matrix = new MatrixRectangular(row, column);
		logger.info("read " + filename + ": row : " + row + " col:" + column);
		readDataMatrix(matrix, bufferedReader);
		bufferedReader.close();
		return matrix;
	}

	/**
	 * Open buffered output stream.
	 * 
	 * @param filename the filename
	 * @param gzip the gzip
	 * @return the buffered output stream
	 * @throws FileNotFoundException the file not found exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected static BufferedOutputStream openBufferedOutputStream(String filename, boolean gzip) throws FileNotFoundException, IOException {
		BufferedOutputStream bufferedOutputStream;
		if (gzip == true) {
			bufferedOutputStream = new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(filename)));
		} else {
			bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(filename));
		}
		return bufferedOutputStream;
	}

	/**
	 * Write matrix.
	 * 
	 * @param matrix the matrix
	 * @param filename the filename
	 * @param gzip the gzip
	 * @throws FileNotFoundException the file not found exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void writeMatrix(MatrixRectangular matrix, String filename, boolean gzip) throws FileNotFoundException, IOException {
		BufferedOutputStream bufferedOutputStream = openBufferedOutputStream(filename, gzip);
		String line = String.format("%d %d %s\n", matrix.numCols(), matrix.numRows(), matrix.getClass().getSimpleName());
		if (SpkDiarizationLogger.DEBUG) logger.info("write matrix : " + line);
		bufferedOutputStream.write(line.getBytes());
		writeMatrixData(matrix, bufferedOutputStream);
		if (SpkDiarizationLogger.DEBUG) logger.info("write matrix done ");
		bufferedOutputStream.close();
	}

	/**
	 * Write matrix.
	 * 
	 * @param matrix the matrix
	 * @param filename the filename
	 * @param gzip the gzip
	 * @throws FileNotFoundException the file not found exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void writeMatrix(MatrixSquare matrix, String filename, boolean gzip) throws FileNotFoundException, IOException {
		BufferedOutputStream bufferedOutputStream = openBufferedOutputStream(filename, gzip);

		String line = String.format("%d %d %s\n", matrix.numCols(), matrix.numRows(), matrix.getClass().getSimpleName());
		bufferedOutputStream.write(line.getBytes());
		writeMatrixData(matrix, bufferedOutputStream);
		bufferedOutputStream.close();
	}

	/**
	 * Write matrix.
	 * 
	 * @param matrix the matrix
	 * @param filename the filename
	 * @param gzip the gzip
	 * @throws FileNotFoundException the file not found exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void writeMatrix(MatrixSymmetric matrix, String filename, boolean gzip) throws FileNotFoundException, IOException {
		BufferedOutputStream bufferedOutputStream = openBufferedOutputStream(filename, gzip);

		String line = String.format("%d %d %s\n", matrix.numCols(), matrix.numRows(), matrix.getClass().getSimpleName());
		bufferedOutputStream.write(line.getBytes());
		writeMatrixData(matrix, bufferedOutputStream);
		bufferedOutputStream.close();
	}

	/**
	 * Write matrix.
	 * 
	 * @param matrix the matrix
	 * @param filename the filename
	 * @param gzip the gzip
	 * @throws FileNotFoundException the file not found exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void writeMatrix(MatrixRowVector matrix, String filename, boolean gzip) throws FileNotFoundException, IOException {
		BufferedOutputStream bufferedOutputStream = openBufferedOutputStream(filename, gzip);

		String line = String.format("%d %d %s\n", matrix.numCols(), matrix.numRows(), matrix.getClass().getSimpleName());
		bufferedOutputStream.write(line.getBytes());
		writeMatrixData(matrix, bufferedOutputStream);
		bufferedOutputStream.close();
	}

	/**
	 * Write matrix data.
	 * 
	 * @param matrix the matrix
	 * @param bufferedOutputStream the buffered output stream
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@SuppressWarnings("rawtypes")
	public static void writeMatrixData(MatrixBase matrix, BufferedOutputStream bufferedOutputStream) throws IOException {
// String line = "";
		for (int i = 0, j = 0; i < matrix.getNumElements(); i++, j++) {
			if (j == matrix.numCols()) {
// line += "\n";
				bufferedOutputStream.write("\n".getBytes());
// line = "";
				j = 0;
			}
			bufferedOutputStream.write(String.format("%8.6f ", matrix.get(i)).getBytes());
		}
	}
}
