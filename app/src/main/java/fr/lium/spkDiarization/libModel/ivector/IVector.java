package fr.lium.spkDiarization.libModel.ivector;

import java.io.Serializable;
import java.util.logging.Logger;

import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libMatrix.MatrixRowVector;
import fr.lium.spkDiarization.libMatrix.MatrixSquare;

/**
 * The Class IVector.
 */
public class IVector implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(IVector.class.getName());

	/** The data. */
	protected MatrixRowVector data;

	/** Gender of the model. */
	private String gender;

	/** name of the model. */
	private String name;

	/** The name of the session. */
	private String session;

	/** The speaker id. */
	private String speakerID;

	/** Size of a feature. */
	private int size;

	/**
	 * Instantiates a new i vector.
	 * 
	 * @param dimension the dimension
	 * @param name the name
	 * @param gender the gender
	 */
	public IVector(int dimension, String name, String gender) {
		super();
		this.gender = gender;
		this.name = name;
		this.size = dimension;
		data = new MatrixRowVector(dimension);
		data.fill(0.0);
	}

	/**
	 * Instantiates a new i vector.
	 * 
	 * @param dimension the dimension
	 */
	public IVector(int dimension) {
		super();
		gender = Cluster.genderStrings[0];
		this.name = "empty";
		this.size = dimension;
		data = new MatrixRowVector(dimension);
		data.fill(0.0);
	}

	/**
	 * Instantiates a new i vector.
	 * 
	 * @param data the data
	 * @param name the name
	 * @param gender the gender
	 */
	public IVector(MatrixRowVector data, String name, String gender) {
		super();
		this.data = data;
		this.gender = gender;
		this.name = name;
		size = data.getSize();
	}

	/**
	 * Gets the dimension.
	 * 
	 * @return the dimension
	 */
	public int getDimension() {
		return size;
	}

	/**
	 * Gets the gender.
	 * 
	 * @return the gender
	 */
	public String getGender() {
		return gender;
	}

	/**
	 * Sets the gender.
	 * 
	 * @param gender the gender to set
	 */
	public void setGender(String gender) {
		this.gender = gender;
	}

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 * 
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the session.
	 * 
	 * @return the session
	 */
	public String getSession() {
		return session;
	}

	/**
	 * Sets the session.
	 * 
	 * @param session the session to set
	 */
	public void setSession(String session) {
		this.session = session;
	}

	/**
	 * Gets the speaker id.
	 * 
	 * @return the speakerID
	 */
	public String getSpeakerID() {
		return speakerID;
	}

	/**
	 * Sets the speaker id.
	 * 
	 * @param speakerID the speakerID to set
	 */
	public void setSpeakerID(String speakerID) {
		this.speakerID = speakerID;
	}

	/**
	 * Gets the.
	 * 
	 * @param i the index in iVector data
	 * @return the double
	 */
	public double get(int i) {
		return data.get(i);
	}

	/**
	 * Sets the value of the iVector indexed by i.
	 * 
	 * @param i the i
	 * @param value the value
	 */
	public void set(int i, double value) {
		data.set(i, value);
	}

	/**
	 * WCCN normalisation of the i-vector: At * X Normalisation by the transpose of a cholesky decomposition matrix of the covariance matrix.
	 * 
	 * @param At the at
	 */
	public void NormalizeWithCholeskyDecompositionMatrix(MatrixSquare At) {
		for (int i = 0; i < getDimension(); i++) {
			double sum = 0.0;
			for (int j = i; j < getDimension(); j++) {
				sum += get(j) * At.get(i, j);
			}
			set(i, sum);
		}

	}

	/**
	 * Gets the data.
	 * 
	 * @return the data
	 */
	public MatrixRowVector getData() {
		return data;
	}

	/**
	 * Debug.
	 */
	public void debug() {
		logger.info("name: " + getName());
		logger.info("gender: " + getGender() + " session:" + session + " id:" + speakerID);
		logger.info("dimmension: " + getDimension());
		String ch = "";
		for (int i = 0; i < getDimension(); i++) {
			ch += String.format("%10.8f", get(i)) + " ";
		}
		logger.info(ch);
	}
}
