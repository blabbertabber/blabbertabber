/**
 * 
 * <p>
 * Cluster
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
 * 
 */

package fr.lium.spkDiarization.libClusteringData;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.lium.experimental.spkDiarization.libClusteringData.speakerName.SpeakerName;
import fr.lium.experimental.spkDiarization.libClusteringData.speakerName.SpeakerNameSet;
import fr.lium.experimental.spkDiarization.libClusteringData.transcription.Entity;
import fr.lium.experimental.spkDiarization.libClusteringData.transcription.EntitySet;
import fr.lium.experimental.spkDiarization.libClusteringData.transcription.Link;
import fr.lium.experimental.spkDiarization.libClusteringData.transcription.LinkSet;
import fr.lium.experimental.spkDiarization.libNamedSpeaker.SpeakerNameUtils;
import fr.lium.spkDiarization.lib.SpkDiarizationLogger;
import fr.lium.spkDiarization.libModel.ModelScores;

/**
 * Container for the storage of segments. A cluster generally corresponds to speaker.
 */
public class Cluster implements Comparable<Cluster>, Cloneable, Iterable<Segment> {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(Cluster.class.getName());

	/** The segment set. */
	protected TreeSet<Segment> segmentSet; // sorted list (a set) of segments, sorted by start time

	/** gender of the speaker. */
	protected String gender;

	/** bandwidth of the speaker. */
	protected String bandwidth;

	/** channel of the audio, set to 1, the first. */
	protected String channel;

	// protected double score;

	/** output name of the cluster/speaker, key in ClusterSet. */
	protected String name;

	/** Universal information storage Map. */
	private TreeMap<String, Object> informationMap;

	/** Real name of speaker storage. */
	private SpeakerNameSet speakerNameSet;

	/** user data information storage. */
	private Object userData;

	/** user data information storage 2. */
	private Object sortedUserData;

	/** number of gender. */
	public static final int numberOfGenders = 4;

	/** Array of string corresponding to a Gender enum. */
	public static final String[] genderStrings = { "U", "M", "F", "C" };

	/** The model scores. */
	protected ModelScores modelScores;

	/**
	 * The Class StringComparator.
	 */
	private class StringComparator implements Comparator<String> {

		/*
		 * (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(String o1, String o2) {
			return o1.compareTo(o2);
		}
	}

	/**
	 * The Constructor.
	 * 
	 * @param _name of the cluster
	 */
	public Cluster(String _name) {
		gender = genderStrings[0];
		name = _name;
		segmentSet = new TreeSet<Segment>();
		informationMap = new TreeMap<String, Object>(new StringComparator());
		bandwidth = Segment.bandwidthStrings[0];
		speakerNameSet = new SpeakerNameSet();
		modelScores = new ModelScores();
	}

