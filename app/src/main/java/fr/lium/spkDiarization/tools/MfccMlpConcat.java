package fr.lium.spkDiarization.tools;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.lib.SpkDiarizationLogger;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.libFeature.AudioFeatureSet;
import fr.lium.spkDiarization.parameter.Parameter;

/**
 * The Class to merge 2 feature. Use to make test with mfcc and mlp features.
 */
public class MfccMlpConcat {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(MfccMlpConcat.class.getName());

	/**
	 * The main method.
	 * 
	 * @param args the arguments
	 * @throws Exception the exception
	 */
	public static void main(String[] args) throws Exception {
		try {
			Parameter parameter = MainTools.getParameters(args);
			info(parameter, "MfccMlpConcat");
			if (parameter.show.isEmpty() == false) {
				// clusters
				ClusterSet clusterSet = null;
				if (parameter.getParameterSegmentationInputFile().getMask().equals("")) {
					clusterSet = new ClusterSet();
					Cluster cluster = clusterSet.createANewCluster("init");
					Segment segment = new Segment(parameter.show, 0, Integer.MAX_VALUE, cluster, parameter.getParameterSegmentationInputFile().getRate());
					cluster.addSegment(segment);

				} else {
					// clusters
					clusterSet = MainTools.readClusterSet(parameter);
					clusterSet.collapse();
				}
				AudioFeatureSet featureSet = MainTools.readFeatureSet(parameter, clusterSet);
				featureSet.setChangePositionOfEnergy(false);
				AudioFeatureSet featureSet2 = MainTools.readTheSecondFeatureSet(parameter, clusterSet);

				for (String showName : clusterSet.getShowNames()) {
					// Features
					featureSet.setCurrentShow(showName);
					featureSet2.setCurrentShow(showName);
					logger.info("input1 num features = " + featureSet.getNumberOfFeatures());
					logger.info("input2 num features = " + featureSet2.getNumberOfFeatures());
					if (Math.abs(featureSet.getNumberOfFeatures() - featureSet2.getNumberOfFeatures()) > 10) {
						if (SpkDiarizationLogger.DEBUG) featureSet.debug(2);
						if (SpkDiarizationLogger.DEBUG) featureSet2.debug(2);

						throw new DiarizationException("MfccMlpConcat difference in number of features larger than 10: num1="
								+ featureSet.getNumberOfFeatures() + " num2=" + featureSet2.getNumberOfFeatures());
					}
					int nbFeatures = Math.min(featureSet.getNumberOfFeatures(), featureSet2.getNumberOfFeatures());
					AudioFeatureSet featureSetResult = new AudioFeatureSet(nbFeatures, parameter.getParameterOutputFeature().getFeaturesDescription());

					for (int i = 0; i < nbFeatures; i++) {
						float[] mfcc = featureSet.getFeatureUnsafe(i);

						float[] mlp = featureSet2.getFeatureUnsafe(i);
						float[] mfccMlp = new float[mfcc.length + mlp.length];

						System.arraycopy(mfcc, 0, mfccMlp, 0, mfcc.length);
						System.arraycopy(mlp, 0, mfccMlp, mfcc.length, mlp.length);
						logger.finest("mfcc[0]=" + mfcc[0] + " mlp[0]=" + mlp[0] + " mfccMlp[0]=" + mfccMlp[0]
								+ " mfccMlp[mfcc.length]=" + mfccMlp[mfcc.length]);
						featureSetResult.addFeature(mfccMlp);
					}
					MainTools.writeFeatureSet(showName, parameter, featureSetResult);
					logger.info("output num features = " + featureSetResult.getNumberOfFeatures());
				}
			} else {
				logger.severe("no show found in segments file");
			}
		} catch (Exception e) {
			System.err.println("error \t exception " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Info.
	 * 
	 * @param parameter the parameter
	 * @param progam the progam
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws InvocationTargetException the invocation target exception
	 */
	public static void info(Parameter parameter, String progam) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (parameter.help) {
			parameter.getSeparator2();
			System.out.println("info[program] \t name = " + progam);
			parameter.getSeparator();
			parameter.logShow();

			parameter.getParameterInputFeature().logAll();
			parameter.getParameterInputFeature2().logAll();
			parameter.getParameterOutputFeature().logAll();
			parameter.getSeparator();
			parameter.getParameterSegmentationInputFile().logAll(); // sInMask

		}
	}

}
