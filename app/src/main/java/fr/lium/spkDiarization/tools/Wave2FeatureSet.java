package fr.lium.spkDiarization.tools;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
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
 * The Class Wave2FeatureSet.
 */
public class Wave2FeatureSet {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(ConcatFeatureSet.class.getName());

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
			info(parameter, "Wave2FeatureSet");
			if (parameter.show.isEmpty() == false) {
				// clusters
				Segment segment = null;
				ClusterSet clusterSet = null;
				if (parameter.getParameterSegmentationInputFile().getMask().equals("")) {
					clusterSet = new ClusterSet();
					Cluster cluster = clusterSet.createANewCluster("init");
					segment = new Segment(parameter.show, 0, 0, cluster, parameter.getParameterSegmentationInputFile().getRate());
					cluster.addSegment(segment);

				} else {
					// clusters
					clusterSet = MainTools.readClusterSet(parameter);
					// clusterSet.collapse();
				}

				for (String show : clusterSet.getShowNames()) {
					AudioFeatureSet featureSet = MainTools.readFeatureSet(parameter, clusterSet);
					featureSet.setCurrentShow(show);
					if (segment != null) {
						segment.setLength(featureSet.getNumberOfFeatures());
					}
					featureSet.getFeature(show, 0);
					MainTools.writeFeatureSetAs(show, parameter, featureSet);
				}
				MainTools.writeClusterSet(parameter, clusterSet);
			}
		} catch (DiarizationException e) {
			logger.log(Level.SEVERE, "", e);
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
			logger.config(parameter.getSeparator2());
			logger.config("Program name = " + progam);
			logger.config(parameter.getSeparator());
			parameter.logShow();

			parameter.getParameterInputFeature().logAll();
			parameter.getParameterOutputFeature().logAll();
			logger.config(parameter.getSeparator());
			parameter.getParameterSegmentationInputFile().logAll(); // sInMask

		}
	}
}
