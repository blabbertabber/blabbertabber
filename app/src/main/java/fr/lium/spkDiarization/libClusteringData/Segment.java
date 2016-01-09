/**
 * <p>
 * Segment
 * </p>
 *
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
 * @author <a href="mailto:gael.salaun@univ-lemans.fr">Gael Salaun</a>
 * @author <a href="mailto:teva.merlin@lium.univ-lemans.fr">Teva Merlin</a>
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
 * <p/>
 * Segment
 */

package fr.lium.spkDiarization.libClusteringData;

import java.util.ArrayList;
import java.util.TreeMap;

import fr.lium.experimental.spkDiarization.libClusteringData.transcription.EntitySet;
import fr.lium.experimental.spkDiarization.libClusteringData.transcription.LinkSet;
import fr.lium.experimental.spkDiarization.libClusteringData.transcription.Transcription;

public class Segment implements Comparable<Segment>, Cloneable {
    /** Feature rate*/
    public final static float Rate = 100.0f;
    public static final String keyScore = "score";
    public static final String keyWords = "words";
    public static final String keyChannel = "channel";
    public static final String keyBand = "band";
    public static final String keyEnvironment = "env";
    public static final String keyGender = "gender";
    /** Number of the bandwidth type */
    public static final int numberOfBandwidth = 3;
    /** Array of string corresponding to a bandwidth
     * U : unknown,
     * T : telephone,
     * S : studio.*/
    public static final String[] bandwidthStrings = {"U", "T", "S"};
    /** Array of string corresponding to a bandwidth, Nist version
     * unk : unknown,
     * F2 : telephone,
     * F0 : studio.
     * */
    public static final String[] bandwidthNistStrings = {"unk", "F2", "F0"};
    /** Number of the Environment type */
    public static final int numberOfEnvironment = 4;
    /** Array of string corresponding to a Environment
     * U : unknown,
     * P : Speech,
     * M : music,
     * S : silence.
     * */
    public static final String[] environmentStrings = {"U", "P", "M", "S"};
    /** Link Set, ie words of the segment */
    //private LinkSet linkSet;

    Transcription transcription;
    /** name of the show, correspond to the name of the audio file */
    private String showName; // Name of the show.

    /** index of the first feature */
    private int startIndex;

    /** length of the segment */
    private int length;

    /** cluster owner of the segment */
    private Cluster owner;

    // Link between Segmentation and features
    /** list of top distribution gaussian */
    private ArrayList<int[]> top; // storage for top gaussian
    /** list the used features */
    @Deprecated
    private ArrayList<Boolean> usedFeatures; // storage for used / unused
    @Deprecated
    private boolean usedFeaturesActive;

    /** store information key->value */
    private TreeMap<String, Object> information;

    /** store temporary information */
    @Deprecated
    private Object userData;

    /** Entity Set, list entities present in the Link Set */
    //private EntitySet entitySet;

