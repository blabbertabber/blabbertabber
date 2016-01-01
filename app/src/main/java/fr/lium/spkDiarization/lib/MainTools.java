/**
 * 
 * <p>
 * MainTools
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
 */

package fr.lium.spkDiarization.lib;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
//// import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.libFeature.AudioFeatureSet;
import fr.lium.spkDiarization.libMatrix.MatrixIO;
import fr.lium.spkDiarization.libMatrix.MatrixSymmetric;
import fr.lium.spkDiarization.libModel.gaussian.GMMArrayList;
import fr.lium.spkDiarization.libModel.gaussian.ModelIO;
import fr.lium.spkDiarization.libModel.ivector.EigenFactorRadialList;
import fr.lium.spkDiarization.libModel.ivector.IVectorArrayList;
import fr.lium.spkDiarization.parameter.Parameter;
import fr.lium.spkDiarization.parameter.ParameterModel;
import fr.lium.spkDiarization.parameter.ParameterModelSet.ModelFormat;

/**
 * This class contains generic tools to read and write models, features and segmentations.
 */
public class MainTools {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(MainTools.class.getName());

	/**
	 * Gets the GMM for top gaussian.
	 * 
	 * @param parameter the param
	 * @param featureSet the features
	 * 
	 * @return the GMM for top gaussian
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws DiarizationException the diarization exception
	 */
	public static GMMArrayList readGMMForTopGaussian(Parameter parameter, AudioFeatureSet featureSet) throws IOException, DiarizationException {
		GMMArrayList gmmList = new GMMArrayList();
		if (parameter.getParameterTopGaussian().getScoreNTop() >= 0) {
			String modelTopFilename = IOFile.getFilename(parameter.getParameterTopGaussian().getScoreNTopGMMMask(), parameter.show);
			File inputFile = new File(modelTopFilename);
			String mode = "rb";
			IOFile inputFileReader = new IOFile(modelTopFilename, mode);
			inputFileReader.open();
			if (parameter.getParameterModelSetInputFile().getFormat() == 1) {
				inputFileReader.setSwap(true);
				String name = inputFile.getName().split("[.]")[0];
				ModelIO.readerGMMContainerALIZE(inputFileReader, gmmList, name);
			} else {
				ModelIO.readerGMMContainer(inputFileReader, gmmList);
			}
			inputFileReader.close();
			gmmList.get(0).sortComponents();
		}
		featureSet.setUbmList(gmmList);
		return gmmList;
	}

	/**
	 * Read the cluster set.
	 * 
	 * @param parameter the parameter
	 * 
	 * @return the input clusters
	 * 
	 * @throws DiarizationException the diarization exception
	 * @throws Exception the exception
	 */
	public static ClusterSet readClusterSet(Parameter parameter) throws DiarizationException, Exception {
		// clusters
		ClusterSet clusterSet = new ClusterSet();
		clusterSet.read(parameter.show, parameter.getParameterSegmentationInputFile());
		return clusterSet;
	}

	/**
	 * Read the second cluster set.
	 * 
	 * @param parameter the parameter
	 * 
	 * @return the input clusters2
	 * 
	 * @throws DiarizationException the diarization exception
	 * @throws Exception the exception
	 */
	public static ClusterSet readTheSecondClusterSet(Parameter parameter) throws DiarizationException, Exception {
		// clusters
		if (parameter.getParameterSegmentationInputFile2().getMask().isEmpty() == false) {
			ClusterSet clusterSet = new ClusterSet();
			clusterSet.read(parameter.show, parameter.getParameterSegmentationInputFile2());
			return clusterSet;
		} else {
			return null;
		}
	}

	/**
	 * Read the 3rd cluster set.
	 * 
	 * @param parameter the parameter
	 * 
	 * @return the input clusters2
	 * 
	 * @throws DiarizationException the diarization exception
	 * @throws Exception the exception
	 */
	public static ClusterSet readThe3rdClusterSet(Parameter parameter) throws DiarizationException, Exception {
		// clusters
		if (parameter.getParameterSegmentationInputFile3().getMask().isEmpty() == false) {
			ClusterSet clusterSet = new ClusterSet();
			clusterSet.read(parameter.show, parameter.getParameterSegmentationInputFile3());
			return clusterSet;
		} else {
			return null;
		}
	}

