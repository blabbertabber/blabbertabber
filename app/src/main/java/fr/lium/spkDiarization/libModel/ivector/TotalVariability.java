package fr.lium.spkDiarization.libModel.ivector;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.IOFile;
import fr.lium.spkDiarization.lib.SpkDiarizationLogger;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libFeature.AudioFeatureSet;
import fr.lium.spkDiarization.libMatrix.MatrixIO;
import fr.lium.spkDiarization.libMatrix.MatrixRectangular;
import fr.lium.spkDiarization.libMatrix.MatrixRowVector;
import fr.lium.spkDiarization.libMatrix.MatrixSymmetric;
import fr.lium.spkDiarization.libModel.gaussian.DiagGaussian;
import fr.lium.spkDiarization.libModel.gaussian.GMM;
import fr.lium.spkDiarization.libModel.gaussian.GMMFactory;
import fr.lium.spkDiarization.libModel.gaussian.Gaussian;

/**
 * TotalVariability class for i-vectors: M = m + T w This class is mostly based on Alize FactorAnalysisStat class.
 * 
 * @author meignier
 */
public class TotalVariability {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(TotalVariability.class.getName());

	/** The ubm. */
	private GMM ubm;

	/** The feature dimension. */
	int featureDimension;

	/** The nb component. */
	int nbComponent;

	/** The super vector dimension. */
	int superVectorDimension;

	/** The i vector dimension. */
	int iVectorDimension;

	/** The accumulator cluster name. */
	private ArrayList<String> accumulatorClusterName; // name of each entry in accumulators (= the name of the cluster)

	/** The accumulator cluster gender. */
	private ArrayList<String> accumulatorClusterGender; // name of each entry in accumulators (= the name of the cluster)

	/** The zero order statistic. */
	private ArrayList<MatrixRowVector> zeroOrderStatistic; // sum of Likelihood for each cluster

	/** The first order statistic. */
	private ArrayList<MatrixRowVector> firstOrderStatistic; // sum of likelihood x feature for each cluster
	private ArrayList<MatrixRowVector> normalizedFirstOrderStatistic; //  sum of likelihood x feature for each cluster minus likelihood x umb mean

	/** The zero order statistic copy. */
	//private ArrayList<MatrixRowVector> zeroOrderStatisticCopy; // copy of sum of Likelihood for each cluster

	/** The first order statistic copy. */
	//private ArrayList<MatrixRowVector> firstOrderStatisticCopy; // copy sum of likelihood x feature for each cluster

	/** The super mean ubm. */
	private MatrixRowVector superMeanUBM; // concatenation of UBM means (m)

	/** The super inverse covariance ubm. */
	private MatrixRowVector superInverseCovarianceUBM; // concatenation of UBM inverse covariance

	/** The total variability matrix. */
	private MatrixRectangular totalVariabilityMatrix; // low rank total variability matrix (T)

	/** The i vector list. */
	// private IVectorArrayList iVectorList; // list of i-vector (w)

	/** The _l_h_inv. */
	// private ArrayList<MatrixSymmetric> _l_h_inv;

	public void debug() {
		logger.info("accumulatorClusterName size : " + accumulatorClusterName.size());
		logger.info("accumulatorClusterGender size : " + accumulatorClusterGender.size());
		logger.info("ubm feature dimesion    : " + featureDimension);
		logger.info("ubm number of components: " + nbComponent);
		logger.info("super vector size       : " + superVectorDimension);
		logger.info("zeroOrderStatistic size : " + zeroOrderStatistic.size());
		if (zeroOrderStatistic.size() > 0) {
			logger.info("zeroOrderStatistic size of get(0) : " + zeroOrderStatistic.get(0).getSize());
		}
		logger.info("firstOrderStatistic size : " + firstOrderStatistic.size());
		if (firstOrderStatistic.size() > 0) {
			logger.info("firstOrderStatistic size of get(0) : " + firstOrderStatistic.get(0).getSize());
		}
		/*
		 * logger.info("_l_h_inv size : "+_l_h_inv.size()); if (_l_h_inv.size() > 0) { logger.info("_l_h_inv size of get(0) : "+_l_h_inv.get(0).getSize()); } logger.info("iVectorList size : "+iVectorList.size()); if (iVectorList.size() > 0) {
		 * logger.info("iVectorList size of get(0) : "+iVectorList.get(0).getDimension()); }
		 */
	}

