package fr.lium.experimental.spkDiarization.programs;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.lium.experimental.spkDiarization.libClusteringData.transcription.EntitySet;
import fr.lium.experimental.spkDiarization.libClusteringData.transcription.Link;
import fr.lium.experimental.spkDiarization.libClusteringData.transcription.LinkSet;
import fr.lium.experimental.spkDiarization.libClusteringData.turnRepresentation.Turn;
import fr.lium.experimental.spkDiarization.libClusteringData.turnRepresentation.TurnSet;
import fr.lium.experimental.spkDiarization.libNamedSpeaker.SpeakerNameUtils;
import fr.lium.experimental.spkDiarization.libNamedSpeaker.TargetNameMap;
import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.lib.SpkDiarizationLogger;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libModel.Distance;
import fr.lium.spkDiarization.parameter.Parameter;

/**
 * The Class SpeakerIdentificationCorpus.
 */
public class SpeakerIdentificationCorpus {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(SpeakerIdentificationCorpus.class.getName());

	/** The parameter. */
	static Parameter parameter;

	/** The name and gender map. */
	static TargetNameMap nameAndGenderMap;

	/**
	 * Print the available options.
	 * 
	 * @param parameter is all the parameters
	 * @param program name of this program
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws InvocationTargetException the invocation target exception
	 */
	public static void info(Parameter parameter, String program) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (parameter.help) {
			logger.config(parameter.getSeparator2());
			logger.config("Program name = " + program);
			logger.config(parameter.getSeparator());
			parameter.logShow();

			parameter.getParameterSegmentationInputFile().logAll(); // sInMask
			parameter.getParameterSegmentationOutputFile().logAll(); // sOutMask
			logger.config(parameter.getSeparator());
			parameter.getParameterNamedSpeaker().logAll();
		}
	}

	/**
	 * Test the SCT over each segment containing a linkSet and an entity. The result of the SCT (speaker name and probability) are stored in the clusters of the previous, current or next turn.
	 * 
	 * @param clusters the clusters
	 * @param targetSpeakerNameMap the target speaker name map
	 * @param isTraining the is training
	 * @return the array list
	 * @throws CloneNotSupportedException the clone not supported exception
	 * @throws DiarizationException TODO manager open and close speaker list
	 */
	public static ArrayList<String> prepareCorpus(ClusterSet clusters, TargetNameMap targetSpeakerNameMap, boolean isTraining) throws CloneNotSupportedException, DiarizationException {
		TurnSet turns = clusters.getTurns();
		ArrayList<String> list = new ArrayList<String>();
		boolean isCloseListCheck = parameter.getParameterNamedSpeaker().isCloseListCheck();
		for (int i = 0; i < turns.size(); i++) {
			Turn currentTurn = turns.get(i);
			LinkSet linkSet = currentTurn.getCollapsedLinkSet();
			boolean startTurn = true;
			boolean endTurn = true;
			SpeakerNameUtils.makeLinkSetForSCT(linkSet, startTurn, endTurn);
			for (int index = 0; index < linkSet.size(); index++) {
				Link link = linkSet.getLink(index);
				if (link.haveEntity(EntitySet.TypePersonne) == true) {
					LinkSet linkSetForTest = SpeakerNameUtils.reduceLinkSetForSCT(linkSet, index, 10, startTurn, endTurn, false);
					String speakerName = link.getWord();
					if (SpeakerNameUtils.checkSpeakerName(speakerName, isCloseListCheck, nameAndGenderMap) == true) {
						String value = linkSetForTest.SCTTrainInformation(currentTurn.first().getShowName(), link.getEntity().getId(), SpeakerNameUtils.entityList);
						if (isTraining == true) {
							String label = findLabel(turns, i, speakerName);
							// label += " <--> "+SpeakerNameUtils.normalizeSpeakerName(speakerName);
							// list.add(value +" "+ getSpeakers(turns, i, speakerName) +"\n@ " + label);
							list.add(value + "\n@ " + label);
						} else {
							// list.add(value+" "+ getSpeakers(turns, i, speakerName));
							list.add(value);
						}
					}
				}
			}
		}
		return list;
	}

	/**
	 * Gets the speakers.
	 * 
	 * @param turns the turns
	 * @param index the index
	 * @param speakerName the speaker name
	 * @return the speakers
	 */
	public static String getSpeakers(TurnSet turns, int index, String speakerName) {
		String next = "empty";
		if ((index + 1) < turns.size()) {
			next = SpeakerNameUtils.normalizeSpeakerName(turns.get(index + 1).getCluster().getName());
		}
		String current = SpeakerNameUtils.normalizeSpeakerName(turns.get(index).getCluster().getName());

		String previous = "empty";
		if ((index - 1) >= 0) {
			previous = SpeakerNameUtils.normalizeSpeakerName(turns.get(index - 1).getCluster().getName());
		}

		String spk = SpeakerNameUtils.normalizeSpeakerName(speakerName);

		return spk + "=(" + previous + ", " + current + ", " + next + ")";
		// +"\n&"+previous+"/"+current+"/"+next;
	}

	/**
	 * Find label.
	 * 
	 * @param turns the turns
	 * @param index the index
	 * @param speakerName the speaker name
	 * @return the string
	 */
	public static String findLabel(TurnSet turns, int index, String speakerName) {
		String result = SpeakerNameUtils.OTHER + " 1";
		String spk = SpeakerNameUtils.normalizeSpeakerName(speakerName);

		if (SpeakerNameUtils.getNbOfLabel() > 4) {
			for (Turn turn : turns) {
				String name = SpeakerNameUtils.normalizeSpeakerName(turn.getCluster().getName());
				if (spk.equals(name) == true) {
					result = SpeakerNameUtils.INSHOW + " 1";
				}
			}
		}
		String next = "empty";
		if ((index + 1) < turns.size()) {
			next = SpeakerNameUtils.normalizeSpeakerName(turns.get(index + 1).getCluster().getName());
		}
		String current = SpeakerNameUtils.normalizeSpeakerName(turns.get(index).getCluster().getName());

		String previous = "empty";
		if ((index - 1) >= 0) {
			previous = SpeakerNameUtils.normalizeSpeakerName(turns.get(index - 1).getCluster().getName());
		}

		if (spk.equals(current) == true) {
			result = SpeakerNameUtils.CURRENT + " 10";
		} else if (spk.equals(next) == true) {
			result = SpeakerNameUtils.NEXT + " 10";
		} else if (spk.equals(previous) == true) {
			result = SpeakerNameUtils.PREVIOUS + " 10";
		} else {
			String spk2 = spk + "_";
			// logger.info("***find : "+spk+"-->"+spk2+"=("+previous+", "+current+", "+next+")");
			if (current.startsWith(spk2) == true) {
				result = SpeakerNameUtils.CURRENT + " 10";
			} else if (next.startsWith(spk2) == true) {
				result = SpeakerNameUtils.NEXT + " 10";
			} else if (previous.startsWith(spk2) == true) {
				result = SpeakerNameUtils.PREVIOUS + " 10";
			}
		}

		int distance = Distance.levenshteinDistance(previous, spk);
		String ch = previous;
		int tmp = Distance.levenshteinDistance(current, spk);
		if (tmp < distance) {
			distance = tmp;
			ch = current;
		}
		tmp = Distance.levenshteinDistance(next, spk);
		if (tmp < distance) {
			distance = tmp;
			ch = next;
		}
		logger.info("Speaker: " + spk + " ~ [" + previous + ", " + current + ", " + next + " ] " + result + " dist("
				+ distance + " / " + ch + ") " + turns.get(index).get(0).getShowName());

		return result;
		// +"\n&"+previous+"/"+current+"/"+next;
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
			parameter = MainTools.getParameters(args);
			info(parameter, "SpeakerIdentificationCorpus");
			if (parameter.show.isEmpty() == false) {
				ClusterSet clusters = MainTools.readClusterSet(parameter);
				clusters.collapse();
				nameAndGenderMap = null;
				if (parameter.getParameterNamedSpeaker().isCloseListCheck() == true) {
/*
 * if (parameter.getParameterNamedSpeaker().isTraining()) { nameAndGenderMap = new TargetNameMap(); for(String name: clusters){ Cluster cluster = clusters.getCluster(name); name = SpeakerNameUtils.normalizeSpeakerName(name);
 * nameAndGenderMap.put(name, cluster.getGender()); } } else {
 */
					nameAndGenderMap = SpeakerNameUtils.loadList(parameter.getParameterNamedSpeaker().getNameAndGenderList());
					// }
				}
				ArrayList<String> list = prepareCorpus(clusters, nameAndGenderMap, parameter.getParameterNamedSpeaker().isTraining());
				MainTools.writeStringList(parameter, list);
			}
		} catch (DiarizationException e) {
			logger.log(Level.SEVERE, "exception ", e);
			e.printStackTrace();
		}
	}

}
