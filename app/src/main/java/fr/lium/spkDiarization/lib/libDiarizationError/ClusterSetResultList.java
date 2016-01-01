package fr.lium.spkDiarization.lib.libDiarizationError;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.parameter.Parameter;

/**
 * The Class ClusterSetResultList.
 */
public class ClusterSetResultList {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(DiarizationResultList.class.getName());

	/** The list. */
	private ArrayList<ClusterSet> list;

	/** The precision. */
	private double precision;

	/** The min. */
	private int min;

	/** The max. */
	private int max;

	/**
	 * Instantiates a new cluster set result list.
	 * 
	 * @param cMin the c min
	 * @param cMax the c max
	 * @param precision the precision
	 */
	public ClusterSetResultList(double cMin, double cMax, double precision) {
		super();
		this.precision = precision;
		min = (int) Math.round(cMin * precision);
		max = (int) Math.round(cMax * precision);
		initialize();
	}

	/**
	 * Adds the result array.
	 * 
	 * @param clusterSetResultList the cluster set result list
	 * @throws DiarizationException the diarization exception
	 */
	public void addResultArray(ClusterSetResultList clusterSetResultList) throws DiarizationException {
		if (list.size() != clusterSetResultList.list.size()) {
			throw new ArrayStoreException("size problem");
		}
		for (int i = 0; i < list.size(); i++) {
			list.set(i, clusterSetResultList.list.get(i));
		}
	}

	/**
	 * Score2int.
	 * 
	 * @param score the score
	 * @return the int
	 */
	private int score2int(double score) {
		if (score == Double.MAX_VALUE) {
			return Integer.MAX_VALUE;
		}
		long result = Math.round(score * precision);
		if (result > Integer.MAX_VALUE) {
			return Integer.MAX_VALUE;
		}
		if (result < Integer.MIN_VALUE) {
			return Integer.MIN_VALUE;
		}
		return (int) result;
	}

	/**
	 * Sets the result.
	 * 
	 * @param previousScore the previous score
	 * @param score the score
	 * @param clusterSet the cluster set
	 * @throws DiarizationException the diarization exception
	 */
	public void setResult(double previousScore, double score, ClusterSet clusterSet) throws DiarizationException {
		long endScore = score2int(score);
		long startScore = score2int(previousScore);
		int end = (int) Math.min(max, endScore);
		int start = (int) Math.max(min, startScore);
		ClusterSet res = clusterSet.clone();
		logger.finer("put score previous=" + previousScore + " score=" + score + "(" + endScore + ") start/minStart="
				+ start + " / " + min + " / " + Math.max(min, startScore) + " end/maxEnd=" + end + " / " + max
				+ " array length=" + list.size());
		for (int i = start; i <= end; i++) {
			list.set(i - min, res);
		}
	}

	/**
	 * Write clr cluster set.
	 * 
	 * @param parameter the parameter
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ParserConfigurationException the parser configuration exception
	 * @throws SAXException the sAX exception
	 * @throws DiarizationException the diarization exception
	 * @throws TransformerException the transformer exception
	 */
	public void writeCLRClusterSet(Parameter parameter) throws IOException, ParserConfigurationException, SAXException, DiarizationException, TransformerException {

		String mask = parameter.getParameterSegmentationOutputFile().getMask();
		String mask2 = mask.replace(".seg", "");

		for (int i = 0; i < list.size(); i++) {
			parameter.getParameterSegmentationOutputFile().setMask(mask2 + "."
					+ String.format("%3d", i + min).replace(" ", "_") + ".c.seg");
			MainTools.writeClusterSet(parameter, list.get(i), false);
			parameter.getParameterSegmentationOutputFile().setMask(mask);
		}
	}

	/**
	 * Initialize.
	 */
	protected void initialize() {
		list = new ArrayList<ClusterSet>();
		list.ensureCapacity((max - min) + 1);
		for (int i = min; i <= max; i++) {
			list.add(i - min, new ClusterSet());
		}
	}

}
