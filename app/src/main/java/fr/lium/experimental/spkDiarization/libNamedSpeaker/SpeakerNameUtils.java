package fr.lium.experimental.spkDiarization.libNamedSpeaker;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import fr.lium.experimental.spkDiarization.libClusteringData.transcription.EntitySet;
import fr.lium.experimental.spkDiarization.libClusteringData.transcription.Link;
import fr.lium.experimental.spkDiarization.libClusteringData.transcription.LinkSet;
import fr.lium.spkDiarization.parameter.Parameter;

/**
 * The Class SpeakerNameUtils.
 */
public class SpeakerNameUtils {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(SpeakerNameUtils.class.getName());

	/** List of removable articles. */
// protected static final String[] Articles = { "au", "de", "ce", "est", "a", "en", "à", "par", "et", "l'", "le", "la", "les", "un", "une", "uns" };
/*
 * protected static final String[] Articles = { "à", "actuel", "actuels", "actuellement", "afin", "ah", "aïe", "ainsi", "ailleurs", "alors", "a", "est", "ont", "as", "es", "y", "sont", "était", "allô", "allo", "après", "assez", "au", "aucune",
 * "aucunement", "aucun", "audit", "aujourd", "auparavant", "auprès", "auquel", "aussi", "aussitôt", "autant", "autour", "autre", "autrement", "autres", "autrefois", "autrui", "auxdits", "auxdites", "aux", "auxquels", "auxquelles", "avec", "avant",
 * "bah", "bang", "beaucoup", "bien", "bientôt", "bis", "bof", "bon", "bons", "bonnes", "bonne", "boum", "bravo", "bref", "brr", "c'", "car", "ca", "ce", "ceci", "celle", "cela", "celles", "celui", "cependant", "certain", "certaine", "certainement",
 * "certaines", "certains", "certes", "ces", "cet", "cette", "cettes", "ceux", "chez", "chut", "ci", "clic", "clac", "cocorico", "combien", "comme", "comment", "contre", "contrairement", "coucou", "crac", "cric", "ça", "çà", "chaque", "chacun",
 * "chacune", "chapitre", "chez", "d'", "dans", "de", "debout", "dedans", "dehors", "déjà", "del", "demain", "depuis", "dernier", "derniers", "dernière", "dernières", "dernièrement", "derrière", "des", "dès", "desdits", "desdites", "désormais",
 * "desquels", "desquelles", "dessous", "dessus", "devant", "diantre", "différents", "différent", "divers", "donc", "dont", "dorénavant", "du", "dudit", "durant", "duquel", "eh", "effectivement", "également", "elle", "elles", "en", "encore", "enfin",
 * "ensemble", "ensuite", "entre", "envers", "environ", "et", "etc", "etcétera", "euh", "eux", "évidemment", "exprès", "façon", "façons", "facilement", "finalement", "fois", "grand", "grands", "grande", "grandes", "généralement", "grâce", "guère",
 * "halte", "hein", "hélas", "hep", "hier", "hormis", "hors", "ho", "hop", "hourrah", "hui", "hue", "hum", "ici", "infiniment", "incessamment", "incessant", "incessante", "il", "ils", "issu", "j'", "jadis", "jamais", "je", "jusqu", "jusque",
 * "justement", "l'", "la", "là", "ladite", "laquelle", "le", "ledit", "les", "lesdits", "lesdites", "lequel", "lesquels", "lesquelles", "leur", "leurs", "loin", "longtemps", "lors", "lorsqu", "lorsque", "lui", "m'", "ma", "maintenant", "maint",
 * "mainte", "maintes", "mais", "mal", "malheureusement", "malgré", "me", "meilleure", "meilleures", "meilleur", "meilleurs", "même", "mêmes", "mes", "miaou", "mien", "miens", "mienne", "miennes", "mieux", "mlle", "mme", "moi", "moindre", "moins",
 * "mon", "moyennant", "n'", "naguère", "ne", "néanmoins", "ni", "non", "nonobstant", "nos", "notre", "notres", "nôtre", "nôtres", "nous", "nul", "nulle", "nullement", "ok", "on", "ou", "ouais", "oui", "or", "outre", "ouf", "paf", "par", "parbleu",
 * "parce", "parfaitement", "parfois", "parmi", "particulièrement", "partout", "pas", "patatras", "pendant", "petit", "petits", "petite", "petites", "peu", "pif", "pis", "pire", "plouf", "plupart", "plus", "plusieurs", "plutôt", "pouah", "pour",
 * "pourtant", "pourqui", "pourquoi", "practiquement", "près", "presque", "presqu", "priori", "probablement", "pst", "puis", "puisqu", "puisque", "qu'", "quand", "quant", "que", "quel", "quelconque", "quelle", "quelles", "quelqu", "quelque",
 * "quelques", "quelquefois", "quels", "qui", "quiconque", "quoi", "quoique", "quoiqu'", "rien", "relativement", "respectivement", "s'", "si", "sa", "sauf", "se", "sans", "selon", "ses", "seul", "seule", "seulement", "sien", "siens", "sienne",
 * "siennes", "simple", "simplement", "sinon", "sitôt", "soi", "son", "soudain", "soudainement", "sous", "souvent", "stop", "subit", "subite", "suivant", "suivante", "sur", "sûr", "sûre", "sûrement", "sûres", "sûrs", "surtout", "susdit", "susdite",
 * "susdits", "susdites", "suite", "t'", "ta", "tandis", "tantôt", "tant", "tard", "tas", "te", "tel", "telle", "telles", "tellement", "tels", "tes", "tic", "tac", "tien", "tiens", "tienne", "tiennes", "toi", "ton", "tôt", "totalement", "toujours",
 * "tous", "tout", "toute", "toutefois", "toutes", "travers", "très", "trop", "tu", "un", "une", "uniquement", "unes", "uns", "vers", "via", "voici", "voilà", "voire", "votre", "votres", "vos", "vôtre", "vôtres", "vous", "vraiment",
 * "vraisemblablement", "youppie", "zut" };
 */
	protected static final String[] Articles = { "l'", "le", "la", "les", "un", "une", "uns", };
	/**
	 * List of station, the EN org.station is not detected by EN detector. It is done in the program
	 */
	protected static final String[] EntityRadio = { "[fF]rance\\s+[Ii]nter", "[fF]rance\\s+[Ii]nfo",
			"[Rr]adio\\s+[Cc]lassique", "[fF]rance\\s+[Cc]ulture", "[Rr][Ff][iI]", "[Rr][Tt][mM]",
			"[Bb][fF][Mm][Tt][vV]", "[Ll][Cc][Pp]", "bfm\\s+story", "pile\\s+et\\s+face", "top\\s+questions",
			"top\\s+question", "entre\\s+les\\s+lignes", "ca\\s+vous\\s+regarde", "sa\\s+vous\\s+regarde",
			"planete\\s+showbiz", "bfm\\s+tv" };