	/**
	 * Initialize.
	 * 
	 * @param _ubm the _ubm
	 * @throws DiarizationException the diarization exception
	 */
	protected void initialize(GMM _ubm) throws DiarizationException {
		ubm = _ubm;

		featureDimension = ubm.getDimension();
		nbComponent = ubm.getNbOfComponents();
		superVectorDimension = featureDimension * nbComponent;

		gmm2SuperVectors(ubm);

		zeroOrderStatistic = new ArrayList<MatrixRowVector>(0);
		firstOrderStatistic = new ArrayList<MatrixRowVector>(0);
		//zeroOrderStatisticCopy = new ArrayList<MatrixRowVector>(0);
		//firstOrderStatisticCopy = new ArrayList<MatrixRowVector>(0);
		normalizedFirstOrderStatistic = new ArrayList<MatrixRowVector>(0);
		accumulatorClusterName = new ArrayList<String>(0);
		accumulatorClusterGender = new ArrayList<String>(0);
		// _l_h_inv = new ArrayList<MatrixSymmetric>(0);
		// accumulatorCluster = new ArrayList<String>(0);
		// iVectorList = new IVectorArrayList();

	}

	// _gmm contains the model and the accumulator
	/**
	 * Instantiates a new total variability.
	 * 
	 * @param _ubm the _ubm
	 * @param _rang the _rang
	 * @throws DiarizationException the diarization exception
	 */
	public TotalVariability(GMM _ubm, int _rang) throws DiarizationException {
		initialize(_ubm);
		iVectorDimension = _rang;
		totalVariabilityMatrix = initializeTotalVariabilityMatrix(superVectorDimension, iVectorDimension, ubm);
	}

	/**
	 * Instantiates a new total variability.
	 * 
	 * @param _ubm the _ubm
	 * @param _matrixU the _matrix u
	 * @throws DiarizationException the diarization exception
	 */
	public TotalVariability(GMM _ubm, MatrixRectangular _matrixU) throws DiarizationException {
		initialize(_ubm);

		totalVariabilityMatrix = _matrixU;
		iVectorDimension = totalVariabilityMatrix.numCols();
		if (totalVariabilityMatrix.numRows() != superVectorDimension) {
			throw new DiarizationException("Total variability matrix row size problem: "
					+ totalVariabilityMatrix.numRows() + " vs. " + superVectorDimension);
		}
		if (totalVariabilityMatrix.numCols() != iVectorDimension) {
			throw new DiarizationException("Total variability matrix column size problem: "
					+ totalVariabilityMatrix.numCols() + " vs. " + iVectorDimension);
		}
	}

	/**
	 * Adds the accumulator from gmm.
	 * 
	 * @param gmm the gmm
	 * @throws DiarizationException the diarization exception
	 */
	protected void addAccumulatorFromGMM(GMM gmm) throws DiarizationException {

		MatrixRowVector super0Order = new MatrixRowVector(nbComponent);
		MatrixRowVector super1Order = new MatrixRowVector(superVectorDimension);
		// DoubleVector super2Order = new DoubleVector(superVectorSize);

		int j = 0, k = 0;
		for (Gaussian gaussian : gmm) {
			// for (int k = 0; k < gmm.componentList.size(); k++) {
			DiagGaussian dg = (DiagGaussian) gaussian;
			super0Order.set(k, dg.getStatistic().getZeroOrder());
			for (int i = 0; i < featureDimension; i++) {
				super1Order.set(j, dg.getStatistic().getFirstOrder().get(i));
				// super2Order.set(j, dg.getAccumulator().getCovarianceAccumulator().get(i));
				j++;
			}
			k++;
		}
		/*
		 * logger.info("0Order: "); for(int i = 0; i < super0Order.getDimension(); i++) { logger.info("i :"+i+" = "+super0Order.get(i)); } logger.info("1Order: "); for(int i = 0; i < super1Order.getDimension(); i++) {
		 * logger.info("i :"+i+" = "+super1Order.get(i)); }
		 */

		zeroOrderStatistic.add(super0Order);
		firstOrderStatistic.add(super1Order);
		// accumulator2Order.add(super2Order);
	}

