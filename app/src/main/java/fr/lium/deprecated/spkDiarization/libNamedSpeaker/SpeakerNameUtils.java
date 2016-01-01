package fr.lium.deprecated.spkDiarization.libNamedSpeaker;

/**
 * The Class SpeakerNameUtils.
 */
public class SpeakerNameUtils {
// private final static Logger logger = Logger.getLogger(SpeakerNameUtils.class.getName());
// /**
// * List of removable articles
// */
// // protected static final String[] Articles = { "au", "de", "ce", "est", "a", "en", "à", "par", "et", "l'", "le", "la", "les", "un", "une", "uns" };
// protected static final String[] Articles = { "l'", "le", "la", "les", "un", "une", "uns", "de", "au" };
// // protected static final String[] Articles = { "l'", "le", "la", "les", "un", "une", "uns" };
// /**
// * List of station, the EN org.station is not detected by EN detector. It is done in the program
// */
// protected static final String[] EntityRadio = { "[fF]rance\\s+[Ii]nter", "[fF]rance\\s+[Ii]nfo",
// "[Rr]adio\\s+[Cc]lassique", "[fF]rance\\s+[Cc]ulture", "[Rr][Ff][iI]", "[Rr][Tt][mM]", "[Rr][Ff][Ii]" };
// protected static final String[] EntityHour = { "[Hh]eure", "[Hh]eures" };
// protected static final String[] EntityDate = { "[sS]amedi", "[jJ]eudi", "[Mm]ardi", "[Dd]imanche", "[Vv]endredi",
// "[Mm]ercredi", "[Ll]undi", "[jJ]anvier", "[fF]évrier", "[mM]ars", "[aA]vril", "[mM]ai", "[jJ]uin",
// "[jJ]uillet", "[aA]oût", "[sS]eptembre", "[oO]ctobre", "[nN]ovembre", "[Dd]écembre" };
//
// public static final String CURRENT = "cur";
// public static final String PREVIOUS = "prev";
// public static final String NEXT = "next";
// // public static final String INSHOW = "inshow";
// public static final String OTHER = "other";
// // private static int nbOfLabel = 5;
// private static int nbOfLabel = 4;
//
// /**
// * Copy and filter the linkSet of the segment.
// *
// * @param startTurn the segment is the first segment of the turn, add <s> link at the begin of the linkSet
// * @param endTurn the segment is the last segment of the turn, add </s> link at the end of the linkSet
// * @return a linkSet
// * @throws CloneNotSupportedException
// */
// public static LinkSet makeLinkSetForSCT(LinkSet linkSet, boolean startTurn, boolean endTurn) throws CloneNotSupportedException {
// mergeEntity(linkSet);
// adaptEntity(linkSet);
// removeUnuseEntity(linkSet);
// removeArticle(linkSet);
// if (startTurn == true) {
// linkSet.add(0, new Link(-1, -1, 0, Link.fillerType, 1.0, "<s>"));
// }
// if (endTurn == true) {
// Link link = linkSet.getLink(linkSet.size() - 1);
// linkSet.add(new Link(link.getId() + 1, link.getEnd(), link.getEnd() + 1, Link.fillerType, 1.0, "</s>"));
// }
//
// return linkSet;
// }
//
// /**
// * Merge links of the same entity in the same link. Words are merged.
// *
// * @param linkSet
// */
// public static void mergeEntity(LinkSet linkSet) {
// for (int i = 0; i < (linkSet.size() - 1);) {
// Link next = linkSet.getLink(i + 1);
// Link current = linkSet.getLink(i);
// if ((current.haveEntity() == true) && (next.haveEntity() == true)) {
// if (current.getEntity() == next.getEntity()) {
// current.merge(next);
// linkSet.remove(i + 1);
// } else {
// i++;
// }
// } else {
// i++;
// }
// }
// }
//
// // il y a un effet de bord...
// // il faut les retirer avant le merge
// // c'est le cas dans XMLInputOutput
// public static void removeUnuseEntity(LinkSet linkSet) {
// for (Link link : linkSet) {
// if (link.haveEntity()) {
// if (link.haveEntity(EntitySet.TypeEster2Time)) {
// link.setEntity(null);
// }
// if (link.haveEntity(EntitySet.TypeEster2TimeDate)) {
// link.setEntity(null);
// }
// }
// }
// }
//
// /**
// * detect entity org.station
// *
// * @param linkSet
// */
// public static void adaptEntity(LinkSet linkSet) {
// for (Link link : linkSet) {
// if (link.haveEntity(EntitySet.TypeEster2Organization) == true) {
// if (isEntityRadio(link.getWord()) == true) {
// link.getEntity().setType(EntitySet.TypeEster2OrganizationStation);
// }
// }
// if (link.haveEntity(EntitySet.TypeEster2Time) == true) {
// if (isEntityHour(link.getWord()) == true) {
// link.getEntity().setType(EntitySet.TypeEster2TimeHours);
// }
// if (isEntityDate(link.getWord()) == true) {
// link.getEntity().setType(EntitySet.TypeEster2TimeDate);
// }
// }
// }
// }
//
// /**
// * Check if the entity org is a station
// *
// * @param word
// * @return true if the word is a station
// */
// public static boolean isEntityRadio(String word) {
// for (String element : SpeakerNameUtils.EntityRadio) {
// if (word.matches(element)) {
// return true;
// }
// }
// return false;
// }
//
// /**
// * Check if the entity time is a hour
// *
// * @param word
// * @return true if the word is a station
// */
// public static boolean isEntityHour(String word) {
// for (String element : SpeakerNameUtils.EntityHour) {
// if (word.matches(element)) {
// return true;
// }
// }
// return false;
// }
//
// /**
// * Check if the entity time is a date
// *
// * @param word
// * @return true if the word is a station
// */
// public static boolean isEntityDate(String word) {
// for (String element : SpeakerNameUtils.EntityDate) {
// if (word.matches(element)) {
// return true;
// }
// }
// return false;
// }
//
// /**
// * Test if the word is equal to an article
// *
// * @param word
// * @return if the word is an article
// */
// public static boolean isARemovableArticle(String word) {
// for (String article : SpeakerNameUtils.Articles) {
// if (word.equals(article)) {
// return true;
// }
// }
// return false;
// }
//
// /**
// * remove all articles of the linkSet
// *
// * @param linkSet
// */
// public static void removeArticle(LinkSet linkSet) {
// for (int i = 0; i < (linkSet.size() - 1);) {
// Link current = linkSet.getLink(i);
// if (isARemovableArticle(current.getWord()) == true) {
// linkSet.remove(i);
// } else {
// i++;
// }
// }
// }
//
// /**
// * Check if the candidate speaker name is a target of the application.
// *
// * @param speakerName the candidate speaker name
// *
// * @return true, if successful
// *
// * TODO remove partial test non name, and clean before entity
// */
// public static boolean checkSpeakerName(String speakerName, boolean isCloseListCheck, TargetNameMap nameAndGenderMap) {
// if (isCloseListCheck == true) {
// String normalizedSpeakerName = normalizeSpeakerName(speakerName);
// if (nameAndGenderMap.containsKey(normalizedSpeakerName) == false) {
// for (String name : nameAndGenderMap.keySet()) {
// if (normalizedSpeakerName.equals(name)) {
// // ??
// nameAndGenderMap.put(normalizedSpeakerName, nameAndGenderMap.get(name));
// return true;
// }
// /*
// * if (normalizedSpeakerName.startsWith(name)) { nameAndGenderMap.put(normalizedSpeakerName, nameAndGenderMap.get(name)); return true; } /*if(normalizedSpeakerName.endsWith(name)) { nameAndGenderMap.put(normalizedSpeakerName,
// * nameAndGenderMap.get(name)); return true; }
// */
// }
// return false;
// } else {
// logger.info("CHECK name not found: " + normalizedSpeakerName);
// }
// } else {
// logger.info("CHECK name not chek: " + speakerName);
// }
// return true;
// }
//
// /**
// * Normalize the speaker name by removing accent and put the string in lower case.
// *
// * @param name the string
// *
// * @return the normalized string
// */
// public static String normalizeSpeakerName(String name) {
// String ch = name.toLowerCase();
// ch = ch.replace('é', 'e');
// ch = ch.replace('è', 'e');
// ch = ch.replace('ê', 'e');
// ch = ch.replace('ë', 'e');
// ch = ch.replace('ô', 'o');
// ch = ch.replace('ö', 'o');
// ch = ch.replace('ï', 'i');
// ch = ch.replace('î', 'i');
// ch = ch.replace('â', 'a');
// ch = ch.replace('à', 'a');
// ch = ch.replace('ä', 'a');
// ch = ch.replace('ü', 'u');
// ch = ch.replace('û', 'u');
// ch = ch.replace('ç', 'c');
// ch = ch.replace(' ', '_');
// ch = ch.replace('-', '_');
//
// return ch;
// }
//
// /**
// * Add the information need for SCT global Information. Global information is questioned by the SCT, information could be : END_TURN, START_TURN, SHORT_TURN and MID_TURN.
// *
// * @param linkSet add information in linkSet
// * @param delay number of word on the right and on the left
// * @param index index the position of the target speaker name in the linkSet
// * @param sizeTurn number of word in the turn
// *
// * TODO: problem a turn could be STAR_TURN and END_TURN, ie a short turn ?
// * @param endTurn
// * @param startTurn
// */
// public static void addGlobalInformation(LinkSet linkSet, int delay, int index, int sizeTurn, boolean startTurn, boolean endTurn) {
// int lenStart = 2 * delay;
// int lenEnd = sizeTurn - (2 * delay);
//
// if ((sizeTurn <= lenStart) && (startTurn == true) && (endTurn == true)) {
// linkSet.setInformation("SHORT_TURN", "true");
// } else if ((index <= lenStart) && (startTurn == true)) {
// linkSet.setInformation("START_TURN", "true");
// } else if ((index >= lenEnd) && (endTurn == true)) {
// linkSet.setInformation("END_TURN", "true");
// } else {
// linkSet.setInformation("MID_TURN", "true");
// }
// }
//
// /**
// * The SCT works over a linkSet of maximum delay*2+1 size centered over a entity of type "pers". The method takes the linkSet of a segment and build a new linkSet of size delay*2+1 with delay link on the left and delay link on the right (if
// * available). Global information is added at the end.
// *
// * TODO: call of addGlobalInformation(result, delay, index, linkSet.size()), linkSet.size() corresponds of the number of the word only if the clusterSet is collapsed
// *
// * @param linkSet corresponding of the full sentence
// * @param index the position of the target speaker name in the linkSet
// * @param delay number of word on the right and on the left
// * @param endTurn
// * @param startTurn
// * @return a reduce LinkSet with the target speaker name as pivot
// * @throws CloneNotSupportedException
// */
// public static LinkSet reduceLinkSetForSCT(LinkSet linkSet, Integer index, int delay, boolean startTurn, boolean endTurn, boolean addGlobal) throws CloneNotSupportedException {
// int start = Math.max(index - delay, 0);
// int end = Math.min(index + delay, linkSet.size() - 1);
// // linkSet.debug();
//
// LinkSet result = new LinkSet(linkSet.getId());
// for (int i = start; i <= end; i++) {
// Link link = (Link) linkSet.getLink(i).cloneWithEntity();
// result.add(link);
// if (i == index) {
// result.getLink(result.size() - 1).getEntity().setType(EntitySet.TypeTargetSpeaker);
// }
// }
// if (addGlobal == true) {
// SpeakerNameUtils.addGlobalInformation(result, delay, index, linkSet.size(), startTurn, endTurn);
// }
// return result;
// }
//
// /**
// * Load a list of target speaker name and gender.
// *
// * @param clientFile the file
// *
// * @return the target speaker name and gender map
// *
// * @throws IOException Signals that an I/O exception has occurred.
// */
// public static TargetNameMap loadList(String clientFile) throws IOException {
// String line;
// TargetNameMap targetSpeakerNameMap = new TargetNameMap();
// BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(clientFile), Parameter.DefaultCharset));
//
// while ((line = bufferedReader.readLine()) != null) {
// String token[] = line.split("\\s+");
// String ch = SpeakerNameUtils.normalizeSpeakerName(token[0]);
// targetSpeakerNameMap.put(ch, token[1]);
// }
// return targetSpeakerNameMap;
// }
//
// /**
// * @param nbOfLabel the nbOfLabel to set
// */
// public static void setNbOfLabel(int nbOfLabel) {
// SpeakerNameUtils.nbOfLabel = nbOfLabel;
// }
//
// /**
// * @return the nbOfLabel
// */
// public static int getNbOfLabel() {
// return nbOfLabel;
// }

}
