/**
 * <p>
 * SNamedSpeaker2
 * </p>
 *
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
 * @author <a href="mailto:vincent.jousse@lium.univ-lemans.fr">Vincent Jousse</a>
 * @version v2.0
 * <p/>
 * Copyright (c) 2007-2009 Universite du Maine. All Rights Reserved. Use is subject to license terms.
 * <p/>
 * THIS SOFTWARE IS PROVIDED BY THE "UNIVERSITE DU MAINE" AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */

package fr.lium.experimental.spkDiarization.programs;

import fr.lium.experimental.spkDiarization.libClusteringData.speakerName.SpeakerName;
import fr.lium.experimental.spkDiarization.libClusteringData.transcription.EntitySet;
import fr.lium.experimental.spkDiarization.libClusteringData.transcription.Link;
import fr.lium.experimental.spkDiarization.libClusteringData.transcription.LinkSet;
import fr.lium.experimental.spkDiarization.libClusteringData.turnRepresentation.Turn;
import fr.lium.experimental.spkDiarization.libClusteringData.turnRepresentation.TurnSet;
import fr.lium.experimental.spkDiarization.libNamedSpeaker.SpeakerNameUtils;
import fr.lium.experimental.spkDiarization.libNamedSpeaker.TargetNameMap;
import fr.lium.experimental.spkDiarization.libSCTree.SCT;
import fr.lium.experimental.spkDiarization.libSCTree.SCTProbabilities;
import fr.lium.experimental.spkDiarization.libSCTree.SCTSolution;
import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libModel.Distance;
import fr.lium.spkDiarization.parameter.Parameter;

/**
 * @author Meignier, Jousse
 */
public class MNamedSpeakerTest {

    public static final double PREVIOUS_THRESHOLD = 0.09;
    public static final double CURRENT_THRESHOLD = 0.2;
    public static final double NEXT_THRESHOLD = 0.2;

    static boolean useSpeakerList;
    static Parameter param;
    static TargetNameMap nameAndGenderMap;
    static boolean trace = true;

    /**
     * Print the available options.
     *
     * @param param is all the parameters
     * @param prog name of this program
     */
    public static void info(Parameter param, String prog) {
        if (param.help) {
            System.out.println("info[info] \t ------------------------------------------------------ ");
            System.out.println("info[program] \t name = " + prog);
            param.printSeparator();
            param.printShow();

            param.parameterSegmentationInputFile.print(); // sInMask
            param.parameterSegmentationOutputFile.print(); // sOutMask
            param.printSeparator();
            param.parameterNamedSpeaker.print();
        }
    }


    /**
     * For each Solution of the SolutionsSet, put probabilities in the Cluster of the previous, current and next Turn for the target speaker
     *
     * @param solution a SCT solution
     * @param speakerName the name of the target speaker
     * @param turns list of turn
     * @param index index of the current turn in turns
     *
     * TODO make permutation of speaker name word (firstname/lastname - lastname/firstname)
     */
    public static void putSpeakerName(SCTSolution solution, String speakerName, TurnSet turns, int index) {

        SCTProbabilities probabilities = solution.getProbabilities();
        String speakerGender = nameAndGenderMap.get(SpeakerNameUtils.normalizeSpeakerName(speakerName));
        if (trace == true)
            System.err.println("normalized spk:" + SpeakerNameUtils.normalizeSpeakerName(speakerName) + " gender:" + speakerGender);
        Turn turn;
        double scorePrev = 0, scoreCurrent = 0, scoreNext = 0.0;
        // previous turn
        if (index - 1 >= 0) {
            turn = turns.get(index - 1);
            scorePrev = probabilities.get(SpeakerNameUtils.PREVIOUS);
            if ((checkGender(turn, speakerGender) == true) && (scorePrev > PREVIOUS_THRESHOLD)) {
                if (trace == true) System.err.println("spk:" + speakerName + " put previous");
                addScore(turn, speakerName, scorePrev);
            }
        }
        // Current turn
        turn = turns.get(index);
        scoreCurrent = probabilities.get(SpeakerNameUtils.CURRENT);
        if ((checkGender(turn, speakerGender) == true) && (scoreCurrent > CURRENT_THRESHOLD)) {
            if (trace == true) System.err.println("spk:" + speakerName + " put current");
            addScore(turn, speakerName, scoreCurrent);
        }
        // newt turn
        if (index + 1 < turns.size()) {
            turn = turns.get(index + 1);
            scoreNext = probabilities.get(SpeakerNameUtils.NEXT);
            if ((checkGender(turn, speakerGender) == true) && (scoreNext > NEXT_THRESHOLD)) {
                if (trace == true) System.err.println("spk:" + speakerName + " put next");
                addScore(turn, speakerName, scoreNext);
            }
        }
        System.err.println("Tree:" + speakerName + " prev=" + scorePrev + " cur=" + scoreCurrent + " next=" + scoreNext);
    }