	/**
	 * Gmm2 super vectors.
	 * 
	 * @param gmm the gmm
	 * @throws DiarizationException the diarization exception
	 */
	protected void gmm2SuperVectors(GMM gmm) throws DiarizationException {
		int dim = gmm.getDimension();
		int nbComp = gmm.getNbOfComponents();
		int size = dim * nbComp;
		superMeanUBM = new MatrixRowVector(size);
		superInverseCovarianceUBM = new MatrixRowVector(size);

		int j = 0;
		for (Gaussian g : gmm) {
			for (int i = 0; i < dim; i++) {
				superMeanUBM.set(j, g.getMean(i));
				superInverseCovarianceUBM.set(j, g.getInvertCovariance(i, i));
				j++;
			}
		}
	}

	/**
	 * Load statistic.
	 * 
	 * @param fileName the file name
	 * @param statistic the statistic
	 * @throws FileNotFoundException the file not found exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected void loadStatistic(String fileName, ArrayList<MatrixRowVector> statistic) throws FileNotFoundException, IOException {
		MatrixRectangular matrix = MatrixIO.readRectMatrix(fileName, false);

		zeroOrderStatistic.ensureCapacity(matrix.numRows());
		firstOrderStatistic.ensureCapacity(matrix.numRows());
		normalizedFirstOrderStatistic.ensureCapacity(matrix.numRows());
//		zeroOrderStatisticCopy.ensureCapacity(matrix.numRows());
//		firstOrderStatisticCopy.ensureCapacity(matrix.numRows());

		for (int i = 0; i < matrix.numRows(); i++) {
			MatrixRowVector vector = new MatrixRowVector(matrix.numCols());
			for (int j = 0; j < matrix.numCols(); j++) {
				vector.set(j, matrix.get(i, j));
			}
			statistic.add(vector);
		}
	}

	/**
	 * Load zero order statistic.
	 * 
	 * @param fileName the file name
	 * @throws FileNotFoundException the file not found exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected void loadZeroOrderStatistic(String fileName) throws FileNotFoundException, IOException {
		loadStatistic(fileName, zeroOrderStatistic);
	}

	/**
	 * Load first order statistic.
	 * 
	 * @param fileName the file name
	 * @throws FileNotFoundException the file not found exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected void loadFirstOrderStatistic(String fileName) throws FileNotFoundException, IOException {
		loadStatistic(fileName, firstOrderStatistic);
	}

	/**
	 * Load statistic.
	 * 
	 * @param fileNameZeroOrder the file name zero order
	 * @param fileNameFirstOrder the file name first order
	 * @throws FileNotFoundException the file not found exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void loadStatistic(String fileNameZeroOrder, String fileNameFirstOrder) throws FileNotFoundException, IOException {
		loadFirstOrderStatistic(fileNameFirstOrder);
		loadZeroOrderStatistic(fileNameZeroOrder);
		if (SpkDiarizationLogger.DEBUG) logger.info("substract speaker statistics");
		substractSpeakerStats();
	}

	protected void copyStatistic(ArrayList<MatrixRowVector> src, ArrayList<MatrixRowVector> dest) {
		dest.clear();
		for (MatrixRowVector s : src) {
			MatrixRowVector d = new MatrixRowVector(s.getSize());
			for (int i = 0; i < s.getSize(); i++) {
				d.set(i, s.get(i));
			}
			dest.add(d);
		}
	}

	/**
	 * Save statistic.
	 * 
	 * @param fileName the file name
	 * @param statistic the statistic
	 * @throws FileNotFoundException the file not found exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected void saveStatistic(String fileName, ArrayList<MatrixRowVector> statistic) throws FileNotFoundException, IOException {
		MatrixRectangular matrix = new MatrixRectangular(statistic.size(), superVectorDimension);

		for (int i = 0; i < matrix.numRows(); i++) {
			MatrixRowVector vector = statistic.get(i);
			for (int j = 0; j < matrix.numCols(); j++) {
				matrix.set(i, j, vector.get(j));
			}
			statistic.add(vector);
		}
		MatrixIO.writeMatrix(matrix, fileName, false);
	}

	/**
	 * Copy statistic.
	 *
	protected void copyStatistic() {
		logger.info("copy statistic");
		copyStatistic(zeroOrderStatistic, zeroOrderStatisticCopy);
		copyStatistic(firstOrderStatistic, firstOrderStatisticCopy);

		// zeroOrderStatisticCopy = (ArrayList<MatrixRowVector>) zeroOrderStatistic.clone();
		// firstOrderStatisticCopy = (ArrayList<MatrixRowVector>) firstOrderStatistic.clone();
	}*/

