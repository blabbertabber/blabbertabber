/**
 * 
 * <p>
 * SNamedSpeaker
 * </p>
 * 
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
 * @author <a href="mailto:vincent.jousse@lium.univ-lemans.fr">Vincent Jousse</a>
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

package fr.lium.deprecated.spkDiarization;

/**
 * The Class SNamedSpeakerOld.
 * 
 * @deprecated
 */
@Deprecated
public class SNamedSpeakerOld {
// private final static Logger logger = Logger.getLogger(SNamedSpeakerOld.class.getName());
//
// public static final int CURRENT = 0;
// public static final int PREVIOUS = 1;
// public static final int NEXT = 2;
// public static final int OTHER = 3;
//
// public static final double PREVIOUS_THRESHOLD = 0.09;
// public static final double CURRENT_THRESHOLD = 0.2;
// public static final double NEXT_THRESHOLD = 0.2;
//
// public static final String MALE = "M";
// public static final String FEMALE = "F";
//
// private static TreeMap<String, String> firstNameList;
// private static TreeMap<String, String> targetList;
// private static TreeMap<String, String> spkIdNames;
//
// private static boolean openList;
// private static boolean checkGender;
//
// private static TreeMap<String, ArrayList<Cluster>> spkClusters = new TreeMap<String, ArrayList<Cluster>>();
//
// private static final ArrayList<Cluster> sortedClusters = new ArrayList<Cluster>();
//
// public static String tagToString(int tagValue) {
// if (tagValue == CURRENT) {
// return "current";
// } else if (tagValue == PREVIOUS) {
// return "previous";
// } else if (tagValue == NEXT) {
// return "next";
// } else if (tagValue == OTHER) {
// return "other";
// } else {
// return "unknown";
// }
// }
//
// public static ArrayList<Pair<Integer, Double>> getThresholdLabelList(double currentScore, double nextScore, double otherScore, double previousScore) {
//
// ArrayList<Pair<Integer, Double>> labelList = new ArrayList<Pair<Integer, Double>>();
//
// if (previousScore >= PREVIOUS_THRESHOLD) {
// labelList.add(new Pair<Integer, Double>(PREVIOUS, previousScore));
// }
// if (currentScore >= CURRENT_THRESHOLD) {
// labelList.add(new Pair<Integer, Double>(CURRENT, currentScore));
// }
// if (nextScore >= NEXT_THRESHOLD) {
// labelList.add(new Pair<Integer, Double>(NEXT, nextScore));
// }
// // return new Pair<Integer, Double>(OTHER, otherScore) ;
// return labelList;
// }
//
// @SuppressWarnings("unchecked")
// public static void sortNamedSpkByCluster(ClusterSet clusters) {
// ArrayList<String> sortedNamedSpeaker;
//
// // On parcourt tous les clusters
// // Ancienne méthode
// // Iterator<Cluster> itCluster =
// // clusters.getClusters().values().iterator();
//
// // Nouvelle methode
// for (Cluster cluster : clusters.clusterSetValue()) {
//
// TreeMap<String, NamedSpeaker> userDataCluster = (TreeMap<String, NamedSpeaker>) cluster.getUserData();
//
// // Si il y a un nom associé au cluster
// if (userDataCluster != null) {
// sortedNamedSpeaker = new ArrayList<String>();
//
// // On parcourt tous les noms et on les classe dans l'arraylist
// // en fonction de leur score combiné
// for (String key : userDataCluster.keySet()) {
//
// // On met a jour la map permettant d'obtenir tous les
// // clusters où se trouve un nom
// if (spkClusters.containsKey(key) == false) {
// ArrayList<Cluster> c = spkClusters.get(key);
// c.add(cluster);
// } else {
// ArrayList<Cluster> c = new ArrayList<Cluster>();
// spkClusters.put(key, c);
// }
//
// double combinedScore = userDataCluster.get(key).getCombinedScore();
//
// if (sortedNamedSpeaker.size() == 0) {
// sortedNamedSpeaker.add(key);
// } else {
// boolean inserted = false;
// for (int j = 0; j < sortedNamedSpeaker.size(); j++) {
// String testKey = sortedNamedSpeaker.get(j);
// // Si celui qu'on parcourt actuellement est plus
// // grand que le premier, on le met devant
// if (combinedScore > userDataCluster.get(testKey).getCombinedScore()) {
// sortedNamedSpeaker.add(j, key);
// inserted = true;
// break;
// }
// }
// // Si il est plus petit que tous, on l'ajoute à la fin
// if (!inserted) {
// sortedNamedSpeaker.add(key);
// }
// }
// }
// cluster.setSortedUserData(sortedNamedSpeaker);
// }
// }
// }
//
// @SuppressWarnings("unchecked")
// public static void sortClustersByCombinedScore(ClusterSet clusters) {
//
// // On parcourt tous les clusters
// for (Cluster cluster : clusters.clusterSetValue()) {
//
// // On recupere le score max du cluster qu'on teste
// TreeMap<String, NamedSpeaker> userDataCluster = (TreeMap<String, NamedSpeaker>) cluster.getUserData();
//
// // Si il y a un nom associé au cluster
// if ((userDataCluster != null) && (userDataCluster.size() > 0)) {
// ArrayList<String> clusterSortedNames = (ArrayList<String>) cluster.getSortedUserData();
// double maxClusterCombinedScore = userDataCluster.get(clusterSortedNames.get(0)).getCombinedScore();
//
// // Si la liste des clusters tries par score combiné le plus
// // eleve est vide, on ajoute directement le cluster dedans
// if (sortedClusters.size() == 0) {
// sortedClusters.add(cluster);
// } else {
// boolean inserted = false;
// // On parcourt les clusters déjà tries, et on regarde ou
// // ajouter notre nouveau cluster
// for (int i = 0; i < sortedClusters.size(); i++) {
// // On recupere le score max du cluster deja trie
// TreeMap<String, NamedSpeaker> userData = (TreeMap<String, NamedSpeaker>) sortedClusters.get(i).getUserData();
// ArrayList<String> sortedNames = (ArrayList<String>) sortedClusters.get(i).getSortedUserData();
// if ((sortedNames == null) || (sortedNames.size() == 0)) {
// sortedClusters.add(i, cluster);
// inserted = true;
// break;
// } else {
// double maxSortedClusterCombinedScore = userData.get(sortedNames.get(0)).getCombinedScore();
// // Si le cluster qu'on teste a un score combine plus
// // grand, on l'ajoute en tete
// if (maxClusterCombinedScore > maxSortedClusterCombinedScore) {
// sortedClusters.add(i, cluster);
// inserted = true;
// break;
// }
// }
// }
// // Si il est plus petit que tous, on l'ajoute à la fin
// if (!inserted) {
// sortedClusters.add(cluster);
// }
// }
// } else {
// // Si il ya pas de données sur le cluster, on l'ajoute à la fin
// sortedClusters.add(cluster);
// }
// }
// }
//
// /**
// * V�rifie que le genre est coh�rent avec le cluster
// *
// * @param userDataCluster Les diff�rents noms possibles pour le cluster
// * @param gender Le genre du cluster auquel on veut les attribuer
// */
// public static void checkNE(TreeMap<String, NamedSpeaker> userDataCluster, String gender) {
//
// if (userDataCluster == null) {
// return;
// }
//
// Iterator<String> it = userDataCluster.keySet().iterator();
//
// // On parcourt tous les noms possibles pour le cluster
// while (it.hasNext()) {
// String key = it.next();
//
// // System.err.println("--- " + key);
//
// StringTokenizer stokFirstname = new StringTokenizer(key, "_");
// String firstName = stokFirstname.nextToken();
//
// boolean toDelete = false;
//
// // Nom partiel
// // Liste ouverte donc rejet selon la liste de pr�noms
// if (!stokFirstname.hasMoreTokens() || (openList && !firstNameList.containsKey(firstName))) {
// toDelete = true;
// }
//
// if (toDelete
// || (firstNameList.containsKey(firstName) && "remove".equals(firstNameList.get(firstName)))
// || (firstNameList.containsKey(firstName) && !"U".equals(firstNameList.get(firstName))
// && !gender.equals(firstNameList.get(firstName)) && checkGender)) {
//
// // System.err.println("@@@@@@@ On vire " + key + ", gender : -" + gender + "-, listGender : -" + firstNameList.get(firstName));
//
// // System.out.println("##### Count Remove Gender : " + key);
//
// /**
// * System.err.println(!stokFirstname.hasMoreTokens() || !firstNameList.containsKey(firstName) || (!"U".equals(firstNameList.get(firstName)) && !gender.equals(firstNameList.get(firstName))) || (spkIdNames.containsKey(key)));
// *
// *
// * System.err.println(!stokFirstname.hasMoreTokens() + "||" + !firstNameList.containsKey(firstName) + "||" + (!"U".equals(firstNameList.get(firstName)) && !gender.equals(firstNameList.get(firstName))) + "||" +
// * (spkIdNames.containsKey(key)));
// **/
// it.remove();
// }
//
// }
//
// }
//
// @SuppressWarnings("unchecked")
// public static void checkTarget(Cluster cluster) {
//
// TreeMap<String, NamedSpeaker> userDataCluster = (TreeMap<String, NamedSpeaker>) cluster.getUserData();
//
// if (userDataCluster == null) {
// return;
// }
//
// Iterator<String> it = userDataCluster.keySet().iterator();
// // System.err.println("Cluster : " + cluster.getName());
// // On parcourt tous les noms possibles
// while (it.hasNext()) {
// String key = it.next();
//
// // System.err.println(key);
//
// // System.out.println("##### Count Total List : " + key);
//
// if (!targetList.containsKey(key)) {
// it.remove();
// // System.err.println("##### Remove : " + key);
// // System.out.println("##### Count Remove List : " + key);
// }
//
// }
// }
//
// public static Pair<String, Integer> extractKeyInformation(String key) {
// // Tokenizer sur la clef (speakerNE:1:Maude@Bayeu)
// StringTokenizer stok2 = new StringTokenizer(key, ":");
// stok2.nextToken();
// // On saute le numero de locuteur
// int idx = Integer.parseInt(stok2.nextToken());
// // On r�cup�re le nom que veut attribuer l'arbre
// String name = stok2.nextToken();
//
// if (stok2.hasMoreTokens()) {
//
// name += ":" + stok2.nextToken();
// }
//
// // System.err.println("nameNE : " + name + " idx="+idx);
//
// return new Pair<String, Integer>(name, idx);
// }
//
// public static ArrayList<Pair<Integer, Double>> extractValueInformation(String value) {
// // Tokenizer sur la clef (speakerNE:1:Maude@Bayeu)
// StringTokenizer stok2 = new StringTokenizer(value, ":");
//
// // On regarde lequel du current/next/other/previous est le
// // maximum (en pond�rant par la PAP)
// // Pair<Integer, Double> pair =
// // getMaxLabel(Double.parseDouble(stok2.nextToken()),
// // Double.parseDouble(stok2.nextToken()),
// // Double.parseDouble(stok2.nextToken()),
// // Double.parseDouble(stok2.nextToken()));
//
// return getThresholdLabelList(Double.parseDouble(stok2.nextToken()), Double.parseDouble(stok2.nextToken()), Double.parseDouble(stok2.nextToken()), Double.parseDouble(stok2.nextToken()));
//
// }
//
// static void convertLocalNEformationInNEInformationSegment(ClusterSet clusters, double thr) {
// ArrayList<Segment> vSeg = clusters.getSegmentVectorRepresentation();
// int size = vSeg.size();
//
// // On parcourt tous les clusters (sous forme de segment) pour leur
// // attribuer les noms avec les probas
// for (int i = 0; i < size; i++) {
// Segment seg = vSeg.get(i);
// // System.err.println("i : " + i + "(" + seg.getStart() + "-" +
// // seg.getLength() + ")" + seg.getInformations());
//
// // On recupere les infos de chaque segment (les decisions de
// // l'arbre)
// TreeMap<String, Object> information = seg.getInformation();
// // On recup�re le nom de locuteur du segment courant
// String nameID = seg.getClusterName();
// Iterator<String> it = information.keySet().iterator();
//
// while (it.hasNext()) {
// String key = it.next();
// // System.err.println("key : " + key);
// if (key.startsWith("speakerNE")) {
//
// // System.err.println(nameNE+"="+(String)
// // information.get(key)+" score="+pair.snd+" max="+tagToString(pair.fst));
//
// Pair<String, Integer> couple = extractKeyInformation(key);
// int spkIndex = couple.getSecond();
// String nameNE = couple.getFirst();
//
// ArrayList<Pair<Integer, Double>> labelList = extractValueInformation((String) information.get(key));
//
// // Si le score est sup�rieur au thresold donn� en param
// // On attribue le locuteur au cluster (avec liste de
// // prenoms)
// // if (pair.snd > thr){
// // if (pair.snd > thr*0.25){
// Pair<Integer, Double> pair;
//
// if (labelList.size() > 0) {
// for (int j = 0; j < labelList.size(); j++) {
// pair = labelList.get(j);
// if (pair.getFirst() == CURRENT) {
// // System.out.println("On attribue " + nameNE +
// // " au courant");
// setNEInformationInCluster(clusters, vSeg, i, nameNE, spkIndex, pair.getSecond(), "doNotCompare");
// } else if (pair.getFirst() == PREVIOUS) {
// // System.out.println("On attribue " + nameNE +
// // " au precedent");
// setNEInformationInCluster(clusters, vSeg, i - 1, nameNE, spkIndex, pair.getSecond(), nameID);
// } else if (pair.getFirst() == NEXT) {
// // System.out.println("On attribue " + nameNE +
// // " au suivant, pos : " + (i+1));
// setNEInformationInCluster(clusters, vSeg, i + 1, nameNE, spkIndex, pair.getSecond(), nameID);
// }
//
// }
// }
// // }
// }
// }
// }
//
// }
//
// /**
// * Attribue un nom � un cluster donn� On v�rifie ici que le nom qu'on attribue est compatible avec la liste des locuteurs
// *
// */
//
// @SuppressWarnings("unchecked")
// static void computeNEStatInCluster(Cluster cluster) {
//
// double totalScore = 0;
//
// TreeMap<String, NamedSpeaker> namesClusters = (TreeMap<String, NamedSpeaker>) cluster.getUserData();
//
// if (namesClusters != null) {
//
// // On parcourt tous les noms de ce cluster
//
// Iterator<String> it = namesClusters.keySet().iterator();
// while (it.hasNext()) {
// String key = it.next();
// totalScore += namesClusters.get(key).getScore();
// }
//
// it = namesClusters.keySet().iterator();
// while (it.hasNext()) {
// String key = it.next();
// NamedSpeaker spk = namesClusters.get(key);
// spk.setRepartition(spk.getScore() / totalScore);
// spk.setCombinedScore(spk.getRepartition() * spk.getScore());
// }
//
// }
//
// }
//
// @SuppressWarnings("unchecked")
// static void display(ClusterSet clusters) {
//
// System.err.println("############### DISPLAY ##################");
//
// // On parcourt tous les clusters
//
// Iterator<Cluster> itCluster = clusters.clusterSetValueIterator();
// while (itCluster.hasNext()) {
// Cluster cluster = itCluster.next();
// TreeMap<String, NamedSpeaker> userDataCluster = (TreeMap<String, NamedSpeaker>) cluster.getUserData();
//
// System.err.println("##### " + cluster.getName());
//
// if (cluster.getUserData() != null) {
//
// Iterator<String> it = userDataCluster.keySet().iterator();
//
// // On parcourt tous les noms possibles
// while (it.hasNext()) {
// String key = it.next();
//
// System.err.println("--- " + key);
// }
//
// }
// }
// }
//
// /**
// *
// * @param clusters Les clusters du fichier avec les infos NE
// * @return les clusters modifi�s avec la prise de d�cision
// */
// @SuppressWarnings("unchecked")
// static ClusterSet decide(ClusterSet clusters) {
//
// System.err.println("---> Decide");
//
// // display(clusters);
//
// // On parcourt tous les clusters
// Iterator<Cluster> itCluster = clusters.clusterSetValueIterator();
// while (itCluster.hasNext()) {
// Cluster cluster = itCluster.next();
// TreeMap<String, NamedSpeaker> userDataCluster = (TreeMap<String, NamedSpeaker>) cluster.getUserData();
//
// // On met tous les noms de clusters a null
// // clusters.getNameMap().remove(cluster.getName());
// // cluster.setName(null);
//
// if (cluster.getUserData() != null) {
//
// System.err.println();
// System.err.println("On regarde pour " + cluster.getName());
//
// // Pour chaque cluster, on va verifier si les noms.prenoms sont
// // coherents avec la liste
// checkNE(userDataCluster, cluster.getGender());
//
// // On calcule la combinaison des scores et de la repartition
// // System.out.println("Before compute");
// computeNEStatInCluster(cluster);
//
// // La ca serait bien d'enlever ceux qui ne sont pas dans la
// // liste des locuteurs cibles
// if (!openList) {
// checkTarget(cluster);
// }
//
// // System.out.println("After compute");
// /**
// * String newName = ""; //getMaxNameNECombinedScore(userDataCluster);
// *
// * System.err.println("New name : " + newName);
// *
// * String oldName = cluster.getName(); if(!newName.equals("unk")) { spkIdNames.put(newName, oldName); } cluster.setInformation("name", new String (oldName)); cluster.setName(newName); clusters.getNameMap().put(newName,
// * cluster.getId()); clusters.getNameMap().remove(oldName);
// **/
// } else {
// System.err.println("User data � null");
//
// /**
// * String oldName = cluster.getName(); cluster.setName("unk"); clusters.getNameMap().put("unk", cluster.getId()); clusters.getNameMap().remove(oldName);
// **/
// }
// }
//
// // On tri les speakers par cluster
// sortNamedSpkByCluster(clusters);
//
// // On tri les clusters en mettant en premier celui qui a le spk avec le
// // plus grand score
// sortClustersByCombinedScore(clusters);
//
// Iterator<Cluster> it = sortedClusters.iterator();
//
// while (it.hasNext()) {
// Cluster cluster = it.next();
// System.out.println("### " + cluster.getName());
//
// ArrayList<String> sortedNamedSpeaker = (ArrayList<String>) cluster.getSortedUserData();
// TreeMap<String, NamedSpeaker> userDataCluster = (TreeMap<String, NamedSpeaker>) cluster.getUserData();
// // On va afficher le tri pour �tre sur
// if (sortedNamedSpeaker != null) {
//
// for (int i = 0; i < sortedNamedSpeaker.size(); i++) {
// String key = sortedNamedSpeaker.get(i);
// System.out.println(key + " : " + userDataCluster.get(key).getCombinedScore() + " = "
// + userDataCluster.get(key).getScore() + " * " + userDataCluster.get(key).getRepartition());
// }
//
// }
//
// }
//
// while (decideOneCluster(clusters)) {
// System.out.println("decide one");
// sortClustersByCombinedScore(clusters);
// }
//
// // On fait la passe en unk maintenant
//
// itCluster = clusters.clusterSetValueIterator();
// while (itCluster.hasNext()) {
// Cluster cluster = itCluster.next();
// if (cluster.getName().startsWith("ANON") && cluster.getName().endsWith("NYME")) {
// cluster.setName("unk");
// cluster.putInformation("name", new String("unk"));
// // clusters.getNameMap().put("unk", cluster.getId());
// }
// }
//
// System.err.println("<--- Decide");
//
// return clusters;
// }
//
// @SuppressWarnings("unchecked")
// static boolean decideOneCluster(ClusterSet clusters) {
// Iterator<Cluster> it = sortedClusters.iterator();
//
// // On prend le premier cluster avec le plus gros score
// if (it.hasNext()) {
// Cluster cluster = it.next();
//
// // POur chaque cluster, on recupere le nom des candidats possibles
// ArrayList<String> names = (ArrayList<String>) cluster.getSortedUserData();
// TreeMap<String, NamedSpeaker> userDataCluster = (TreeMap<String, NamedSpeaker>) cluster.getUserData();
//
// // Si il y a des candidats pour le cluster
// if ((names != null) && (names.size() > 0) && (userDataCluster != null) && (userDataCluster.size() > 0)) {
//
// Iterator<String> itKey = names.iterator();
//
// // on prend le premier candidat, et on l'affecte a ce cluster
// if (itKey.hasNext()) {
// String oldName = cluster.getName();
// String newName = itKey.next();
//
// System.out.println("Pour " + oldName + " on attribue : " + newName);
//
// /**
// * if(newName.equals("unk")) { System.out.println("##### Count Attribution unk"); } else { System.out.println("##### Count Attribution ok : " + newName); }
// **/
//
// if (!newName.equals("unk")) {
// spkIdNames.put(newName, oldName);
// }
// cluster.putInformation("name", new String(newName));
// cluster.setName(newName);
// // clusters.getNameMap().put(newName, cluster.getId());
//
// // if(oldName != null)
// // clusters.getNameMap().remove(oldName);
//
// // Une fois affecte, on l'enleve des clusters ou il se
// // trouve
// ArrayList<Cluster> affectedClusters = spkClusters.get(newName);
//
// Iterator<Cluster> itAffectedClusters = affectedClusters.iterator();
//
// while (itAffectedClusters.hasNext()) {
// Cluster affectedCluster = itAffectedClusters.next();
// TreeMap<String, NamedSpeaker> userDataAffectedCluster = (TreeMap<String, NamedSpeaker>) affectedCluster.getUserData();
//
// if (userDataAffectedCluster != null) {
// userDataAffectedCluster.remove(newName);
// }
//
// ArrayList<String> sortedSpk = (ArrayList<String>) affectedCluster.getSortedUserData();
//
// if (sortedSpk != null) {
// sortedSpk.remove(newName);
// }
//
// }
//
// // On vide le cluster actuel, il est affecte
// cluster.setSortedUserData(null);
// cluster.setUserData(null);
//
// return true;
//
// }
//
// }
//
// }
//
// return false;
//
// }
//
// public static Pair<Integer, Double> getMaxLabel(double currentScore, double nextScore, double otherScore, double previousScore) {
// currentScore *= 0.07;
// nextScore *= 0.45;
// otherScore *= 0.35;
// previousScore *= 0.13;
// if ((previousScore >= currentScore) && (previousScore >= nextScore) && (previousScore >= otherScore)) {
// return new Pair<Integer, Double>(PREVIOUS, previousScore);
// }
// if ((currentScore >= previousScore) && (currentScore >= nextScore) && (currentScore >= otherScore)) {
// return new Pair<Integer, Double>(CURRENT, currentScore);
// }
// if ((nextScore >= previousScore) && (nextScore >= currentScore) && (nextScore >= otherScore)) {
// return new Pair<Integer, Double>(NEXT, nextScore);
// }
// return new Pair<Integer, Double>(OTHER, otherScore);
// }
//
// public static String getMaxNameNEClass(TreeMap<String, NamedSpeaker> userDataCluster) {
// String result = "unk";
// double max = 0;
// Iterator<String> it = userDataCluster.keySet().iterator();
// while (it.hasNext()) {
// String key = it.next();
//
// double score = userDataCluster.get(key).getScore();
//
// // System.err.println("##### " + key + " : " + score);
//
// if (max < score) {
// max = score;
// result = key;
// }
// }
// return result;
// }
//
// static void makeSpeakerTurn(ClusterSet clusters) {
// ArrayList<Segment> vSeg = clusters.getSegmentVectorRepresentation();
// int i = 1;
// while (i < vSeg.size()) {
// Segment prev = vSeg.get(i - 1);
// Segment cur = vSeg.get(i);
// // String namePrev = prev.getInformation("name");
// // String nameCur = cur.getInformation("name");
// String namePrev = prev.getClusterName();
// String nameCur = cur.getClusterName();
//
// if (namePrev.compareTo(nameCur) == 0) {
// /**
// * int prevEnd = prev.getStartFrameIndex() + prev.getLength() + 250; int curStart = cur.getStartFrameIndex(); if( prevEnd >= curStart){ int prevStart = prev.getStartFrameIndex(); int curLen = cur.getLength(); prev.setLength(curStart -
// * prevStart + curLen); prev.getInformation().putAll(cur.getInformation()); vSeg.remove(i); i--; }
// **/
// int curStart = cur.getStart();
// int prevStart = prev.getStart();
// int curLen = cur.getLength();
// prev.setLength((curStart - prevStart) + curLen);
// prev.getInformation().putAll(cur.getInformation());
// // prev.setGender(prev.getCluster().getGender());
// vSeg.remove(i);
// cur.getCluster().removeSegment(cur);
// i--;
//
// }
// i++;
// }
// // ClusterSet res = new ClusterSet();
// // res.addVector(vSeg);
//
// // return res;
// }
//
// @SuppressWarnings("unchecked")
// static void putNameNEInClusterInformation(ClusterSet clusters) {
// Iterator<Cluster> itCluster = clusters.clusterSetValueIterator();
// while (itCluster.hasNext()) {
// Cluster cluster = itCluster.next();
// TreeMap<String, NamedSpeaker> userDataCluster = (TreeMap<String, NamedSpeaker>) cluster.getUserData();
// if (cluster.getUserData() != null) {
// Iterator<String> it = userDataCluster.keySet().iterator();
// while (it.hasNext()) {
// String key = it.next();
// double score = userDataCluster.get(key).getScore();
// cluster.putInformation(key, score);
// }
// }
// }
// }
//
// /**
// * Sylvain
// *
// * @param clusters
// * @param vSeg
// * @param pos
// * @param nameNE
// * @param score
// * @param nameID
// * @return
// * @SuppressWarnings("unchecked") static String setNEInformationInCluster(ClusterSet clusters, ArrayList<Segment> vSeg, int pos, String nameNE, double score, String nameID) { if ((pos > 0) && (pos < vSeg.size())) { Segment seg = vSeg.get(pos);
// * String nameCluster = seg.getInformation("name"); if (nameCluster.compareTo(nameID) != 0) { // Segment if (seg.getUserData() == null) { seg.setUserData(new TreeMap<String, NamedSpeaker>()); } TreeMap<String,
// * NamedSpeaker> namesSeg = (TreeMap<String, NamedSpeaker>) seg.getUserData(); namesSeg.put(nameNE, new NamedSpeaker(nameCluster, 1, score));
// *
// * // Cluster Cluster cluster = clusters.getCluster(nameCluster); if (cluster.getUserData() == null) { cluster.setUserData(new TreeMap<String, NamedSpeaker>()); } TreeMap<String, NamedSpeaker> namesClusters =
// * (TreeMap<String, NamedSpeaker>) cluster.getUserData(); if (namesClusters.get(nameNE) == null) { namesClusters.put(nameNE, new NamedSpeaker(nameCluster, 1, score)); } else {
// * namesClusters.get(nameNE).addScore(score); } return nameCluster; } } return ""; }
// **/
//
// /**
// * Attribue un nom � un cluster donn� On v�rifie ici que le nom qu'on attribue est compatible avec la liste des locuteurs
// *
// */
//
// @SuppressWarnings("unchecked")
// static String setNEInformationInCluster(ClusterSet clusters, ArrayList<Segment> vSeg, int pos, String nameNE, int spkIndex, double score, String nameID) {
//
// if ((pos > 0) && (pos < vSeg.size())) {
//
// Segment seg = vSeg.get(pos);
// String nameCluster = seg.getClusterName();
//
// // On attribue le nom que si il est diff�rent du cluster ou on veut
// // l'attribuer
// // Si il il y a au moins nom/prenom
// // Si le prenom fait partie de la liste des 21000 prenoms
// // Si le genre du pr�nom n'est pas mixte
// // Si le sexe du pr�nom correspond au sexe du cluster
//
// if (nameCluster.compareTo(nameID) != 0) {
//
// // Segment
// if (seg.getUserData() == null) {
// seg.setUserData(new TreeMap<String, NamedSpeaker>());
// }
// TreeMap<String, NamedSpeaker> namesSeg = (TreeMap<String, NamedSpeaker>) seg.getUserData();
// if (namesSeg.containsKey(nameNE)) {
// // Si le nom est deja dans la map on cumule les scores
// namesSeg.get(nameNE).addScore(score);
// } else {
// namesSeg.put(nameNE, new NamedSpeaker(nameCluster, 1, score));
// }
//
// // Cluster
// Cluster cluster = clusters.getCluster(nameCluster);
// if (cluster.getUserData() == null) {
// cluster.setUserData(new TreeMap<String, NamedSpeaker>());
// }
// TreeMap<String, NamedSpeaker> namesClusters = (TreeMap<String, NamedSpeaker>) cluster.getUserData();
// if (namesClusters.get(nameNE) == null) {
// namesClusters.put(nameNE, new NamedSpeaker(nameCluster, 1, score));
// } else {
// namesClusters.get(nameNE).addScore(score);
// }
// return nameCluster;
// }
// }
// return "";
// }
//
// public static void info(Parameter param, String prog) {
// if (param.help) {
// System.out.println("info[info] \t ------------------------------------------------------ ");
// System.out.println("info[program] \t name = " + prog);
// param.getSeparator();
// param.logShow();
//
// param.getParameterSegmentationInputFile().logMask(); // sInMask
// param.getParameterSegmentationInputFile().logEncodingFormat();
// param.getParameterSegmentationOutputFile().logMask(); // sOutMask
// param.getParameterSegmentationOutputFile().logEncodingFormat();
// param.getSeparator();
// param.getParameterNamedSpeaker().logThreshold();
// // param.getParameterNamedSpeaker().printFirstnameList();
// // param.getParameterNamedSpeaker().printTargetList();
// }
// }
//
// public static void loadFirstNameLst(String firstNameFile) {
//
// String line;
// BufferedReader bufferedReader;
//
// firstNameList = new TreeMap<String, String>();
//
// try {
// // On charge la liste pass�e en param
// bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(firstNameFile), "ISO-8859-1"));
//
// while ((line = bufferedReader.readLine()) != null) {
//
// String firstName = null;
// String sex = null;
//
// StringTokenizer stok = new StringTokenizer(line, " ");
// int res = 0;
//
// // On extrait pour chaque ligne son prenom et son genre
// while (stok.hasMoreTokens()) {
// if (res == 0) {
// firstName = stok.nextToken();
// } else if (res == 1) {
// sex = stok.nextToken();
// }
// res++;
// }
//
// // On se cr�e une map avec comme clef les pr�noms et la valeur
// // le sexe
// firstNameList.put(firstName, sex);
//
// }
//
// firstNameList.put("al", "remove");
// } catch (IOException e) {
// logger.log(Level.SEVERE, "", e);
// e.printStackTrace();
// }
//
// }
//
// public static void loadTargetLst(String clientFile) {
//
// String line;
// BufferedReader bufferedReader;
//
// targetList = new TreeMap<String, String>();
//
// System.out.println("#######  Client File : " + clientFile);
//
// try {
// // On charge la liste pass�e en param
// bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(clientFile), "ISO-8859-1"));
//
// while ((line = bufferedReader.readLine()) != null) {
//
// String name = null;
//
// StringTokenizer stok = new StringTokenizer(line, " ");
//
// while (stok.hasMoreTokens()) {
// name = stok.nextToken();
// }
//
// // On se cr�e une map avec comme clef les pr�noms et la valeur
// // le sexe
// // System.out.println("On charge " + name);
// targetList.put(name, "");
//
// }
// } catch (IOException e) {
// logger.log(Level.SEVERE, "", e);
// e.printStackTrace();
// }
//
// }
//
// /*
// * public static void main(String[] args) throws Exception { try { Parameter param = MainTools.getParam("SNamedSpeaker", args, "sNamedSpeaker"); info(param, "SNamedSpeaker"); if (param.nbShow > 0) { // clusters ClusterSet clusters =
// * MainTools.getInputClusters(param); // clusters.putClusterNameInSegmentInformation(); clusters = makeSpeakerTrun(clusters); convertLocalNEformationInNEInformationSegment(clusters, param.getParameterNamedSpeaker().getThreshold());
// decide(clusters);
// * putNameNEInClusterInformation(clusters); MainTools.setOutputClusters(param, clusters, false); } } catch (SphinxClustException e) { System.out.println("error \t exception " + e.getMessage()); } }
// */
//
// public static void main(String[] args) throws Exception {
// /*
// * try { Parameter param = MainTools.getParameters("SNamedSpeaker", args); if (param.nbShow > 0) { checkGender = !param.getParameterNamedSpeaker().getRemoveCheckGender(); ClusterSet clusters = MainTools.readClusterSet(param);
// * makeSpeakerTurn(clusters); // On charge la liste des prénoms System.out.println(param.getParameterNamedSpeaker().getTargetList()); if (param.getParameterNamedSpeaker().getTargetList().equals("")) { openList = true; } else {
// * loadTargetLst(param.getParameterNamedSpeaker().getTargetList()); } loadFirstNameLst(param.getParameterNamedSpeaker().getFirstnameList()); spkIdNames = new TreeMap<String, String>(); convertLocalNEformationInNEInformationSegment(clusters,
// * param.getParameterNamedSpeaker().getThreshold()); decide(clusters); putNameNEInClusterInformation(clusters); makeSpeakerTurn(clusters); MainTools.writeClusterSet(param, clusters, false); } } catch (DiarizationException e) {
// * System.out.println("error \t exception " + e.getMessage()); }
// */}

}

/*
 * Note : Scoring Tranter = 0.4 - Recall : 15.34 %, precision : 78.06 %, f-mesure: 25.6411220556745 (avec controle des effectations, avec les currents) Scoring Tranter = 0.4 - Recall : 15.74 %, precision : 82.85 %, f-mesure: 26.4541839943199 (avec
 * controle des effectations,sans currents, bug) Scoring Tranter = 0.4 - Recall : 15.13 %, precision : 77.01 %, f-mesure: 25.2911070110701 (sans contr�le l'affectation des noms) Scoring Tranter = 0.4 - Recall : 15.30 %, precision : 82.44 %, f-mesure:
 * 25.8099447513812 (avec controle des effectations, Cur Prev next pond�r�e par proba aprioi 0.11, 0.19, 0.7 ) Scoring Tranter = 0.4 - Recall : 15.30 %, precision : 82.44 %, f-mesure: 25.8099447513812(avec controle des effectations, le 4 cat�gorie
 * pond�r�e par proba apriori )
 */
