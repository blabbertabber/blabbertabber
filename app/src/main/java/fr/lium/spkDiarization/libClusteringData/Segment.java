/**
 * 
 * <p>
 * Segment
 * </p>
 * 
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
 * @author <a href="mailto:gael.salaun@univ-lemans.fr">Gael Salaun</a>
 * @author <a href="mailto:teva.merlin@lium.univ-lemans.fr">Teva Merlin</a>
 * @version v2.0
 * 
 *          Copyright (c) 2007-2009 Universite du Maine. All Rights Reserved. Use is subject to license terms.
 * 
 *          THIS SOFTWARE IS PROVIDED BY THE "UNIVERSITE DU MAINE" AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *          DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *          USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *          ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 *          Segment
 * 
 */

package fr.lium.spkDiarization.libClusteringData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.lium.experimental.spkDiarization.libClusteringData.transcription.EntitySet;
import fr.lium.experimental.spkDiarization.libClusteringData.transcription.LinkSet;
import fr.lium.experimental.spkDiarization.libClusteringData.transcription.Transcription;
import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.SpkDiarizationLogger;

/**
 * The Class Segment.
 */
public class Segment implements Comparable<Segment>, Cloneable, Iterable<Integer> {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(Segment.class.getName());

	/** Type of bandwidth recording conditions. */
	public enum BandwidthType {

		/** The bunk. */
		BUNK,
		/** The tel. */
		TEL,
		/** The studio. */
		STUDIO
	}

	/** Feature rate. */
	public float Rate = 100.0f;

	/** The Constant keyScore. */
	public static final String keyScore = "score";

	/** The Constant keyWords. */
	public static final String keyWords = "words";

	/** The Constant keyChannel. */
	public static final String keyChannel = "channel";

	/** The Constant keyBand. */
	public static final String keyBand = "band";

	/** The Constant keyEnvironment. */
	public static final String keyEnvironment = "env";

	/** The Constant keyGender. */
	public static final String keyGender = "gender";

	/** Number of the bandwidth type. */
	public static final int numberOfBandwidth = 3;

	/**
	 * Array of string corresponding to a bandwidth U : unknown, T : telephone, S : studio.
	 */
	public static final String[] bandwidthStrings = { "U", "T", "S" };

	/**
	 * Array of string corresponding to a bandwidth, Nist version unk : unknown, F2 : telephone, F0 : studio.
	 * */
	public static final String[] bandwidthNistStrings = { "unk", "F2", "F0" };

	/** Number of the Environment type. */
	public static final int numberOfEnvironment = 4;

	/**
	 * Array of string corresponding to a Environment U : unknown, P : Speech, M : music, S : silence.
	 * */
	public static final String[] environmentStrings = { "U", "P", "M", "S" };

	/** name of the show, correspond to the name of the audio file. */
	private String showName; // Name of the show.

	/** index of the first feature. */
	private int startIndex;

	/** length of the segment. */
	private int length;

	/** cluster owner of the segment. */
	private Cluster owner;

	// Link between Segmentation and features
	/** list of top distribution gaussian. */
	private ArrayList<int[]> topGaussienList; // storage for top gaussian

	/** list the used features. */
	private ArrayList<Boolean> speechFeatureList; // storage for used / unused

	/** The used features active. */
	@Deprecated
	private boolean usedFeaturesActive;

	/** store information key->value. */
	private TreeMap<String, Object> informationMap;

	/** store temporary information. */
	@Deprecated
	private Object userData;

	/** Entity Set, list entities present in the Link Set. */
	// private EntitySet entitySet;

	/** Link Set, ie words of the segment */
	// private LinkSet linkSet;

	Transcription transcription;