	/** The Constant EntityHour. */
	protected static final String[] EntityHour = { "[Hh]eure", "[Hh]eures" };

	/** The Constant EntityDate. */
	protected static final String[] EntityDate = { "[sS]amedi", "[jJ]eudi", "[Mm]ardi", "[Dd]imanche", "[Vv]endredi",
			"[Mm]ercredi", "[Ll]undi", "[jJ]anvier", "[fF]évrier", "[mM]ars", "[aA]vril", "[mM]ai", "[jJ]uin",
			"[jJ]uillet", "[aA]oût", "[sS]eptembre", "[oO]ctobre", "[nN]ovembre", "[Dd]écembre" };

	/** The Constant CURRENT. */
	public static final String CURRENT = "cur";

	/** The Constant PREVIOUS. */
	public static final String PREVIOUS = "prev";

	/** The Constant NEXT. */
	public static final String NEXT = "next";

	/** The Constant OTHER. */
	public static final String OTHER = "other";

	/** The Constant INSHOW. */
	public static final String INSHOW = "inshow";

	/** The nb of label. */
	private static int nbOfLabel = 4;

	/** The Constant entityList. */
	public static final String[] entityList = {
			EntitySet.TypePersonne,
			EntitySet.TypeTargetSpeaker
			// ,EntitySet.TypeEster2Organization
			// ,EntitySet.TypeEster2OrganizationStation
			, EntitySet.TypeEster2Localization, EntitySet.TypeEster2Localization2, EntitySet.TypeEtapeLocalizationTown,
			EntitySet.TypeEtapeLocalizationReg
			// ,EntitySet.TypeEster2Time
			// ,EntitySet.TypeEster2TimeHours
			// ,EntitySet.TypeEster2TimeDate
			// ,EntitySet.TypeEster2Amount
			, EntitySet.TypeEster2Fonction
			// ,EntitySet.TypeUnknown
			, EntitySet.TypeEster2Production, EntitySet.TypeEtapeProductionMedia };