	/**
	 * Restore statistic.
	 *
	protected void restoreStatistic() {
		logger.info("restore statistic");
		copyStatistic(zeroOrderStatisticCopy, zeroOrderStatistic);
		copyStatistic(firstOrderStatisticCopy, firstOrderStatistic);
		// zeroOrderStatistic = (ArrayList<MatrixRowVector>) zeroOrderStatisticCopy.clone();
		// firstOrderStatistic = (ArrayList<MatrixRowVector>) firstOrderStatisticCopy.clone();
	}*/

	/**
	 * Compute statistics.
	 * 
	 * @param clusterSet the cluster set
	 * @param featureSet the feature set
	 * @param useSpeechDetection the use speech detection
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void computeStatistics(ClusterSet clusterSet, AudioFeatureSet featureSet, boolean useSpeechDetection) throws DiarizationException, IOException {
		// calculer les occupations, occ1, occ2 à partir de l'UBM
		// attention le clusterSet doit être correctement organisé (session), cf ClusterSet.toSpeakerSession()
		int minCapacity = clusterSet.clusterGetSize();
		if (SpkDiarizationLogger.DEBUG) logger.finest("capacity :" + minCapacity);
		zeroOrderStatistic.ensureCapacity(minCapacity);
		firstOrderStatistic.ensureCapacity(minCapacity);
		normalizedFirstOrderStatistic.ensureCapacity(minCapacity);
//		zeroOrderStatisticCopy.ensureCapacity(minCapacity);
//		firstOrderStatisticCopy.ensureCapacity(minCapacity);
		accumulatorClusterName.ensureCapacity(minCapacity);
		accumulatorClusterGender.ensureCapacity(minCapacity);

		for (String clusterName : clusterSet) {
			Cluster cluster = clusterSet.getCluster(clusterName);
			GMM gmm = (ubm.clone());
			GMMFactory.iterationAccumulation(cluster, featureSet, ubm, gmm, useSpeechDetection);
			addAccumulatorFromGMM(gmm);
			accumulatorClusterName.add(clusterName);
			accumulatorClusterGender.add(cluster.getGender());
		}
		if (SpkDiarizationLogger.DEBUG) logger.info("substract speaker statistics");
		substractSpeakerStats();

	}

	/**
	 * Estimate l.
	 * 
	 * @throws DiarizationException the diarization exception
	 */
	protected ArrayList<MatrixSymmetric> estimateL() throws DiarizationException {
		ArrayList<MatrixSymmetric> _l_h_inv = new ArrayList<MatrixSymmetric>(zeroOrderStatistic.size());
		MatrixSymmetric matrixL = new MatrixSymmetric(iVectorDimension); // L(_rang)

		for (int sent = 0; sent < zeroOrderStatistic.size(); sent++) {
			if ((sent % 1000) == 0) {
				logger.info("\t sent #: " + sent);
			}
			matrixL.fill(0.0);
			MatrixRowVector sentN = zeroOrderStatistic.get(sent);
			for (int g = 0; g < nbComponent; g++) {
				double zeroOrderStatistic4n_g = sentN.get(g);
				for (int l = 0; l < featureDimension; l++) {
					int k = (g * featureDimension) + l;
					double valueK = zeroOrderStatistic4n_g * superInverseCovarianceUBM.get(k); 
					for (int i = 0; i < iVectorDimension; i++) {
						double valueI = valueK * totalVariabilityMatrix.get(k, i);
						for (int j = i; j < iVectorDimension; j++) {
							matrixL.add(i, j, valueI * totalVariabilityMatrix.get(k, j));
						}
					}
				}
			}

			for (int i = 0; i < iVectorDimension; i++) {
				matrixL.add(i, i, 1.0);
			}
			MatrixSymmetric matrixLI = matrixL.invert();
			_l_h_inv.add(matrixLI);
		}
		return _l_h_inv;
	}