	/**
	 * Gets the information.
	 * 
	 * @return the information TreeMap
	 */
	public TreeMap<String, Object> getInformation() {
		return informationMap;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Cluster) {
			Cluster other = (Cluster) obj;
			boolean segEqual = other.segmentSet.equals(segmentSet);
			if ((segEqual == true) && (other.gender == gender) && (other.channel == channel) && (other.name == name)
					&& (other.bandwidth == bandwidth)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets the speaker name set.
	 * 
	 * @return the Speaker Name set
	 */
	public SpeakerNameSet getSpeakerNameSet() {
		return speakerNameSet;
	}

	/**
	 * Clear speaker name set.
	 */
	public void clearSpeakerNameSet() {
		speakerNameSet.clear();
	}

	/**
	 * Gets the speaker name.
	 * 
	 * @param name the name of the real speaker name
	 * 
	 * @return the SpeakerName instance
	 */
	public SpeakerName getSpeakerName(String name) {
		return speakerNameSet.get(name);
	}

	/**
	 * Normalize the scoreCluster of speaker name set.
	 */
	public void computeNormalizedScore() {
		speakerNameSet.normalizeScoreCluster();
	}

	/**
	 * Mean the scoreCluster of speaker name set.
	 */
	public void computeMeanScore() {
		speakerNameSet.meanScoreCluster();
	}

	/**
	 * Compute belief functions of the scoreCluster of speaker name set.
	 * 
	 * @throws Exception the exception
	 */
	public void computeBeliefFunctions() throws Exception {
		speakerNameSet.computeBeliefFunctions();
	}

	/**
	 * Compute sum.
	 * 
	 * @throws Exception the exception
	 */
	public void computeSum() throws Exception {
		speakerNameSet.computeSum();
	}

	/**
	 * Removes a SpeakerSame instance according the given real speaker name.
	 * 
	 * @param name the real speaker name
	 */
	public void RemoveSpeakerName(String name) {
		speakerNameSet.remove(name);
	}

	/**
	 * Gets the maximum scoreCluster for the given real speaker name.
	 * 
	 * @return the maximum scoreCluster for the given real speaker name.
	 */
	public SpeakerName getMaxSpeakerName() {
		return speakerNameSet.getMaxScore();
	}

	/**
	 * Adds a segment to the inner container.
	 * 
	 * @param segment the segment to add
	 * 
	 * @return true, if add successful
	 */
	public boolean addSegment(Segment segment) {
		segment.setCluster(this);
		return segmentSet.add(segment);
	}

	/**
	 * Adds the segments according an iterator.
	 * 
	 * @param itSegment the iterator segment
	 */
	public void addSegments(Iterator<Segment> itSegment) {
		while (itSegment.hasNext()) {
			addSegment(itSegment.next());
		}
	}

	/**
	 * Clear segment container.
	 */
	public void clearSegments() {
		segmentSet.clear();
	}

	/**
	 * Removes the given segment.
	 * 
	 * @param segment the segment to remove
	 */
	public void removeSegment(Segment segment) {
		segmentSet.remove(segment);
	}

	/**
	 * Creates a deep copy of the cluster: segments in the new cluster are copies of the original segments, not references.
	 * 
	 * @return the object
	 */
	@Override
	public Cluster clone() {
		Cluster result = null;
		try {
			result = (Cluster) (super.clone());
		} catch (CloneNotSupportedException e) {
			logger.log(Level.SEVERE, "", e);
			e.printStackTrace();
		}
		result.segmentSet = new TreeSet<Segment>();
		for (Segment segment : segmentSet) {
			result.segmentSet.add((segment.clone()));
		}
		result.informationMap = new TreeMap<String, Object>(new StringComparator());
		for (String key : informationMap.keySet()) {
			result.setInformation(key, informationMap.get(key));
		}

		result.modelScores = new ModelScores();
		for (String key : modelScores.keySet()) {
			result.modelScores.put(key, modelScores.get(key));
		}

		result.speakerNameSet = (SpeakerNameSet) speakerNameSet.clone();
		
		
		return result;
	}

	/**
	 * Cluster to frames convertor.
	 * 
	 * @return the tree map< integer, segment> containing the segment sorted by show first and start time
	 */
	public TreeMap<Integer, Segment> clusterToFrames() {
		TreeMap<Integer, Segment> segmentTreeMapResult = new TreeMap<Integer, Segment>();
		TreeSet<Segment> segmentList = getSegments();
		for (Segment segment : segmentList) {
			int start = segment.getStart();
			int length = segment.getLength();
			for (int i = start; i < (start + length); i++) {
				Segment newSegment = (segment.clone());
				newSegment.setStart(i);
				newSegment.setLength(1);
				newSegment.setCluster(this);
				// Plus utile le genre porte sur le cluster
				// newSegment.setGender(getGender());
				segmentTreeMapResult.put(i, newSegment);
			}
		}
		return segmentTreeMapResult;
	}

	/**
	 * Collapse the near segments.
	 * 
	 * @param delay the possible delay between segment
	 * @return the array list
	 */
	public ArrayList<Integer> collapse(int delay) {
		Iterator<Segment> segmentIterator = iterator();
		ArrayList<Integer> list = new ArrayList<Integer>();
		Segment previous, current;
		if (segmentIterator.hasNext()) {
			previous = segmentIterator.next();
			while (segmentIterator.hasNext()) {
				current = segmentIterator.next();
				if (current.getShowName().compareTo(previous.getShowName()) == 0) {
					int previousStart = previous.getStart();
					int previousEnd = previousStart + previous.getLength();
					int currentStart = current.getStart();
					int currentLength = current.getLength();
					list.add(currentLength);
					if ((previousEnd + delay) >= currentStart) {
						previous.setLength((currentStart - previousStart) + currentLength);
						LinkSet linkSetSave = previous.getLinkSet();
						EntitySet entitiesSave = previous.getEntities();
						previous.getInformation().putAll(current.getInformation());
						previous.setLinkSet(linkSetSave);
						previous.setEntities(entitiesSave);
						int sizeLink = previous.getLinkSet().size();
						for (Link link : current.getLinkSet()) {
							link.setId(sizeLink++);
							previous.getLinkSet().add(link);
						}
						for (Entity entity : current.getEntities()) {
							previous.getEntities().add(entity);
						}
						segmentIterator.remove();
					} else {
						previous = current;
					}
				} else {
					previous = current;
				}
			}
		}
		return list;
	}

	/**
	 * Collapse segments.
	 * 
	 * @return the array list
	 */
	public ArrayList<Integer> collapse() {
		return collapse(0);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Cluster aCluster) {
		// Cluster cluster = aCluster;
		if (segmentSet.isEmpty()) {
			return -1;
		}
		if (aCluster.segmentSet.isEmpty()) {
			return -1;
		}

		return (segmentSet.first().compareTo(aCluster.segmentSet.first()));
	}

	/**
	 * Information of the cluster.
	 * 
	 * @param level the level
	 */
	public void debug(int level) {
		logger.fine("name= " + getName() + " gender= " + getGender() + " channel= " + getChannel() + " Length= "
				+ getLength() + " info= " + getInformations());
		speakerNameSet.debug();
		for (Segment segment : segmentSet) {
			segment.debug(level);
		}
	}

	/**
	 * Debug speaker name.
	 */
	public void debugSpeakerName() {
		for (String name : getSpeakerNameSet()) {
			SpeakerName speakerName = getSpeakerName(name);
			if (SpkDiarizationLogger.DEBUG) logger.fine("name= " + getName() + " -- > "+name+" = "+speakerName.getScore() + " / "+getLength()/100.0);
			speakerName.debug();
		}

	}

	/**
	 * First segment of the container.
	 * 
	 * @return the fist segment
	 */
	public Segment firstSegment() {
		return segmentSet.first();
	}

	/**
	 * Gets the bandwidth of the culster.
	 * 
	 * @return the bandwidth
	 */
	public String getBandwidth() {
		return bandwidth;
	}

	/**
	 * Gets the channel of the cluster.
	 * 
	 * @return the channel
	 */
	public String getChannel() {
		return channel;
	}

	/**
	 * Get the gender.
	 * 
	 * @return the gender
	 * 
	 * @see #genderStrings
	 */
	public String getGender() {
		return this.gender;
	}

	/**
	 * Gets the information.
	 * 
	 * @param key the key in information Map
	 * 
	 * @return the information
	 */
	public String getInformation(String key) {
		if (informationMap.get(key) == null) {
			return "";
		}
		return informationMap.get(key).toString();
	}

	/**
	 * Gets the information Map instance.
	 * 
	 * @return the informations
	 */
	public String getInformations() {
		String result = "";
		for (String key : informationMap.keySet()) {
			result = result + " [ " + key + " = " + informationMap.get(key) + " ]";
		}
		return result;
	}

	/**
	 * Gets the sum of the segment lengths.
	 * 
	 * @return the length sum
	 */
	public int getLength() {
		int length = 0;
		for (Segment segment : segmentSet) {
			length += segment.getLength();
		}
		return length;
	}

	/**
	 * Get the cluster name.
	 * 
	 * @return the name of the cluster
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Get the container Useful to add and delete segments.
	 * 
	 * @return the container, ie the list of segments
	 */
	private TreeSet<Segment> getSegments() {
		return this.segmentSet;
	}

	/**
	 * Gets the user data Object instance.
	 * 
	 * @return the user data
	 */
	public Object getUserData() {
		return userData;
	}

	/**
	 * Gets the sorted user data Object.
	 * 
	 * @return the sorted user data
	 */
	public Object getSortedUserData() {
		return sortedUserData;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Segment> iterator() {
		return segmentSet.iterator();
	}

	/**
	 * Last segment.
	 * 
	 * @return the last segment in the segment container
	 */
	public Segment LastSegment() {
		return segmentSet.last();
	}

	/**
	 * Removes the information given by the key.
	 * 
	 * @param key the key
	 */
	public void removeInformation(Object key) {
		informationMap.remove(key);
	}

	/**
	 * Segments size.
	 * 
	 * @return the number of the segment in the container
	 */
	public int segmentsSize() {
		return segmentSet.size();
	}

	/**
	 * Sets the bandwidth value.
	 * 
	 * @param bandwidth the new bandwidth
	 */
	public void setBandwidth(String bandwidth) {
		this.bandwidth = bandwidth;
	}

	/**
	 * Sets the channel of the cluster.
	 * 
	 * @param chanel the new channel
	 */
	public void setChannel(String chanel) {
		this.channel = chanel;
	}

	/**
	 * Set the gender of the cluster.
	 * 
	 * @param c the gender
	 */
	public void setGender(String c) {
		this.gender = c;
	}

	/**
	 * Puts Information (key, information).
	 * 
	 * @param key the key
	 * @param value the value
	 */
	public void setInformation(String key, Object value) {
		informationMap.put(key, value);
	}

	/**
	 * Put information (key, information).
	 * 
	 * @param key the key
	 * @param value the value
	 */
	public void setInformation(String key, String value) {
		informationMap.put(key, value);
	}

	/**
	 * Set the cluster name.
	 * 
	 * @param newName the name of the cluster
	 */
	public void setName(String newName) {
		this.name = newName;
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
	 * Sets the sorted user data.
	 * 
	 * @param sortedUserData the new sorted user data
	 */
	public void setSortedUserData(Object sortedUserData) {
		this.sortedUserData = sortedUserData;
	}

	/**
	 * Write the cluster and the segments to a \e *FILE.
	 * 
	 * @param dos the DataOutPutstream to write to
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeAsSeg(OutputStreamWriter dos) throws IOException {
		Set<Entry<String, Object>> set = getInformation().entrySet();
		String info = "";
		for (Entry<String, Object> entry : set) {
			info += "[ " + entry.getKey() + " = " + entry.getValue().toString() + " ] ";
		}
		dos.write(";; cluster " + name + " " + info + "\n");
		String line;
		/*
		 * String line = getInformations(); if (line != "") { line = ";; cluster:" + name + getInformations() + "\n"; dos.write(line); }
		 */
		for (Segment segment : segmentSet) {
			line = segment.getShowName() + " " + segment.getChannel() + " " + segment.getStart();
			line += " " + segment.getLength() + " " + gender + " " + segment.getBandwidth();
			line += " " + segment.getEnvironement() + " " + name + segment.getInformations() + "\n";
			if (segment.getLength() > 0) {
				dos.write(line);
			}
		}
	}

	/**
	 * Write the cluster and the segments to a \e *FILE.
	 * 
	 * @param dos the DataOutPutstream to write to
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeAsXSeg(OutputStreamWriter dos) throws IOException {
		String line = getInformations();
		if (line != "") {
			line = "audio cluster " + name + " " + getInformations() + "\n";
			dos.write(line);
		}
		for (Segment segment : segmentSet) {
			line = "audio segment " + segment.getShowName() + " " + segment.getChannel() + " " + segment.getStart();
			line += " " + segment.getLength() + " " + gender + " " + segment.getBandwidth();
			line += " " + segment.getEnvironement() + " " + name + segment.getInformations() + "\n";
			dos.write(line);
		}
	}

	/**
	 * Write the cluster and the segments to a CTL \e *FILE.
	 * 
	 * @param dos the DataOutPutstream to write to
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeAsCTL(OutputStreamWriter dos) throws IOException {
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setDecimalSeparator('.');
		DecimalFormat df = new DecimalFormat();
		df.setDecimalFormatSymbols(dfs);
		df.setMaximumFractionDigits(2);
		df.setMinimumFractionDigits(2);
		df.setGroupingUsed(false);

		for (Segment seg : segmentSet) {
			float start = seg.getStartInSecond();
			float end = seg.getEndInSecond();
			String band = Segment.bandwidthNistStrings[0];
			if (seg.getBandwidth() == Segment.bandwidthStrings[1]) {
				band = Segment.bandwidthNistStrings[1];
			} else if (seg.getBandwidth() == Segment.bandwidthStrings[2]) {
				band = Segment.bandwidthNistStrings[2];
			}

			String line = seg.getShowName() + " " + seg.getStart() + " " + (seg.getStart() + seg.getLength());
			line += " " + seg.getShowName() + "-" + df.format(start) + "-" + df.format(end) + "-" + band;
			line += "-" + this.gender + "-" + name + "\n";
			dos.write(line);
		}
	}

	/**
	 * Gets the model scores.
	 * 
	 * @return the modelScores
	 */
	public ModelScores getModelScores() {
		return modelScores;
	}

	/**
	 * Sets the model scores.
	 * 
	 * @param modelScores the modelScores to set
	 */
	public void setModelScores(ModelScores modelScores) {
		this.modelScores = modelScores;
	}

	/**
	 * Write as eger.
	 * 
	 * @param dos the dos
	 * @param type the type
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeAsEGER(OutputStreamWriter dos, String type) throws IOException {
		String line;
		String nameEN;
		for (Segment segment : segmentSet) {
			if (type.equals("writing")) {
				EntitySet entitySet = segment.getTranscription().getEntitySet();
				for (Entity entity : entitySet) {
// logger.info("WRITING entity : "+entity.getListOfWords());
					if (entity.isPerson()) {
						nameEN = SpeakerNameUtils.normalizeSpeakerName(entity.getListOfWords());
						line = segment.getShowName();
						line += " " + segment.getStartInSecond();
						line += " " + segment.getLastInSecond();
						line += " written";
						line += " " + nameEN + "\n";
						if (segment.getLength() > 0) {
							dos.write(line);
						}
					}
				}

			} else {
				line = segment.getShowName();
				line += " " + segment.getStartInSecond();
				line += " " + segment.getLastInSecond();
				line += " " + type;
				line += " " + name + "\n";
				if (segment.getLength() > 0) {
					dos.write(line);
				}
			}
		}
	}

}
