/**
 * 
 */
package fr.lium.spkDiarization.libModel.gaussian;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.libFeature.AudioFeatureSet;
import fr.lium.spkDiarization.parameter.ParameterTopGaussian;

/**
 * The Class GMMArrayList.
 * 
 * @author meignier
 */
public class GMMArrayList extends ArrayList<GMM> {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(GMMArrayList.class.getName());

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new gMM array list.
	 */
	public GMMArrayList() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Instantiates a new gMM array list.
	 * 
	 * @param arg0 the arg0
	 */
	public GMMArrayList(int arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Instantiates a new gMM array list.
	 * 
	 * @param arg0 the arg0
	 */
	public GMMArrayList(Collection<? extends GMM> arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Accumulate likelihood.
	 * 
	 * @param featureSet the feature set
	 * @param segment the segment
	 * @param gmmTop the gmm top
	 * @param parameterTopGaussian the parameter top gaussian
	 * @param genderCluster the gender cluster
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void accumulateLikelihood(AudioFeatureSet featureSet, Segment segment, GMM gmmTop, ParameterTopGaussian parameterTopGaussian, String genderCluster) throws DiarizationException, IOException {
		// String bandwidthSegment = segment.getBandwidth();

		featureSet.setCurrentShow(segment.getShowName());
		for (int i = 0; i < size(); i++) {
			get(i).score_initialize();
		}
		for (int start = segment.getStart(); start < (segment.getStart() + segment.getLength()); start++) {
			boolean first = true;
			for (int i = 0; i < size(); i++) {
				GMM gmm = get(i);
				// String token[] = gmm.getName().split("-");
				/*
				 * for (int j = 0; j < token.length; j++) { logger.info("token i:"+j+" val:"+token[j]); }
				 */
				// logger.info(token[token.length-2]+"/"+gender+" -- "+token[token.length-1]+"/"+bandwidth);
				// String genderGmm = token[token.length - 2];
				// String bandwidthGMM = token[token.length - 1];
				// if (genderTest == true) {
				// if ((genderTest == true) && (bandwidthTest == true)){
				if (parameterTopGaussian.getScoreNTop() >= 0) {
					if (first == true) {
						gmmTop.score_getAndAccumulateAndFindTopComponents(featureSet, start, parameterTopGaussian.getScoreNTop());
						first = false;
					}
					gmm.score_getAndAccumulateForComponentSubset(featureSet, start, gmmTop.getTopGaussianVector());
				} else {
					gmm.score_getAndAccumulate(featureSet, start);
				}
				// }
			}
		}
	}

	/**
	 * Reset score accumulator.
	 */
	public void resetScoreAccumulator() {
		for (int i = 0; i < size(); i++) {
			get(i).score_reset();
		}
	}

	/**
	 * Debug.
	 * 
	 * @param level the level
	 * @throws DiarizationException the diarization exception
	 */
	public void debug(int level) throws DiarizationException {
		for (int i = 0; i < size(); i++) {
			logger.info("gmm name:" + get(i).getName() + " gender:" + get(i).getGender() + " dim:"
					+ get(i).getDimension() + " token:" + get(i).getName().split("-").toString());
			if (level > 0) {
				get(i).debug(level);
			}
		}
	}

}