	/**
	 * Read the featureSet.
	 * 
	 * @param parameter the parameter
	 * @param clusterSet the clusters
	 * 
	 * @return the input features
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws DiarizationException the diarization exception
	 */
	public static AudioFeatureSet readFeatureSet(Parameter parameter, ClusterSet clusterSet) throws IOException, DiarizationException {
		AudioFeatureSet featureSet = new AudioFeatureSet(clusterSet, parameter.getParameterInputFeature());
		if (SpkDiarizationLogger.DEBUG) featureSet.debug();
		return featureSet;
	}

	/**
	 * Read the second featureSet.
	 * 
	 * @param parameter the parameter
	 * @param clusterSet the clusters
	 * 
	 * @return the input features
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws DiarizationException the diarization exception
	 */
	public static AudioFeatureSet readTheSecondFeatureSet(Parameter parameter, ClusterSet clusterSet) throws IOException, DiarizationException {
		AudioFeatureSet featureSet = new AudioFeatureSet(clusterSet, parameter.getParameterInputFeature2());
		if (SpkDiarizationLogger.DEBUG) featureSet.debug();
		return featureSet;
	}

	/**
	 * Read the first featureSet.
	 * 
	 * @param parameter the parameter
	 * @param clusterSet the clusters
	 * @param featureSetBase the features base
	 * 
	 * @return the input features
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws DiarizationException the diarization exception
	 */
	public static AudioFeatureSet readFeatureSet(Parameter parameter, ClusterSet clusterSet, AudioFeatureSet featureSetBase) throws IOException, DiarizationException {
		AudioFeatureSet features = new AudioFeatureSet(featureSetBase, clusterSet, parameter.getParameterInputFeature());
		if (SpkDiarizationLogger.DEBUG) features.debug();
		return features;
	}

	/**
	 * Read i-vector array list.
	 * 
	 * @param parameter the parameter
	 * @return the i vector array list
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws DiarizationException the diarization exception
	 */
	public static IVectorArrayList readIVectorArrayList(Parameter parameter) throws IOException, DiarizationException {
		String inputFilename = IOFile.getFilename(parameter.getParameterModelSetInputFile().getMask(), parameter.show);
		logger.info("I-Vector read: " + inputFilename);
		return IVectorArrayList.loadIVector(inputFilename);
	}

	/**
	 * Write i-vector array list.
	 * 
	 * @param iVectorArrayList the i vector array list
	 * @param parameter the parameter
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws DiarizationException the diarization exception
	 */
	public static void writeIVectorArrayList(IVectorArrayList iVectorArrayList, Parameter parameter) throws IOException, DiarizationException {
		String outputFilename = IOFile.getFilename(parameter.getParameterModelSetOutputFile().getMask(), parameter.show);
		logger.info("I-Vector write: " + outputFilename);
		IVectorArrayList.writeIVector(outputFilename, iVectorArrayList);
	}

	/**
	 * Read i-vector array list2.
	 * 
	 * @param parameter the parameter
	 * @return the i vector array list
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws DiarizationException the diarization exception
	 */
	public static IVectorArrayList readIVectorArrayList2(Parameter parameter) throws IOException, DiarizationException {
		String inputFilename = IOFile.getFilename(parameter.getParameterModelSetInputFile2().getMask(), parameter.show);
		logger.info("I-Vector 2 read: " + inputFilename);
		return IVectorArrayList.loadIVector(inputFilename);
	}

	/**
	 * Read eigen factor radial normalization.
	 * 
	 * @param parameter the parameter
	 * @return the eigen factor radial list
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException the class not found exception
	 */
	public static EigenFactorRadialList readEigenFactorRadialNormalization(Parameter parameter) throws IOException, ClassNotFoundException {
		String inputFilename = IOFile.getFilename(parameter.getParameterNormlization().getEigenFactorRadialMask(), parameter.show);
		EigenFactorRadialList interSessionCompensation = EigenFactorRadialList.readXML(inputFilename);
		logger.info("EigenFactorRadial read: " + inputFilename + " number of iteration: "
				+ interSessionCompensation.size());
		return interSessionCompensation;
	}

