package fr.lium.spkDiarization.programs.ivector;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.IOFile;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.lib.SpkDiarizationLogger;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libFeature.AudioFeatureSet;
import fr.lium.spkDiarization.libMatrix.MatrixIO;
import fr.lium.spkDiarization.libMatrix.MatrixRectangular;
import fr.lium.spkDiarization.libModel.gaussian.GMM;
import fr.lium.spkDiarization.libModel.gaussian.GMMArrayList;
import fr.lium.spkDiarization.libModel.ivector.IVectorArrayList;
import fr.lium.spkDiarization.libModel.ivector.TotalVariability;
import fr.lium.spkDiarization.parameter.Parameter;

/**
 * The Class TrainIVectorOrTV.
 */
public class TrainIVectorOrTV {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(TrainIVectorOrTV.class.getName());

	/**
	 * Make.
	 * 
	 * @param clusterSet the cluster set
	 * @param featureSet the feature set
	 * @param parameter the parameter
	 * @return the i vector array list
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws SAXException the sAX exception
	 * @throws ParserConfigurationException the parser configuration exception
	 */
	public static IVectorArrayList make(ClusterSet clusterSet, AudioFeatureSet featureSet, Parameter parameter) throws DiarizationException, IOException, SAXException, ParserConfigurationException {
		GMMArrayList gmmList = MainTools.readGMMContainer(parameter);
		GMM ubm = gmmList.get(0);
		String fileNameMatrixU = IOFile.getFilename(parameter.getParameterTotalVariability().getTotalVariabilityMatrixMask(), parameter.show);
		int size = parameter.getParameterTotalVariability().getTotalVariabilityMatrixSize();
		int nbIt = parameter.getParameterTotalVariability().getNumberOfInteration();
		String fileNameBase = IOFile.getFilename(parameter.getParameterTotalVariability().getPartialTotalVariabilityMatrixMask(), parameter.show);
		String fileNameZeroOrderStatistic = IOFile.getFilename(parameter.getParameterTotalVariability().getZeroOrderStatisticMask(), parameter.show);
		String fileNameInitialTotalVariabilityMatrix = IOFile.getFilename(parameter.getParameterTotalVariability().getInitialTotalVariabilityMatrixMask(), parameter.show);
		String fileNameFirstOrderStatistic = IOFile.getFilename(parameter.getParameterTotalVariability().getFirstOrderStatisticMask(), parameter.show);
		boolean trainTotalVariabilityMatrix = parameter.getParameterTotalVariability().getTrainTotalVariabilityMatrix();

		TotalVariability totalFactorFactory = null;
		if ((fileNameMatrixU.isEmpty() == true) || (trainTotalVariabilityMatrix == true)) {
			if (fileNameInitialTotalVariabilityMatrix.isEmpty()) {
				totalFactorFactory = new TotalVariability(ubm, size);
			} else {
				MatrixRectangular totalFactorMatrix = MatrixIO.readRectMatrix(fileNameInitialTotalVariabilityMatrix, false);
				totalFactorFactory = new TotalVariability(ubm, totalFactorMatrix);
			}
		} else {
			MatrixRectangular totalFactorMatrix = MatrixIO.readRectMatrix(fileNameMatrixU, false);
			totalFactorFactory = new TotalVariability(ubm, totalFactorMatrix);
		}

		if (fileNameZeroOrderStatistic.isEmpty() || fileNameFirstOrderStatistic.isEmpty()) {
			logger.info("Compute statistics");
			totalFactorFactory.computeStatistics(clusterSet, featureSet, false);
		} else {
			logger.info("Load statistics");
			totalFactorFactory.loadStatistic(fileNameZeroOrderStatistic, fileNameFirstOrderStatistic);
		}
		if (trainTotalVariabilityMatrix == true) {
			logger.info("Train total variability matrix");
			MatrixRectangular totalFactorMatrix = totalFactorFactory.trainTotalVariabilityMatrix(nbIt, fileNameBase);
			MatrixIO.writeMatrix(totalFactorMatrix, fileNameMatrixU, false);
		}
		logger.info("Train i-vector");
		return totalFactorFactory.trainIVector();
	}

	/**
	 * The main method.
	 * 
	 * @param args the arguments
	 * @throws Exception the exception
	 */
	public static void main(String[] args) throws Exception {
		try {
			SpkDiarizationLogger.setup();
			Parameter parameter = MainTools.getParameters(args);
			info(parameter, "test");
			if (parameter.show.isEmpty() == false) {
				// clusters
				ClusterSet clusterSet = MainTools.readClusterSet(parameter);
				// Features
				AudioFeatureSet featureSet = MainTools.readFeatureSet(parameter, clusterSet);

				IVectorArrayList iVectorList = make(clusterSet, featureSet, parameter);
				MainTools.writeIVectorArrayList(iVectorList, parameter);

			}
		} catch (DiarizationException e) {
			logger.log(Level.SEVERE, "error \t exception ", e);
			e.printStackTrace();
		}
	}

	/**
	 * Info.
	 * 
	 * @param parameter the parameter
	 * @param program the program
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws InvocationTargetException the invocation target exception
	 */
	public static void info(Parameter parameter, String program) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (parameter.help) {
			logger.config(parameter.getSeparator2());
			logger.config("info[program] \t name = " + program);
			parameter.getSeparator();
			parameter.logShow();

			parameter.getParameterInputFeature().logAll(); // fInMask
			logger.config(parameter.getSeparator());
			parameter.getParameterSegmentationInputFile().logAll();
			parameter.getParameterSegmentationOutputFile().logAll(); // sOutMask
			logger.config(parameter.getSeparator());
			parameter.getParameterModelSetInputFile().logAll(); // tInMask
			logger.config(parameter.getSeparator());
			parameter.getParameterTotalVariability().logAll();
		}
	}

}