    /**
     * Check coherence of genres between the target speaker name and the gender of the trun .
     *
     * @param turn the turn
     * @param speakerGender the speaker gender
     *
     * @return true, if successful
     */
    public static boolean checkGender(Turn turn, String speakerGender) {
        if (trace == true)
            System.err.println("gender speaker:" + speakerGender + " check=" + param.parameterNamedSpeaker.isDontCheckGender());
        if (param.parameterNamedSpeaker.isDontCheckGender() == false) {
            if (trace == true)
                System.err.println("gender check speaker:" + speakerGender + " turn=" + turn.getCluster().getGender());
            if (turn.getCluster().getGender().equals(speakerGender) == false) {
                return false;
            }
        }
        return true;
    }

    /**
     * Adds the score to the cluster attached to the turn.
     *
     * @param turn the turn link to a cluster
     * @param name the name of the target speaker
     * @param value the score
     */
    public static void addScore(Turn turn, String name, double value) {
        Cluster cluster = turn.getCluster();
        SpeakerName speakerName = cluster.getSpeakerName(name);

        //Keeping the old way (just summing the score)
        speakerName.incrementScoreCluster(value);

        //Adding the new way, will keep trace of each score
        speakerName.addScoreCluster(value);
    }

    /**
     * Test the SCT over each segment containing a linkSet and an entity. The result of the SCT (speaker name and probability) are stored in the clusters of the
     * previous, current or next turn.
     *
     * @param clusters
     * @param sct
     * @throws CloneNotSupportedException
     * @throws DiarizationException
     *
     * TODO manager open and close speaker list
     */
    public static void computeSCTSCore(ClusterSet clusters, SCT sct, TargetNameMap targetSpeakerNameMap) throws CloneNotSupportedException, DiarizationException {
        TurnSet turns = clusters.getTurns();
        boolean isCloseListCheck = param.parameterNamedSpeaker.isCloseListCheck();
        for (int i = 0; i < turns.size(); i++) {
            if (trace == true)
                System.err.println("+++++++++++++++++++++++++++++++++++++++++++++++");
            Turn currentTurn = turns.get(i);
            LinkSet linkSet = currentTurn.getCollapsedLinkSet();
            //linkSet.debug();
            boolean startTurn = true;
            boolean endTurn = true;
            SpeakerNameUtils.makeLinkSetForSCT(linkSet, startTurn, endTurn);
            if (trace == true)
                System.err.println("*" + currentTurn.first().getStartInSecond() + "/" + currentTurn.last().getLastInSecond() + "/" + currentTurn.getCluster().getName()
                        + "***********************************************");
            for (int index = 0; index < linkSet.size(); index++) {
                Link link = linkSet.getLink(index);
                if (link.haveEntity(EntitySet.TypePersonne) == true) {
                    LinkSet linkSetForTest = SpeakerNameUtils.reduceLinkSetForSCT(linkSet, index, 5, startTurn, endTurn);
                    if (trace == true)
                        System.err.println(currentTurn.first().getStart() + " -------------------------------------------------");
                    if (trace == true) linkSetForTest.debug();
                    String speakerName = link.getWord();
                    if (SpeakerNameUtils.checkSpeakerName(speakerName, isCloseListCheck, nameAndGenderMap) == true) {
                        if (trace == true) System.err.println("test speaker:" + speakerName);
                        SCTSolution solution = sct.test(linkSetForTest);
                        if (trace == true)
                            System.err.println("put solution speaker:" + speakerName);
                        putSpeakerName(solution, speakerName, turns, i);
                    }
                }
            }

        }
    }


    /**
     * Assign the candidate speaker name to cluster.
     *
     * @param clusters the clusters
     * @param result
     *
     * @return the a new cluster set
     *
     */
    public static ClusterSet decide(ClusterSet clusters, ClusterSet result) {

        for (String name : clusters) {
            Cluster cluster = clusters.getCluster(name);
            System.err.print("[debug] decide: Cluster = " + cluster.getName() + " ");
            cluster.getSpeakerNameSet().debug();
        }

        SpeakerName max = new SpeakerName("");
        int size = clusters.clusterGetSize();
        for (int i = 0; i < size; i++) {
            Cluster cluster = getMaxSpeakerName(clusters, max);
            if (cluster == null) {
                break;
            }
            String newName = SpeakerNameUtils.normalizeSpeakerName(max.getName().replace(' ', '_').toLowerCase());
            int dist = Distance.levenshteinDistance(cluster.getName(), max.getName());
            System.err.print("[debug] decide: Cluster = " + cluster.getName() + " --> " + newName + " lenvenshtein=" + dist + " score=" + max.getScore() + " || ");
            cluster.getSpeakerNameSet().debug();
            result.getCluster(cluster.getName()).setName(newName);
            clusters.removeCluster(cluster.getName());
            for (String name : clusters) {
                clusters.getCluster(name).RemoveSpeakerName(max.getName());
            }
        }

        String unk = "unk";
        result.createANewCluster(unk);
        for (String name : clusters) {
            System.err.println("[debug] decide: create unk");
            result.mergeCluster(unk, name);
        }

        return result;
    }