	/**
	 * Write eigen factor radial normalization.
	 * 
	 * @param interSessionCompensation the inter session compensation
	 * @param parameter the parameter
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void writeEigenFactorRadialNormalization(EigenFactorRadialList interSessionCompensation, Parameter parameter) throws IOException {
		String outputFilename = IOFile.getFilename(parameter.getParameterNormlization().getEigenFactorRadialMask(), parameter.show);
		logger.info("EigenFactorRadial write: " + outputFilename + " number of iteration: "
				+ interSessionCompensation.size());
		EigenFactorRadialList.writeXML(interSessionCompensation, outputFilename);
	}

	/**
	 * Read wccn matrix.
	 * 
	 * @param parameter the parameter
	 * @return the matrix symmetric
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static MatrixSymmetric readWCCNMatrix(Parameter parameter) throws IOException {
		String inputFilename = IOFile.getFilename(parameter.getParameterNormlization().getWCCNMask(), parameter.show);
		logger.info("WCCN Matrix read: " + inputFilename);
		return MatrixIO.readMatrixSymmetric(inputFilename, false);
	}

	/**
	 * Write wccn matrix.
	 * 
	 * @param covariance the covariance
	 * @param parameter the parameter
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void writeWCCNMatrix(MatrixSymmetric covariance, Parameter parameter) throws IOException {
		String outputFilename = IOFile.getFilename(parameter.getParameterNormlization().getWCCNMask(), parameter.show);
		logger.info("WCCN Matrix write: " + outputFilename);
		MatrixIO.writeMatrix(covariance, outputFilename, false);
	}

	/**
	 * Read mahanalonis covariance matrix.
	 * 
	 * @param parameter the parameter
	 * @return the matrix symmetric
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static MatrixSymmetric readMahanalonisCovarianceMatrix(Parameter parameter) throws IOException {
		String inputFilename = IOFile.getFilename(parameter.getParameterNormlization().getMahanalobisCovarianceMask(), parameter.show);
		logger.info("Mahanalobis Covariance read: " + inputFilename);
		return MatrixIO.readMatrixSymmetric(inputFilename, false);
	}

	/**
	 * Write w mahanalonis covariance matrix.
	 * 
	 * @param covariance the covariance
	 * @param parameter the parameter
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void writeWMahanalonisCovarianceMatrix(MatrixSymmetric covariance, Parameter parameter) throws IOException {
		String outputFilename = IOFile.getFilename(parameter.getParameterNormlization().getMahanalobisCovarianceMask(), parameter.show);
		logger.info("Mahanalobis Covariance write: " + outputFilename);
		MatrixIO.writeMatrix(covariance, outputFilename, false);
	}

	/**
	 * Read a GMM container.
	 * 
	 * @param parameter the parameter
	 * @return the input gmm container
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws SAXException the sAX exception
	 * @throws ParserConfigurationException the parser configuration exception
	 */
	public static GMMArrayList readGMMContainer(Parameter parameter) throws DiarizationException, IOException, SAXException, ParserConfigurationException {
		GMMArrayList gmmList = new GMMArrayList();
		String inputFilename = IOFile.getFilename(parameter.getParameterModelSetInputFile().getMask(), parameter.show);
		// logger.info("read GMM "+inputFilename);
		if (inputFilename.equals("")) {
			logger.warning("error: input model empty " + inputFilename);
			return null;
		}
		File inputFile = new File(inputFilename);
		if (inputFile.exists() == false) {
			logger.warning("error: file not found " + inputFilename);
			return null;
		}
		IOFile inputFileReader = new IOFile(inputFilename, "rb");
		inputFileReader.open();
		if (parameter.getParameterModelSetInputFile().getFormat() == ModelFormat.ALIZE.ordinal()) {
			inputFileReader.setSwap(true);
			String name = inputFile.getName().split("[.]")[0];
			ModelIO.readerGMMContainerALIZE(inputFileReader, gmmList, name);
		} else if (parameter.getParameterModelSetInputFile().getFormat() == ModelFormat.ALIZEXML.ordinal()) {
// logger.info("read GMM alize xml");
			ModelIO.readGMMContainerXMLAlize(new File(inputFilename), gmmList);
// logger.info("read GMMalize xml done");
		} else {
			ModelIO.readerGMMContainer(inputFileReader, gmmList);
		}
		inputFileReader.close();
		if (gmmList.size() > 0) {
			parameter.getParameterModel().setKind(gmmList.get(0).getGaussianKind());
			parameter.getParameterModel().setNumberOfComponents(gmmList.get(0).getNbOfComponents());
		}
		/*
		 * for (int i = 0; i < gmmList.size(); i++) { gmmList.get(i).sortComponents(); }
		 */
		return gmmList;
	}