	/**
	 * Copy and filter the linkSet of the segment.
	 * 
	 * @param linkSet the link set
	 * @param startTurn the segment is the first segment of the turn, add <s> link at the begin of the linkSet
	 * @param endTurn the segment is the last segment of the turn, add </s> link at the end of the linkSet
	 * @return a linkSet
	 * @throws CloneNotSupportedException the clone not supported exception
	 */
	public static LinkSet makeLinkSetForSCT(LinkSet linkSet, boolean startTurn, boolean endTurn) throws CloneNotSupportedException {
		mergeEntity(linkSet);
		adaptEntity(linkSet);
		removeUnuseEntity(linkSet);
		removeArticle(linkSet);
		if (startTurn == true) {
			linkSet.add(0, new Link(-1, -1, 0, Link.fillerType, 1.0, "<s>"));
		}
		if (endTurn == true) {
			Link link = linkSet.getLink(linkSet.size() - 1);
			linkSet.add(new Link(link.getId() + 1, link.getEnd(), link.getEnd() + 1, Link.fillerType, 1.0, "</s>"));
		}

		return linkSet;
	}

	/**
	 * Merge links of the same entity in the same link. Words are merged.
	 * 
	 * @param linkSet the link set
	 */
	public static void mergeEntity(LinkSet linkSet) {
		for (int i = 0; i < (linkSet.size() - 1);) {
			Link next = linkSet.getLink(i + 1);
			Link current = linkSet.getLink(i);
			if ((current.haveEntity() == true) && (next.haveEntity() == true)) {
				if (current.getEntity() == next.getEntity()) {
					current.merge(next);
					linkSet.remove(i + 1);
				} else {
					i++;
				}
			} else {
				i++;
			}
		}
	}

	// il y a un effet de bord...
	// il faut les retirer avant le merge
	// c'est le cas dans XMLInputOutput
	/**
	 * Removes the unuse entity.
	 * 
	 * @param linkSet the link set
	 */
	public static void removeUnuseEntity(LinkSet linkSet) {
		for (Link link : linkSet) {
			if (link.haveEntity()) {
				if (link.haveEntity(EntitySet.TypeEster2Time)) {
					link.setEntity(null);
				}
				if (link.haveEntity(EntitySet.TypeEster2TimeDate)) {
					link.setEntity(null);
				}
			}
		}
	}