	/**
	 * Substract speaker stats.
	 */
	protected void substractSpeakerStats() {
		// Alize AccumulateTV::substractM
		// C'est constant ? si oui, alors sauvegarder firstOrder
		
		normalizedFirstOrderStatistic.clear();
		
		for (int sent = 0; sent < zeroOrderStatistic.size(); sent++) { // for each speaker
			MatrixRowVector sentN = zeroOrderStatistic.get(sent);
			MatrixRowVector sentSX = firstOrderStatistic.get(sent);
			MatrixRowVector sentSX_norm = sentSX.copy();
			normalizedFirstOrderStatistic.add(sentSX_norm);
			for (int i = 0; i < nbComponent; i++) {
				int iDec = i * featureDimension;
				for (int j = 0; j < featureDimension; j++) {
					double value = -(sentN.get(i) * superMeanUBM.get(iDec + j));
					sentSX_norm.add(iDec + j, value);
				}
			}

		}
	}

	/**
	 * Estimate total variability matrix.
	 * 
	 * @throws DiarizationException the diarization exception
	 */
	protected void estimateTotalVariabilityMatrix(ArrayList<MatrixSymmetric> _l_h_inv, IVectorArrayList iVectorList) throws DiarizationException {
		MatrixRowVector C = new MatrixRowVector(iVectorDimension);
		MatrixSymmetric A = new MatrixSymmetric(iVectorDimension);

		totalVariabilityMatrix.fill(0.0);
		for (int g = 0; g < nbComponent; g++) {
			A.fill(0.0);

			for (int j = 0; j < zeroOrderStatistic.size(); j++) {
				MatrixSymmetric matrixLInv = _l_h_inv.get(j);
				IVector iVector = iVectorList.get(j);
				for (int k = 0; k < iVectorDimension; k++) {
					for (int l = k; l < iVectorDimension; l++) {
						double value = (matrixLInv.get(k, l) + (iVector.get(k) * iVector.get(l)))
								* zeroOrderStatistic.get(j).get(g);
						A.add(k, l, value); // ok
					}
				}
			}
			MatrixSymmetric AInvert = A.invert(); // ok

			for (int i = 0; i < featureDimension; i++) {
				C.fill(0.0); // ok
				int gi = (g * featureDimension) + i; // ok
				for (int j = 0; j < zeroOrderStatistic.size(); j++) {
					for (int k = 0; k < iVectorDimension; k++) {
						C.add(k, normalizedFirstOrderStatistic.get(j).get(gi) * iVectorList.get(j).get(k));// ok
//						R.add(k, firstOrderStatistic.get(j).get(gi) * iVectorList.get(j).get(k));// ok
					}
				}

				for (int j = 0; j < iVectorDimension; j++) {
					for (int k = 0; k < iVectorDimension; k++) {
						totalVariabilityMatrix.add(gi, j, AInvert.get(j, k) * C.get(k));// ok
					}
				}
			}
		}

	}

	/**
	 * Estimate i vector.
	 */
	protected IVectorArrayList estimateIVector(ArrayList<MatrixSymmetric> listOfLInvert) {
		// matrice U, occ, order1, L, superCov
		IVectorArrayList iVectorList = new IVectorArrayList();
		iVectorList.ensureCapacity(zeroOrderStatistic.size());
		MatrixRowVector tmp = new MatrixRowVector(iVectorDimension);

		for (int sent = 0; sent < zeroOrderStatistic.size(); sent++) {
			MatrixRowVector W = new MatrixRowVector(iVectorDimension);
			W.fill(0.0);
			tmp.fill(0.0);

			for (int i = 0; i < iVectorDimension; i++) {
				for (int k = 0; k < superVectorDimension; k++) {
					double v = totalVariabilityMatrix.get(k, i);
					v *= superInverseCovarianceUBM.get(k);
					//v *= firstOrderStatistic.get(sent).get(k);
					v *= normalizedFirstOrderStatistic.get(sent).get(k);
					tmp.add(i, v);
				}
			}

			for (int i = 0; i < iVectorDimension; i++) {
				for (int k = 0; k < iVectorDimension; k++) {
					W.add(i, listOfLInvert.get(sent).get(i, k) * tmp.get(k));
				}
			}
			if (accumulatorClusterName.size() == 0) {
				iVectorList.add(new IVector(W, "sent_" + sent, Cluster.genderStrings[0]));
			} else {
				iVectorList.add(new IVector(W, accumulatorClusterName.get(sent), accumulatorClusterGender.get(sent)));
			}
		}
		return iVectorList;
	}