	/**
	 * Read a gmm container.
	 * 
	 * @param inputStream the url og the GMM file
	 * @param parameterModel the parameter model
	 * 
	 * @return the input gmm container
	 * 
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static GMMArrayList readGMMContainer(InputStream inputStream, ParameterModel parameterModel) throws DiarizationException, IOException {

		IOFile inputFileReader = new IOFile(inputStream);

		GMMArrayList gmmList = new GMMArrayList();
		inputFileReader.open();
		ModelIO.readerGMMContainer(inputFileReader, gmmList);
		inputFileReader.close();
		if (gmmList.size() > 0) {
			parameterModel.setKind(gmmList.get(0).getGaussianKind());
			parameterModel.setNumberOfComponents(gmmList.get(0).getNbOfComponents());
		}
		for (int i = 0; i < gmmList.size(); i++) {
			gmmList.get(i).sortComponents();
		}
		return gmmList;
	}

	/**
	 * Gets the parameter.
	 * 
	 * @param args the argument of the program
	 * 
	 * @return the param
	 */
	public static Parameter getParameters(String args[]) {
		Parameter parameter = new Parameter();
		parameter.readParameters(args);
		if (args.length <= 1) {
			parameter.help = true;
		}
		parameter.logCmdLine(args);
		return parameter;
	}

	/**
	 * Write the output clusters.
	 * 
	 * @param parameter the parameter
	 * @param clusterSet the clusters
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ParserConfigurationException the parser configuration exception
	 * @throws SAXException the SAX exception
	 * @throws DiarizationException the diarization exception
	 * @throws TransformerException the transformer exception
	 */
	public static void writeClusterSet(Parameter parameter, ClusterSet clusterSet) throws IOException, ParserConfigurationException, SAXException, DiarizationException, TransformerException {
		writeClusterSet(parameter, clusterSet, false);
	}

	/**
	 * Write the output clusters.
	 * 
	 * @param parameter the parameter
	 * @param clusterSet the clusters
	 * @param collapse the segment of each cluster
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ParserConfigurationException the parser configuration exception
	 * @throws SAXException the SAX exception
	 * @throws DiarizationException the diarization exception
	 * @throws TransformerException the transformer exception
	 */
	public static void writeClusterSet(Parameter parameter, ClusterSet clusterSet, boolean collapse) throws IOException, ParserConfigurationException, SAXException, DiarizationException, TransformerException {
		if (collapse) {
			clusterSet.collapse();
		}
		clusterSet.write(parameter.show, parameter.getParameterSegmentationOutputFile());
	}

	/**
	 * Write a stringList.
	 * 
	 * @param parameter the parameter
	 * @param stringList the string list
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void writeStringList(Parameter parameter, ArrayList<String> stringList) throws IOException {
		String outputFilename = IOFile.getFilename(parameter.getParameterSegmentationOutputFile().getMask(), parameter.show);
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFilename), Parameter.DefaultCharset));

		for (String line : stringList) {
			writer.write(line + "\n");
		}

		writer.close();
	}

	/**
	 * Write a featureSet.
	 * 
	 * @param parameter the parameter
	 * @param featureSet the featureSet
	 * 
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void writeFeatureSet(Parameter parameter, AudioFeatureSet featureSet) throws DiarizationException, IOException {
		featureSet.write(parameter.show, parameter.getParameterOutputFeature().getFeatureMask(), parameter.getParameterOutputFeature().getFeaturesDescription());
	}

	/**
	 * Write a featureSet.
	 * 
	 * @param parameter the parameter
	 * @param featureSet the featureSet
	 * 
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void writeFeatureSetAs(String show, Parameter parameter, AudioFeatureSet featureSet) throws DiarizationException, IOException {
		featureSet.write(show, parameter.getParameterOutputFeature().getFeatureMask(), parameter.getParameterOutputFeature().getFeaturesDescription());
	}

	/**
	 * Write a featureSet.
	 * 
	 * @param showName the show name
	 * @param parameter the parameter
	 * @param featureSet the f
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void writeFeatureSet(String showName, Parameter parameter, AudioFeatureSet featureSet) throws DiarizationException, IOException {
		featureSet.write(showName, parameter.getParameterOutputFeature().getFeatureMask(), parameter.getParameterOutputFeature().getFeaturesDescription());
	}

	/**
	 * Write a gmm container.
	 * 
	 * @param parameter the parameter
	 * @param gmmList the GMM vector
	 * 
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void writeGMMContainer(Parameter parameter, GMMArrayList gmmList) throws DiarizationException, IOException {
		String modelOutputFilename = IOFile.getFilename(parameter.getParameterModelSetOutputFile().getMask(), parameter.show);
		IOFile fo = new IOFile(modelOutputFilename, "wb");
		fo.open();
		ModelIO.writerGMMContainer(fo, gmmList);
		fo.close();
	}

	/**
	 * The Class comparClusterSet.
	 */
	protected class comparClusterSet implements Comparator<ClusterSet> {