	/**
	 * detect entity org.station
	 * 
	 * @param linkSet the link set
	 */
	public static void adaptEntity(LinkSet linkSet) {
		for (Link link : linkSet) {
			if (link.haveEntity(EntitySet.TypeEster2Organization) == true) {
				if (isEntityRadio(link.getWord()) == true) {
					link.getEntity().setType(EntitySet.TypeEster2OrganizationStation);
				}
			}
			if (link.haveEntity(EntitySet.TypeEster2Time) == true) {
				if (isEntityHour(link.getWord()) == true) {
					link.getEntity().setType(EntitySet.TypeEster2TimeHours);
				}
				if (isEntityDate(link.getWord()) == true) {
					link.getEntity().setType(EntitySet.TypeEster2TimeDate);
				}
			}
		}
	}

	/**
	 * Check if the entity org is a station.
	 * 
	 * @param word the word
	 * @return true if the word is a station
	 */
	public static boolean isEntityRadio(String word) {
		for (String element : SpeakerNameUtils.EntityRadio) {
			if (word.matches(element)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if the entity time is a hour.
	 * 
	 * @param word the word
	 * @return true if the word is a station
	 */
	public static boolean isEntityHour(String word) {
		for (String element : SpeakerNameUtils.EntityHour) {
			if (word.matches(element)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if the entity time is a date.
	 * 
	 * @param word the word
	 * @return true if the word is a station
	 */
	public static boolean isEntityDate(String word) {
		for (String element : SpeakerNameUtils.EntityDate) {
			if (word.matches(element)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Test if the word is equal to an article.
	 * 
	 * @param word the word
	 * @return if the word is an article
	 */
	public static boolean isARemovableArticle(String word) {
		for (String article : SpeakerNameUtils.Articles) {
			if (word.equals(article)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * remove all articles of the linkSet.
	 * 
	 * @param linkSet the link set
	 */
	public static void removeArticle(LinkSet linkSet) {
		for (int i = 0; i < (linkSet.size() - 1);) {
			Link current = linkSet.getLink(i);
			if (isARemovableArticle(current.getWord()) == true) {
				linkSet.remove(i);
			} else {
				i++;
			}
		}
	}

	/**
	 * Check if the candidate speaker name is a target of the application.
	 * 
	 * @param speakerName the candidate speaker name
	 * @param isCloseListCheck the is close list check
	 * @param nameAndGenderMap the name and gender map
	 * @param firstNameAndGenderMap the first name and gender map
	 * @return true, if successful
	 * 
	 *         TODO remove partial test non name, and clean before entity
	 */
	public static boolean checkSpeakerName(String speakerName, boolean isCloseListCheck, TargetNameMap nameAndGenderMap, TargetNameMap firstNameAndGenderMap) {
		String normalizedSpeakerName = normalizeSpeakerName(speakerName);
		if (isCloseListCheck == true) {
			if (nameAndGenderMap.containsKey(normalizedSpeakerName) == false) {
				/*
				 * for (String name : nameAndGenderMap.keySet()) { if (normalizedSpeakerName.equals(name)) { logger.warning("CHECK add name in list: "+speakerName); nameAndGenderMap.put(normalizedSpeakerName, nameAndGenderMap.get(name)); return true;
				 * } /* if (normalizedSpeakerName.startsWith(name)) { nameAndGenderMap.put(normalizedSpeakerName, nameAndGenderMap.get(name)); return true; } /*if(normalizedSpeakerName.endsWith(name)) { nameAndGenderMap.put(normalizedSpeakerName,
				 * nameAndGenderMap.get(name)); return true; } }
				 */
				return false;
			}
		} else {
			String[] splitName = normalizedSpeakerName.split("_");
			if (splitName.length <= 1) {
				return false;
			}
			// logger.info(normalizedSpeakerName+" // "+splitName.toString());
			if (firstNameAndGenderMap.containsKey(splitName[0]) == false) {
				logger.warning("CHECK fristname reject : " + speakerName);
				return false;
			}
		}
		return true;
	}

	/* First name only accepted */
	/**
	 * Check speaker name plus first name.
	 * 
	 * @param speakerName the speaker name
	 * @param isCloseListCheck the is close list check
	 * @param nameAndGenderMap the name and gender map
	 * @param firstNameAndGenderMap the first name and gender map
	 * @return the int
	 */
	public static int checkSpeakerNamePlusFirstName(String speakerName, boolean isCloseListCheck, TargetNameMap nameAndGenderMap, TargetNameMap firstNameAndGenderMap) {
		String normalizedSpeakerName = normalizeSpeakerName(speakerName);
		if (isCloseListCheck == true) {
			if (nameAndGenderMap.containsKey(normalizedSpeakerName) == false) {
				/*
				 * for (String name : nameAndGenderMap.keySet()) { if (normalizedSpeakerName.equals(name)) { logger.warning("CHECK add name in list: "+speakerName); nameAndGenderMap.put(normalizedSpeakerName, nameAndGenderMap.get(name)); return true;
				 * } /* if (normalizedSpeakerName.startsWith(name)) { nameAndGenderMap.put(normalizedSpeakerName, nameAndGenderMap.get(name)); return true; } /*if(normalizedSpeakerName.endsWith(name)) { nameAndGenderMap.put(normalizedSpeakerName,
				 * nameAndGenderMap.get(name)); return true; } }
				 */
				return -1;
			}
		} else {
			String[] splitName = normalizedSpeakerName.split("_");
			if (firstNameAndGenderMap.containsKey(splitName[0]) == false) {
				logger.warning("CHECK fristname reject : " + speakerName);
				return -1;
			}
			if (splitName.length <= 1) {
				return 2;
			}
		}
		return 1;
	}

	/**
	 * Check speaker name.
	 * 
	 * @param speakerName the speaker name
	 * @param isCloseListCheck the is close list check
	 * @param nameAndGenderMap the name and gender map
	 * @return true, if successful
	 */
	public static boolean checkSpeakerName(String speakerName, boolean isCloseListCheck, TargetNameMap nameAndGenderMap) {
		String normalizedSpeakerName = normalizeSpeakerName(speakerName);
		if (isCloseListCheck == true) {
			if (nameAndGenderMap.containsKey(normalizedSpeakerName) == false) {
				/*
				 * for (String name : nameAndGenderMap.keySet()) { if (normalizedSpeakerName.equals(name)) { logger.warning("CHECK add name in list: "+speakerName); nameAndGenderMap.put(normalizedSpeakerName, nameAndGenderMap.get(name)); return true;
				 * } /* if (normalizedSpeakerName.startsWith(name)) { nameAndGenderMap.put(normalizedSpeakerName, nameAndGenderMap.get(name)); return true; } /*if(normalizedSpeakerName.endsWith(name)) { nameAndGenderMap.put(normalizedSpeakerName,
				 * nameAndGenderMap.get(name)); return true; } }
				 */
				return false;
			}
		}
		return true;
	}

	/**
	 * Normalize the speaker name by removing accent and put the string in lower case.
	 * 
	 * @param name the string
	 * 
	 * @return the normalized string
	 */
	public static String normalizeSpeakerName(String name) {
		String ch = name.toLowerCase();
		ch = ch.replace('é', 'e');
		ch = ch.replace('è', 'e');
		ch = ch.replace('ê', 'e');
		ch = ch.replace('ë', 'e');
		ch = ch.replace('ô', 'o');
		ch = ch.replace('ö', 'o');
		ch = ch.replace('ï', 'i');
		ch = ch.replace('î', 'i');
		ch = ch.replace('â', 'a');
		ch = ch.replace('à', 'a');
		ch = ch.replace('ä', 'a');
		ch = ch.replace('ü', 'u');
		ch = ch.replace('û', 'u');
		ch = ch.replace('ç', 'c');
		ch = ch.replace(' ', '_');
		ch = ch.replace('-', '_');

		return ch;
	}

	/**
	 * Add the information need for SCT global Information. Global information is questioned by the SCT, information could be : END_TURN, START_TURN, SHORT_TURN and MID_TURN.
	 * 
	 * @param linkSet add information in linkSet
	 * @param delay number of word on the right and on the left
	 * @param index index the position of the target speaker name in the linkSet
	 * @param sizeTurn number of word in the turn
	 * 
	 *            TODO: problem a turn could be STAR_TURN and END_TURN, ie a short turn ?
	 * @param startTurn the start turn
	 * @param endTurn the end turn
	 */
	public static void addGlobalInformation(LinkSet linkSet, int delay, int index, int sizeTurn, boolean startTurn, boolean endTurn) {
		int lenStart = 2 * delay;
		int lenEnd = sizeTurn - (2 * delay);

		if ((sizeTurn <= lenStart) && (startTurn == true) && (endTurn == true)) {
			linkSet.setInformation("SHORT_TURN", "true");
		} else if ((index <= lenStart) && (startTurn == true)) {
			linkSet.setInformation("START_TURN", "true");
		} else if ((index >= lenEnd) && (endTurn == true)) {
			linkSet.setInformation("END_TURN", "true");
		} else {
			linkSet.setInformation("MID_TURN", "true");
		}
	}

	/**
	 * The SCT works over a linkSet of maximum delay*2+1 size centered over a entity of type "pers". The method takes the linkSet of a segment and build a new linkSet of size delay*2+1 with delay link on the left and delay link on the right (if
	 * available). Global information is added at the end.
	 * 
	 * TODO: call of addGlobalInformation(result, delay, index, linkSet.size()), linkSet.size() corresponds of the number of the word only if the clusterSet is collapsed
	 * 
	 * @param linkSet corresponding of the full sentence
	 * @param index the position of the target speaker name in the linkSet
	 * @param delay number of word on the right and on the left
	 * @param startTurn the start turn
	 * @param endTurn the end turn
	 * @param addGlabalQuestion the add glabal question
	 * @return a reduce LinkSet with the target speaker name as pivot
	 * @throws CloneNotSupportedException the clone not supported exception
	 */
	public static LinkSet reduceLinkSetForSCT(LinkSet linkSet, Integer index, int delay, boolean startTurn, boolean endTurn, boolean addGlabalQuestion) throws CloneNotSupportedException {
		int start = Math.max(index - delay, 0);
		int end = Math.min(index + delay, linkSet.size() - 1);
		// linkSet.debug();

		LinkSet result = new LinkSet(linkSet.getId());
		for (int i = start; i <= end; i++) {
			Link link = (Link) linkSet.getLink(i).cloneWithEntity();
			result.add(link);
			if (i == index) {
				result.getLink(result.size() - 1).getEntity().setType(EntitySet.TypeTargetSpeaker);
			}
		}
		if (addGlabalQuestion == true) {
			SpeakerNameUtils.addGlobalInformation(result, delay, index, linkSet.size(), startTurn, endTurn);
		}
		return result;
	}

	/**
	 * Load a list of target speaker name and gender.
	 * 
	 * @param clientFile the file
	 * 
	 * @return the target speaker name and gender map
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static TargetNameMap loadList(String clientFile) throws IOException {
		String line;
		TargetNameMap targetSpeakerNameMap = new TargetNameMap();
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(clientFile), Parameter.DefaultCharset));

		while ((line = bufferedReader.readLine()) != null) {
			String token[] = line.split("\\s+");
			String ch = SpeakerNameUtils.normalizeSpeakerName(token[0]);
			targetSpeakerNameMap.put(ch, token[1]);
		}
		return targetSpeakerNameMap;
	}

	/**
	 * Sets the nb of label.
	 * 
	 * @param nbOfLabel the nbOfLabel to set
	 */
	public static void setNbOfLabel(int nbOfLabel) {
		SpeakerNameUtils.nbOfLabel = nbOfLabel;
	}

	/**
	 * Gets the nb of label.
	 * 
	 * @return the nbOfLabel
	 */
	public static int getNbOfLabel() {
		return nbOfLabel;
	}

}
