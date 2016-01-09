/**
 * <p>
 * SpeakerNameSet
 * </p>
 *
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
 * @version v3.0
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

package fr.lium.experimental.spkDiarization.libClusteringData.speakerName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;

import fr.lium.experimental.spkDiarization.libNamedSpeaker.SpeakerNameUtils;
import fr.lium.spkDiarization.libModel.Distance;

// TODO: Auto-generated Javadoc

/**
 * The Class SpeakerNameSet.
 */
public class SpeakerNameSet implements Iterable<String> {

    /** The container. */
    TreeMap<String, SpeakerName> container;
    TreeSet<SpeakerName> containerByScore;

    /** the container is sorted or not. */
    boolean isSorted;

    /** The sum of score for this speaker name set. */
    double sumOfScore;

    /**
     * Instantiates a new speaker name set.
     */
    public SpeakerNameSet() {
        super();
        container = new TreeMap<String, SpeakerName>();
        containerByScore = null;
        isSorted = false;
        sumOfScore = 0;
    }

    /**
     * Put.
     *
     * @param name the name of the speaker
     *
     * @return the speaker name
     */
    public SpeakerName put(String name) {
        if (containerByScore != null) {
            isSorted = false;
        }
        name = SpeakerNameUtils.normalizeSpeakerName(name.replace(' ', '_').toLowerCase());
        return container.put(name, new SpeakerName(name));
    }

    /**
     * Gets the speakerName or create it.
     *
     *
     * @param name the name of the speaker
     *
     * @return the speaker name
     */
    public SpeakerName get(String name) {

        //we should normalize before putting
        name = SpeakerNameUtils.normalizeSpeakerName(name.replace(' ', '_').toLowerCase());

        if (container.containsKey(name)) {
            return container.get(name);
        }
        if (containerByScore != null) {
            isSorted = false;
        }
        put(name);
        //put(name);
        return container.get(name);
    }

    /**
     * put the speaker name instance.
     *
     * @param o the speaker name instance
     *
     * @return the speaker name
     */
    public SpeakerName put(SpeakerName o) {
        if (containerByScore != null) {
            isSorted = false;
        }
        return container.put(o.getName(), o);
    }

    /**
     * Removes the speaker name.
     *
     * @param name the name of the speaker
     *
     * @return the speaker name instance
     */
    public SpeakerName remove(String name) {
        if (containerByScore != null) {
            isSorted = false;
        }
        return container.remove(name);
    }

    /**
     * Clear.
     */
    public void clear() {
        containerByScore = null;
        container.clear();
    }

    /**
     * Size.
     *
     * @return the size
     */
    public int size() {
        return container.size();
    }

    /**
     * Debug.
     */
    public void debug() {
        System.out.println("debug[SpeakerNameSet]: size=" + size() + " sum=" + sumOfScore + " || ");
        for (SpeakerName speakerName : container.values()) {
            speakerName.debug();
        }
        System.out.println("");
    }

    /**
     * Normalize score.
     */
    public void normalizeScoreCluster() {
        sumOfScore = 0;
        for (SpeakerName speakerName : container.values()) {
            sumOfScore += speakerName.getScoreCluster();
        }
        for (SpeakerName speakerName : container.values()) {
            speakerName.setScoreCluster(speakerName.getScoreCluster() * speakerName.getScoreCluster() / sumOfScore);
        }
    }

    /**
     * Will go trhough each SpeakerName and compute belief functions
     */
    public void computeBeliefFunctions() throws Exception {
        HashMap<String, Double> finalScores = new HashMap<String, Double>();

        //Going through each possible speaker for this SpeakerNameSet
        //so, for the corresponding Cluster
        for (SpeakerName speakerName : container.values()) {

            //For this speaker, we fetch all the scores coming from the SCT
            ArrayList<Double> scores = speakerName.getScoreClusterList();

            Iterator<Double> it = scores.iterator();

            while (it.hasNext()) {
                Double score = it.next();

                if (finalScores.size() == 0) {
                    finalScores.put(speakerName.getName(), score);
                    finalScores.put("_omega_", 1 - score);
                    continue;
                }

                HashMap<String, Double> scoreHash = new HashMap<String, Double>();
                scoreHash.put(speakerName.getName(), score);
                scoreHash.put("_omega_", 1 - score);

                finalScores = Distance.computeBeliefFunctions(finalScores, scoreHash);

                System.out.print(score + " ");
            }
        }

        for (SpeakerName speakerName : container.values()) {
            if (!finalScores.containsKey(speakerName.getName())) {
                throw new Exception("All items should be in the" +
                        " finalScore Map of belief functions");
            }

            speakerName.setScoreCluster(finalScores.get(speakerName.getName()));
        }


    }

    /**
     * Sort by score.
     */
    protected void sortByScore() {
        //System.err.println("[debug] sortByScore");
        containerByScore = new TreeSet<SpeakerName>(container.values());
        isSorted = true;
    }

    /**
     * Gets the max score.
     *
     * @return a SpeakerName instance
     */
    public SpeakerName getMaxScore() {
        //System.err.println("[debug] getMaxScore");
        if ((containerByScore == null) || (isSorted == false)) {
            sortByScore();
        }
        if (containerByScore.size() == 0) {
            //System.err.println("[debug] return null");
            return null;
        }
        //System.err.println("[debug] return first");
        return containerByScore.first();
    }

    /* (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<String> iterator() {
        // TODO Auto-generated method stub
        return container.keySet().iterator();
    }
}