		/*
		 * (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(ClusterSet o1, ClusterSet o2) {
			int l1 = o1.getLength();
			int l2 = o2.getLength();

			if (l1 == l2) {
				return 0;
			} else {
				if (l1 > l2) {
					return -1;
				}
			}
			return 1;
		}
	}

	/**
	 * Split hypotesis.
	 * 
	 * @param fullClusterSet the full cluster set
	 * @return the array list
	 */
	public static ArrayList<ClusterSet> splitHypotesis(ClusterSet fullClusterSet) {
		ArrayList<ClusterSet> listOfClusterSet = new ArrayList<ClusterSet>();
		for (String showName : fullClusterSet.getShowNames()) {
			logger.finer("showName=" + showName);
			ClusterSet clusterSet = new ClusterSet();
			for (Segment segment : fullClusterSet.getSegments()) {
				if (segment.getShowName().equals(showName) == true) {
					Cluster cluster = clusterSet.getOrCreateANewCluster(segment.getClusterName());
					cluster.addSegment(segment);
				}
			}
			listOfClusterSet.add(clusterSet);
		}
		// Collections.sort(listOfClusterSet, new comparClusterSet());
		return listOfClusterSet;
	}

	/** The max memory used. */
	private static float maxMemoryUsed = 0.0f;

	/** The num memory stats. */
	private static int numMemoryStats = 0;

	/** The avg memory used. */
	private static float avgMemoryUsed = 0.0f;

	/** The mem format. */
	private static DecimalFormat memFormat = new DecimalFormat("0.00 Mb");

	/**
	 * Calculate memory usage.
	 * 
	 * @param show the show
	 * @param memoryOccupationRate the memory occupation rate
	 * @return the float
	 */
	public static float calculateMemoryUsage(boolean show, double memoryOccupationRate) {
		//// // max memory xmx: ManagementFactory.getRuntimeMXBean().getInputArguments()
		float totalMem = Runtime.getRuntime().totalMemory() / (1024.0f * 1024.0f);
		float freeMem = Runtime.getRuntime().freeMemory() / (1024.0f * 1024.0f);
		float usedMem = totalMem - freeMem;
		if (usedMem > maxMemoryUsed) {
			maxMemoryUsed = usedMem;
		}
		//// TODO: deleteme ////if (SpkDiarizationLogger.DEBUG) logger.info(ManagementFactory.getRuntimeMXBean().getInputArguments().toString());
		numMemoryStats++;
		avgMemoryUsed = ((avgMemoryUsed * (numMemoryStats - 1)) + usedMem) / numMemoryStats;
		float rate = usedMem / totalMem;
		if ((SpkDiarizationLogger.DEBUG) && (show)) {
			logger.info("   Mem  Total: " + memFormat.format(totalMem) + "  " + "Free: " + memFormat.format(freeMem));
			logger.info("   Rate      : " + (rate * 100) + " memoryOccupationRate: " + memoryOccupationRate);
			logger.info("   Used: This: " + memFormat.format(usedMem) + "  " + "Avg: "
					+ memFormat.format(avgMemoryUsed) + "  " + "Max: " + memFormat.format(maxMemoryUsed));
		}

		return rate;
	}

}