	/**
	 * Train i vector.
	 * 
	 * @return the i vector array list
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public IVectorArrayList trainIVector() throws DiarizationException, IOException {
		// computeStatistics(clusterSet, featureSet, false);
		if (SpkDiarizationLogger.DEBUG) logger.info("trainIVector: estimate L");
		ArrayList<MatrixSymmetric> _l_h_inv = estimateL();
		//logger.info("trainIVector: substract speaker statistics");
		//substractSpeakerStats();
		if (SpkDiarizationLogger.DEBUG) logger.info("trainIVector: estimate i-vector");
		IVectorArrayList iVectorList = estimateIVector(_l_h_inv);
		return iVectorList;
	}

	/**
	 * Train total variability matrix.
	 * 
	 * @param nbIteration the nb iteration
	 * @param fileNameBase the file name base
	 * @return the matrix rectangular
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public MatrixRectangular trainTotalVariabilityMatrix(int nbIteration, String fileNameBase) throws DiarizationException, IOException {
		if (fileNameBase.isEmpty() == false) {
			String fileName = IOFile.getFilename(fileNameBase, -1);
			MatrixIO.writeMatrix(totalVariabilityMatrix, fileName, false);
		}

		// computeStatistics(clusterSet, featureSet, false);
		//copyStatistic();
		for (int i = 0; i < nbIteration; i++) {
			logger.info("iteration :" + i);
			if (SpkDiarizationLogger.DEBUG) debug();
			if (SpkDiarizationLogger.DEBUG) logger.info("---->start: " + zeroOrderStatistic.get(0).get(0));
			if (SpkDiarizationLogger.DEBUG) logger.info("---->start: " + firstOrderStatistic.get(0).get(0));
			if (SpkDiarizationLogger.DEBUG) logger.info("\ttrainTotalVariabilityMatrix: estimate L");
			ArrayList<MatrixSymmetric> _l_h_inv = estimateL();
			if (SpkDiarizationLogger.DEBUG) logger.info("---->after L: " + zeroOrderStatistic.get(0).get(0));
			if (SpkDiarizationLogger.DEBUG) logger.info("---->after L: " + firstOrderStatistic.get(0).get(0));
			if (SpkDiarizationLogger.DEBUG) logger.info("\ttrainTotalVariabilityMatrix: estimate i-vector");
			IVectorArrayList iVectorList = estimateIVector(_l_h_inv);
			if (SpkDiarizationLogger.DEBUG) logger.info("---->after Iv: " + zeroOrderStatistic.get(0).get(0));
			if (SpkDiarizationLogger.DEBUG) logger.info("---->after Iv: " + firstOrderStatistic.get(0).get(0));
			if (SpkDiarizationLogger.DEBUG) logger.info("\ttrainTotalVariabilityMatrix: estimate TV matrix");
			estimateTotalVariabilityMatrix(_l_h_inv, iVectorList);
			if (fileNameBase.isEmpty() == false) {
				String fileName = IOFile.getFilename(fileNameBase, i);
				MatrixIO.writeMatrix(totalVariabilityMatrix, fileName, false);
			}
			//logger.info("---->before: " + zeroOrderStatistic.get(0).get(0));
			//logger.info("---->before: " + firstOrderStatistic.get(0).get(0));
			//restoreStatistic();
			//logger.info("---->after: " + zeroOrderStatistic.get(0).get(0));
			//logger.info("---->after: " + firstOrderStatistic.get(0).get(0));
		}
		return totalVariabilityMatrix;
	}

	/**
	 * Initialize total variability matrix.
	 * 
	 * @param rows the rows
	 * @param cols the cols
	 * @param ubm the ubm
	 * @return the matrix rectangular
	 * @throws DiarizationException the diarization exception
	 */
	protected static MatrixRectangular initializeTotalVariabilityMatrix(int rows, int cols, GMM ubm) throws DiarizationException {
		double sumInvertCovariance = 0.0;
		for (Gaussian g : ubm) {
			DiagGaussian dg = (DiagGaussian) g;
			for (int i = 0; i < dg.getDimension(); i++) {
				sumInvertCovariance += dg.getCovariance(i, i);
			}
		}
		return MatrixIO.createGaussianRandom(rows, cols, 0, Math.sqrt(sumInvertCovariance / ubm.getDimension()));
	}

}