    public Segment(String showName, int startIndex, int lenght, Cluster cluster) {
        this.showName = showName;
        this.startIndex = startIndex;
        this.length = lenght;
        this.owner = cluster;
        top = new ArrayList<int[]>();
        usedFeatures = new ArrayList<Boolean>();
        usedFeaturesActive = false;
        information = new TreeMap<String, Object>();
        userData = null;
        transcription = new Transcription();
        //linkSet = new LinkSet(0);
        //entitySet = new EntitySet();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Segment) {
            Segment other = (Segment) obj;
            if ((other.showName == showName) && (other.startIndex == startIndex) && (other.length == length)) {
                return true;
            } else {
                System.err.printf("+ segment no equal showname:" + (other.showName == showName) + " start:" + (other.startIndex == startIndex) + " lenght:"
                        + (other.length == length));
            }
        }
        return false;
    }

    /**
     * Creates a deep copy of the segment: the content of top is copied, not just referenced.
     */
    @Override
    public Object clone() {
        Segment result = null;
        try {
            result = (Segment) (super.clone());
        } catch (CloneNotSupportedException e) {
        }
        result.top = new ArrayList<int[]>(top.size());
        for (int i = 0; i < top.size(); i++) {
            result.top.add(top.get(i).clone());
        }
        result.information = new TreeMap<String, Object>();
        for (String key : information.keySet()) {
            /*
			 * Iterator<String> it = information.keySet().iterator(); while (it.hasNext()) { String key = it.next();
			 */
            result.setInformation(key, information.get(key));
        }
        try {
            result.transcription = (Transcription) transcription.clone();
            //result.linkSet = (LinkSet) linkSet.clone();
            //result.entitySet = (EntitySet) entitySet.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return result;
    }

    public int compareTo(Segment seg) {
        int compare = showName.compareTo(seg.showName);
        if (compare == 0) {
            if (startIndex < seg.startIndex) {
                return -1;
            } else {
                return (startIndex > seg.startIndex) ? 1 : 0;
            }
        }
        return compare;
    }

    public void debug(int level) {
        System.out.print("debug[Segment] \t seg show= " + getShowName());
        System.out.print(" channel= " + getChannel());
        System.out.print(" start= " + getStart() + " len= ");
        System.out.print(getLength() + " end=" + getLast() + " clusterGender= " + owner.getGender());
        System.out.print(" band= " + getBand() + " env= " + getEnv());
        System.out.println(" clusterName= " + getClusterName() + " " + getInformations());
        if (level > 1) {
            LinkSet linkSet = getLinkSet();
            if (linkSet != null) {
                linkSet.debug();
            }
/*			if (getEntities() != null) {
				getEntities().debug();
			}*/
        }
    }

    /**
     * Get the bandwidth type
     */
    public String getBand() {
        String res = (String) information.get(keyBand);
        if (res == null) {
            return bandwidthStrings[0];
        }
        return res;
    }

    /**
     * Set band type coming from #strBand
     */
    public void setBand(String c) {
        information.put(keyBand, c);
    }

    /**
     * Get the channel type
     */
    public String getChannel() {
        String res = (String) information.get(keyChannel);
        if (res == null) {
            return "1";
        }
        return res;
    }

    /**
     * Set channel type
     */
    public void setChannel(String c) {
        information.put(keyChannel, c);
    }

    /**
     * Get the cluster
     */
    public Cluster getCluster() {
        return owner;

    }

    /**
     * Set the tmp name
     */
    public void setCluster(Cluster c) {
        owner = c;
    }

    /**
     * Get the name of the cluster
     */
    public String getClusterName() {
        return owner.getName();

    }

    /**
     * Get the environment type
     */
    public String getEnv() {
        String res = (String) information.get(keyEnvironment);
        if (res == null) {
            return environmentStrings[0];
        }
        return res;
    }

    /**
     * Gets the information, return the TreeMap.
     *
     * @return the information
     */
    public TreeMap<String, Object> getInformation() {
        return information;
    }

    /**
     * Gets the information.
     *
     * @param key the name of the key
     *
     * @return the information as Object
     */
    public Object getInformation(Object key) {
        return information.get(key);
    }

    /**
     * Gets the information.
     *
     * @param key
     * @return the information as a string
     */
    public String getInformation(String key) {
        return information.get(key).toString();
    }

    /**
     * Gets the informations.
     *
     * @return the concatenation of informations
     */
    public String getInformations() {
        String res = "";
        for (String key : information.keySet()) {
			/*
			 * Iterator<String> it = information.keySet().iterator(); while (it.hasNext()) { String key = it.next().toString();
			 */
            if ((key != keyChannel) && (key != keyBand) && (key != keyEnvironment) && (key != keyGender)) {
                res = res + " [ " + key + " = " + information.get(key) + " ]";
            }
        }
        return res;

    }

    /**
     * Get the last index of the segment
     */
    public int getLast() {
        return startIndex + length - 1;
    }

    /**
     * Gets the last in second.
     *
     * @return the last feature in second
     */
    public float getLastInSecond() {
        return (getLast()) / Rate;
    }

    /**
     * Gets the end of the segment in second.
     * end = last + 1
     *
     * @return the end in second
     */
    public float getEndInSecond() {
        return (getLast() + 1) / Rate;
    }

    /**
     * Get the length of the segment
     */
    public int getLength() {
        return length;
    }

    /**
     * Set the length of a segment in number of features
     */
    public void setLength(int c) {
        length = c;
    }

    /**
     * Gets the length in second.
     *
     * @return the length in second
     */
    public float getLengthInSecond() {
        return (length) / Rate;
    }

    /**
     * Gets the score.
     *
     * @return the score of teh segment
     */
    public double getScore() {
        return (Double) information.get(keyScore);
    }

    /**
     * Sets the score of the segment.
     *
     * @param c the new score
     */
    public void setScore(double c) {
        information.put(keyScore, c);
    }

    /**
     * Get the name of the show
     */
    public String getShowName() {
        return showName;
    }

    /**
     * Set show name
     */
    public void setShowName(String c) {
        showName = c;
    }

    /**
     * Get the start index of the segment
     */
    public int getStart() {
        return startIndex;
    }

    /**
     * Set start of the segment, ie a feature index
     */
    public void setStart(int c) {
        startIndex = c;
    }

    /**
     * Gets the start in second.
     *
     * @return the start in second
     */
    public float getStartInSecond() {
        return (startIndex) / Rate;
    }

    /**
     * Gets the arrayList of top gaussians of the segment.
     *
     * @return the top
     */
    public ArrayList<int[]> getTop() {
        return top;
    }

    /**
     * Checks if is used feature.
     *
     * @param idx the idx
     *
     * @return true, if is used feature
     */
    public boolean isUsedFeature(int idx) {
        return usedFeatures.get(idx);
    }

    /**
     * Gets the ArrayList of the used features.
     *
     * @return the used features
     */
    public ArrayList<Boolean> getUsedFeatures() {
        return usedFeatures;
    }

    /**
     * Sets the used features.
     *
     * @param usedFeatures the new used features
     */
    public void setUsedFeatures(ArrayList<Boolean> usedFeatures) {
        this.usedFeatures = usedFeatures;
    }

    /**
     * Gets the user data.
     *
     * @return the user data
     */
    public Object getUserData() {
        return userData;
    }

    /**
     * Sets the user data.
     *
     * @param userData the new user data
     */
    public void setUserData(Object userData) {
        this.userData = userData;
    }

    /**
     * Get the word list
     */
    public String getWordList() {
        return (String) information.get(keyWords);
    }

    /**
     * Set the word list
     */
    public void setWordList(String c) {
        information.put(keyWords, c);
    }

    /**
     * Checks if is used features active.
     *
     * @return true, if is used features active
     */
    public boolean isUsedFeaturesActive() {
        return usedFeaturesActive;
    }

    /**
     * Sets the used features active.
     *
     * @param usedFeaturesActive the new used features active
     */
    public void setUsedFeaturesActive(boolean usedFeaturesActive) {
        this.usedFeaturesActive = usedFeaturesActive;
    }

    /**
     * Removes the information.
     *
     * @param key the key
     */
    public void removeInformation(Object key) {
        information.remove(key);
    }

    /**
     * Set Environment type coming from #strEnv
     */
    public void setEnvironment(String c) {
        information.put(keyEnvironment, c);
    }

    /**
     * public void setGender(String c) { information.put(keyGender, c); }
     **/

    public void setInformation(String key, Object value) {
        information.put(key, value);
    }

    /**
     * Sets the used feature.
     *
     * @param idx the idx
     * @param value the value
     */
    public void setUsedFeature(int idx, boolean value) {
        for (int i = usedFeatures.size(); i <= idx; i++) {
            usedFeatures.add(true);
        }
        usedFeatures.set(idx, value);
    }

    /**
     * @return the transcription
     */
    public Transcription getTranscription() {
        return transcription;
    }

    /**
     * @param transcription the transcription to set
     */
    public void setTranscription(Transcription transcription) {
        this.transcription = transcription;
    }

    /**
     * Get the sausage set of the segment
     *
     * @return FSMSausageSet
     */
    protected LinkSet getLinkSet() {
        return transcription.getLinkSet();
    }

    /**
     * Sets the link set.
     *
     * @param linkSet the new link set
     */
    protected void setLinkSet(LinkSet linkSet) {
        transcription.setLinkSet(linkSet);
    }

    /**
     * Gets the entities.
     *
     * @return the entities
     */
    protected EntitySet getEntities() {
        return transcription.getEntitySet();
    }

    /**
     * Sets the entities.
     *
     * @param entities the new entities
     */
    protected void setEntities(EntitySet entitySet) {
        transcription.setEntitySet(entitySet);
    }

    /** Type of bandwidth recording conditions.*/
    public enum BandwidthType {
        BUNK, TEL, STUDIO
    }
}