    /**
     * Gets the maximum score of the SpeakerNameSet instance of each cluster instance stored in clusters.
     *
     * @param clusters the clusters in with the cluster with the maximum score is searched
     * @param max the max score SpeakerName instance, this is an output
     *
     * @return the cluster with the maximum score
     */
    public static Cluster getMaxSpeakerName(ClusterSet clusters, SpeakerName max) {
        Cluster maxCluster = null;
        max.set("", Double.NEGATIVE_INFINITY, 1.0);
        for (String name : clusters) {
            Cluster cluster = clusters.getCluster(name);
            SpeakerName tmp = cluster.getMaxSpeakerName();
            if (tmp != null) {
                if (max.getScore() < tmp.getScore()) {
                    max.set(tmp.getName(), tmp.getScore(), 1.0);
                    maxCluster = cluster;
                }
            }
        }
        return maxCluster;
    }

    /*
     * Killer function ;-)
     *
     * It will go through each cluster, and compute each speaker score
     * according to the belief function theory
     */
    public static void computeBeliefFunctions(ClusterSet clusters) throws Exception {

        for (String name : clusters) {
            Cluster cluster = clusters.getCluster(name);
            cluster.computeBeliefFunctions();
        }

    }

        /*
         * Is called to normalized each summed score
         * It computes the score of a speaker for a cluster using normalization
         */

    public static void setScore(ClusterSet clusters) {
/*	Cluster + speaker*/
        /*
		 SpeakerNameSet result = new SpeakerNameSet();
		for(String clusterName: clusters){
			Cluster cluster = clusters.getCluster(clusterName);
			SpeakerNameSet speakerNameSet = cluster.getSpeakerNameSet();
			for(String name: speakerNameSet){
				result.get(name).setScoreSpeaker(0.0);
			}
		}

		for(String clusterName: clusters){
			Cluster cluster = clusters.getCluster(clusterName);
			SpeakerNameSet speakerNameSet = cluster.getSpeakerNameSet();
			for(String name: speakerNameSet){
				result.get(name).incrementScoreCluster(speakerNameSet.get(name).getScoreCluster());
			}
		}
		for(String clusterName: clusters){
			Cluster cluster = clusters.getCluster(clusterName);
			SpeakerNameSet speakerNameSet = cluster.getSpeakerNameSet();
			for(String name: speakerNameSet){
				double d = speakerNameSet.get(name).getScoreCluster();
				speakerNameSet.get(name).setScoreSpeaker(d * d / result.get(name).getScoreCluster());
			}
		}*/

        //Normalize the speakers scores
        for (String name : clusters) {
            Cluster cluster = clusters.getCluster(name);
            cluster.normalizeSpeakerNameSet();
        }
    }


    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        try {
            param = MainTools.getParameters(args);
            info(param, "SNamedSpeaker2");
            if (param.nbShow > 0) {
                ClusterSet clusters = MainTools.readClusterSet(param);
                ClusterSet save = (ClusterSet) clusters.clone();
                clusters.collapse();
                // get the speaker name list
                nameAndGenderMap = null;
                nameAndGenderMap = SpeakerNameUtils.loadList(param.parameterNamedSpeaker.getNameAndGenderList());
                useSpeakerList = true;
                SCT sct = new SCT(SpeakerNameUtils.getNbOfLabel(), param.trace);
                sct.read(param.show, param.parameterNamedSpeaker.getSCTMask());

                computeSCTSCore(clusters, sct, nameAndGenderMap);

                if (!param.parameterNamedSpeaker.isBeliefFunctions()) {
                    //Decide has to be splitted
                    // First compute the score, then decide

                    for (String name : clusters) {
                        Cluster cluster = clusters.getCluster(name);
                        System.err.print("[debug] decide: Cluster = " + cluster.getName() + " ");
                        cluster.getSpeakerNameSet().debug();
                    }
                    System.err.println("+++++++++++++++++++++++++++++++++++++++++++++++");

                    //Compute the score the old way
                    setScore(clusters);

                } else {
                    //Put the code for the belief funtions
                    computeBeliefFunctions(clusters);
                }

                clusters = decide(clusters, save);
                MainTools.writeClusterSet(param, save, true);
            }
        } catch (DiarizationException e) {
            System.out.println("error \t exception " + e.getMessage());
        }
    }

}
