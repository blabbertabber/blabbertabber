package fr.lium.spkDiarization.libModel.ivector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.SpkDiarizationLogger;
import fr.lium.spkDiarization.libMatrix.MatrixRowVector;
import fr.lium.spkDiarization.libMatrix.MatrixSquare;
import fr.lium.spkDiarization.libMatrix.MatrixSymmetric;
import fr.lium.spkDiarization.libModel.gaussian.FullGaussian;

/**
 * The Class EigenFactorRadialList.
 */
public class EigenFactorRadialList implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(EigenFactorRadialList.class.getName());

	/** The global mean covariance. */
	FullGaussian globalMeanCovariance;

	/** The list. */
	ArrayList<EigenFactorRadialData> list;

	/**
	 * Instantiates a new eigen factor radial list.
	 * 
	 * @param nb the nb
	 */
	public EigenFactorRadialList(int nb) {
		super();
		list = new ArrayList<EigenFactorRadialData>(nb);
	}

	/**
	 * Instantiates a new eigen factor radial list.
	 */
	public EigenFactorRadialList() {
		super();
		list = new ArrayList<EigenFactorRadialData>();
	}

	/**
	 * Gets the t.
	 * 
	 * @param n the n
	 * @return the t
	 */
	public MatrixSquare getT(int n) {
		return list.get(n).getT();
	}

	/**
	 * Gets the mean.
	 * 
	 * @param n the n
	 * @return the mean
	 */
	public MatrixRowVector getMean(int n) {
		return list.get(n).getMean();
	}

	/**
	 * Gets the covariance.
	 * 
	 * @param n the n
	 * @return the covariance
	 */
	public MatrixSymmetric getCovariance(int n) {
		return list.get(n).getCovariance();
	}

	/**
	 * Adds the.
	 * 
	 * @param meanAndCov the mean and cov
	 * @param w the w
	 */
	public void add(FullGaussian meanAndCov, MatrixSquare w) {
		list.add(new EigenFactorRadialData(meanAndCov, w));
	}

	/**
	 * Adds the.
	 * 
	 * @param d the d
	 */
	public void add(EigenFactorRadialData d) {
		list.add(d);
	}

	/**
	 * Size.
	 * 
	 * @return the nbIteration
	 */
	public int size() {
		return list.size();
	}

	/**
	 * Gets the list.
	 * 
	 * @return the list
	 */
	protected ArrayList<EigenFactorRadialData> getList() {
		return list;
	}

	/**
	 * Sets the list.
	 * 
	 * @param list the list to set
	 */
	protected void setList(ArrayList<EigenFactorRadialData> list) {
		this.list = list;
	}

	/**
	 * Gets the global invert covariance.
	 * 
	 * @return the global invert covariance
	 * @throws DiarizationException the diarization exception
	 */
	public MatrixSymmetric getGlobalInvertCovariance() throws DiarizationException {
		return globalMeanCovariance.getCovariance().invert();
	}

	/**
	 * Creates the data mean cov.
	 * 
	 * @param meanAndCov the mean and cov
	 * @param t the t
	 * @return the eigen factor radial data
	 */
	static public EigenFactorRadialData createDataMeanCov(FullGaussian meanAndCov, MatrixSquare t) {
		EigenFactorRadialData result = new EigenFactorRadialData(meanAndCov, t);
		// list.add(result);
		return result;
	}

	/**
	 * Write binary.
	 * 
	 * @param normalize the normalize
	 * @param fileName the file name
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	static public void writeBinary(EigenFactorRadialList normalize, String fileName) throws IOException {
		if (SpkDiarizationLogger.DEBUG) logger.info("write EigenFactorRadialList, nb iteration: " + normalize.size());
		ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(fileName));
		stream.writeObject(normalize);
		stream.close();
	}

	/**
	 * Read binary.
	 * 
	 * @param fileName the file name
	 * @return the eigen factor radial list
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException the class not found exception
	 */
	static public EigenFactorRadialList readBinary(String fileName) throws IOException, ClassNotFoundException {
		ObjectInputStream stream = new ObjectInputStream(new FileInputStream(fileName));

		EigenFactorRadialList normalize = (EigenFactorRadialList) stream.readObject();
		stream.close();
		return normalize;
	}

	/**
	 * Write xml.
	 * 
	 * @param normalize the normalize
	 * @param fileName the file name
	 * @throws FileNotFoundException the file not found exception
	 */
	static public void writeXML(EigenFactorRadialList normalize, String fileName) throws FileNotFoundException {
		XStream xstream = new XStream(new DomDriver());
		xstream.toXML(normalize, new FileOutputStream(fileName));
	}

	/**
	 * Read xml.
	 * 
	 * @param fileName the file name
	 * @return the eigen factor radial list
	 */
	static public EigenFactorRadialList readXML(String fileName) {
		EigenFactorRadialList normalize = new EigenFactorRadialList();
		XStream xstream = new XStream(new DomDriver());
		xstream.fromXML(new File(fileName), normalize);
		return normalize;
	}

	/**
	 * Gets the global mean covariance.
	 * 
	 * @return the globalMeanCovariance
	 */
	public FullGaussian getGlobalMeanCovariance() {
		return globalMeanCovariance;
	}

	/**
	 * Sets the global mean covariance.
	 * 
	 * @param globalMeanCovariance the globalMeanCovariance to set
	 */
	public void setGlobalMeanCovariance(FullGaussian globalMeanCovariance) {
		this.globalMeanCovariance = globalMeanCovariance;
	}

	/**
	 * Debug.
	 * 
	 * @throws DiarizationException the diarization exception
	 */
	public void debug() throws DiarizationException {
		logger.info(" EigenFactorRadialList, number of iteration: " + size());
		int i = 0;
		for (EigenFactorRadialData data : list) {
			logger.info("normalization number:" + i);
			data.debug();
			i++;
		}
	}

}