	/**
	 * Instantiates a new segment.
	 * 
	 * @param showName the show name
	 * @param startIndex the start index
	 * @param lenght the lenght
	 * @param cluster the cluster
	 * @param rate the rate
	 */
	public Segment(String showName, int startIndex, int lenght, Cluster cluster, float rate) {
		this.showName = showName;
		this.startIndex = startIndex;
		this.length = lenght;
		this.owner = cluster;
		this.Rate = rate;
		topGaussienList = new ArrayList<int[]>(1);
		speechFeatureList = new ArrayList<Boolean>(1);
		usedFeaturesActive = false;
		informationMap = new TreeMap<String, Object>();
		userData = null;
		transcription = new Transcription();
		// linkSet = new LinkSet(0);
		// entitySet = new EntitySet();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Segment) {
			Segment other = (Segment) obj;
			if ((other.showName == showName) && (other.startIndex == startIndex) && (other.length == length)) {
				return true;
			} else {
				logger.severe("+ segment no equal showname:" + (other.showName == showName) + " start:"
						+ (other.startIndex == startIndex) + " lenght:" + (other.length == length));
			}
		}
		return false;
	}

	/**
	 * Creates a deep copy of the segment: the content of top is copied, not just referenced.
	 * 
	 * @return the segment
	 */
	@Override
	public Segment clone() {
		Segment result = null;
		try {
			result = (Segment) (super.clone());
		} catch (CloneNotSupportedException e) {
			logger.log(Level.SEVERE, "", e);
			e.printStackTrace();
		}
		result.topGaussienList = new ArrayList<int[]>(topGaussienList.size());
		for (int i = 0; i < topGaussienList.size(); i++) {
			result.topGaussienList.add(topGaussienList.get(i).clone());
		}

		result.speechFeatureList = new ArrayList<Boolean>(speechFeatureList.size());
		for (int i = 0; i < speechFeatureList.size(); i++) {
			result.speechFeatureList.add(speechFeatureList.get(i));
		}

		result.informationMap = new TreeMap<String, Object>();
		for (String key : informationMap.keySet()) {
			/*
			 * Iterator<String> it = information.keySet().iterator(); while (it.hasNext()) { String key = it.next();
			 */
			result.setInformation(key, informationMap.get(key));
		}
		try {
			result.transcription = (Transcription) transcription.clone();
			// result.linkSet = (LinkSet) linkSet.clone();
			// result.entitySet = (EntitySet) entitySet.clone();
		} catch (CloneNotSupportedException e) {
			logger.log(Level.SEVERE, "", e);
			e.printStackTrace();
		}
		if (SpkDiarizationLogger.DEBUG)  logger.info("segment clone:"+result);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
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

	/**
	 * Debug.
	 * 
	 * @param level the level
	 */
	public void debug(int level) {
		logger.fine("seg show= " + getShowName() + " channel= " + getChannel());
		logger.fine(" start= " + getStart() + " len= " + getLength() + " end=" + getLast() + " clusterGender= "
				+ owner.getGender() + " band= " + getBandwidth() + " env= " + getEnvironement() + " clusterName= "
				+ getClusterName() + " " + getInformations());
		if (level > 1) {
			LinkSet linkSet = getLinkSet();
			if (linkSet != null) {
				linkSet.debug();
			}
			EntitySet entitySet = getEntities();
			if (entitySet != null) {
				entitySet.debug();
			}
		}
	}

	/**
	 * Get the bandwidth type.
	 * 
	 * @return the bandwidth
	 */
	public String getBandwidth() {
		String res = (String) informationMap.get(keyBand);
		if (res == null) {
			return bandwidthStrings[0];
		}
		return res;
	}

	/**
	 * Get the channel type.
	 * 
	 * @return the channel
	 */
	public String getChannel() {
		String res = (String) informationMap.get(keyChannel);
		if (res == null) {
			return "1";
		}
		return res;
	}

	/**
	 * Get the cluster.
	 * 
	 * @return the cluster
	 */
	public Cluster getCluster() {
		return owner;

	}

	/**
	 * Get the name of the cluster.
	 * 
	 * @return the cluster name
	 */
	public String getClusterName() {
		return owner.getName();

	}

	/**
	 * Get the environment type.
	 * 
	 * @return the environement
	 */
	public String getEnvironement() {
		String res = (String) informationMap.get(keyEnvironment);
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
		return informationMap;
	}

	/**
	 * Gets the information.
	 * 
	 * @param key the name of the key
	 * 
	 * @return the information as Object
	 */
	public Object getInformation(Object key) {
		return informationMap.get(key);
	}

	/**
	 * Gets the information.
	 * 
	 * @param key the key
	 * @return the information as a string
	 */
	public String getInformation(String key) {
		Object o = informationMap.get(key);
		if (o == null) {
			return "";
		}
		return o.toString();
	}

	/**
	 * Gets the informations.
	 * 
	 * @return the concatenation of informations
	 */
	public String getInformations() {
		String res = "";
		for (String key : informationMap.keySet()) {
			/*
			 * Iterator<String> it = information.keySet().iterator(); while (it.hasNext()) { String key = it.next().toString();
			 */
			if ((key != keyChannel) && (key != keyBand) && (key != keyEnvironment) && (key != keyGender)) {
				res = res + " [ " + key + " = " + informationMap.get(key) + " ]";
			}
		}
		return res;

	}

	/**
	 * Get the last index of the segment.
	 * 
	 * @return the last
	 */
	public int getLast() {
		return (startIndex + length) - 1;
	}

	/**
	 * Get the last index of the segment.
	 * 
	 * @param start the start
	 * @param last the last
	 */
	public void setStartAndLast(int start, int last) {
		setStart(start);
		setLength((last - start) + 1);
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
	 * Gets the end of the segment in second. end = last + 1
	 * 
	 * @return the end in second
	 */
	public float getEndInSecond() {
		return (getLast() + 1) / Rate;
	}

	/**
	 * Get the length of the segment.
	 * 
	 * @return the length
	 */
	public int getLength() {
		return length;
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
		return (Double) informationMap.get(keyScore);
	}

	/**
	 * Get the name of the show.
	 * 
	 * @return the show name
	 */
	public String getShowName() {
		return showName;
	}

	/**
	 * Get the start index of the segment.
	 * 
	 * @return the start
	 */
	public int getStart() {
		return startIndex;
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
	public ArrayList<int[]> getTopGaussianList() {
		return topGaussienList;
	}

	/**
	 * Checks if is used feature.
	 * 
	 * @param absoluteFeatureIndex the idx
	 * 
	 * @return true, if is used feature
	 */
	public boolean isSpeechFeature(int absoluteFeatureIndex) {
		if (SpkDiarizationLogger.DEBUG) logger.info("isSpeechFeature: "+speechFeatureList.size()+" "+this);
		return speechFeatureList.get(absoluteFeatureIndex - startIndex);
	}

	/**
	 * Gets the ArrayList of the used features.
	 * 
	 * @return the used features
	 */
	public ArrayList<Boolean> getSpeechFeatureList() {
		return speechFeatureList;
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
	 * Get the word list.
	 * 
	 * @return the word list
	 */
	public String getWordList() {
		return (String) informationMap.get(keyWords);
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
	 * Removes the information.
	 * 
	 * @param key the key
	 */
	public void removeInformation(Object key) {
		informationMap.remove(key);
	}

	/**
	 * Set band type coming from #strBand.
	 * 
	 * @param c the new bandwidth
	 */
	public void setBandwidth(String c) {
		informationMap.put(keyBand, c);
	}

	/**
	 * Set channel type.
	 * 
	 * @param c the new channel
	 */
	public void setChannel(String c) {
		informationMap.put(keyChannel, c);
	}

	/**
	 * Set the tmp name.
	 * 
	 * @param c the new cluster
	 */
	public void setCluster(Cluster c) {
		owner = c;
	}

	/**
	 * Set Environment type coming from #strEnv.
	 * 
	 * @param c the new environment
	 */
	public void setEnvironment(String c) {
		informationMap.put(keyEnvironment, c);
	}

	/**
	 * public void setGender(String c) { information.put(keyGender, c); }
	 * 
	 * @param key the key
	 * @param value the value
	 */

	public void setInformation(String key, Object value) {
		informationMap.put(key, value);
	}

	/**
	 * Set the length of a segment in number of features.
	 * 
	 * @param c the new length
	 */
	public void setLength(int c) {
		length = c;
	}

	/**
	 * Sets the score of the segment.
	 * 
	 * @param c the new score
	 */
	public void setScore(double c) {
		informationMap.put(keyScore, c);
	}

	/**
	 * Set show name.
	 * 
	 * @param c the new show name
	 */
	public void setShowName(String c) {
		showName = c;
	}

	/**
	 * Set start of the segment,: ie a feature index.
	 * 
	 * @param c the new start
	 */
	public void setStart(int c) {
		startIndex = c;
	}

	/**
	 * Sets the used feature.
	 * 
	 * @param idx the idx
	 * @param value the value
	 */
	public void setSpeechFeature(int idx, boolean value) {
		for (int i = speechFeatureList.size(); i <= length; i++) {
			speechFeatureList.add(true);
		}
		if (idx >= speechFeatureList.size()) {
			if (SpkDiarizationLogger.DEBUG) debug(3);
			logger.warning("size problem :" + speechFeatureList.size() + " idx=" + idx + " length:" + length);
		}
		speechFeatureList.set(idx, value);
	}

	/**
	 * Sets the used features.
	 * 
	 * @param speechFeatureList the new used features
	 */
	public void setSpeechFeatureList(ArrayList<Boolean> speechFeatureList) {
		this.speechFeatureList = speechFeatureList;
	}

	/**
	 * Removes the speech feature list.
	 */
	public void removeSpeechFeatureList() {
		this.speechFeatureList = new ArrayList<Boolean>(1);
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
	 * Sets the user data.
	 * 
	 * @param userData the new user data
	 */
	public void setUserData(Object userData) {
		this.userData = userData;
	}

	/**
	 * Set the word list.
	 * 
	 * @param c the new word list
	 */
	public void setWordList(String c) {
		informationMap.put(keyWords, c);
	}

	/**
	 * Gets the transcription.
	 * 
	 * @return the transcription
	 */
	public Transcription getTranscription() {
		return transcription;
	}

	/**
	 * Sets the transcription.
	 * 
	 * @param transcription the transcription to set
	 */
	public void setTranscription(Transcription transcription) {
		this.transcription = transcription;
	}

	/**
	 * Get the sausage set of the segment.
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
	 * @param entitySet the new entities
	 */
	protected void setEntities(EntitySet entitySet) {
		transcription.setEntitySet(entitySet);
	}

	/**
	 * Gets the rate.
	 * 
	 * @return the rate
	 */

	public float getRate() {
		return Rate;
	}

	/**
	 * Sets the rate.
	 * 
	 * @param rate the rate to set
	 */
	public void setRate(float rate) {
		Rate = rate;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Integer> iterator() {
		return new Iterator<Integer>() {
			protected int index = startIndex;
			protected int end = startIndex + length;

			@Override
			public boolean hasNext() {

				return (index < end);
			}

			@Override
			public Integer next() {
				return index++;
			}

			@Override
			public void remove() {
				try {
					throw new DiarizationException("remove not available");
				} catch (DiarizationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
	}
}
