/**
 * 
 * <p>
 * Clusters
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
 */

package fr.lium.spkDiarization.libClusteringData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import fr.lium.experimental.EPAC.xml.XmlEPACInputOutput;
import fr.lium.experimental.MEDIA.xml.XmlMEDIAInputOutput;
import fr.lium.experimental.REPERE.xml.XmlREPEREInputOutput;
import fr.lium.experimental.spkDiarization.libClusteringData.speakerName.SpeakerName;
import fr.lium.experimental.spkDiarization.libClusteringData.transcription.Entity;
import fr.lium.experimental.spkDiarization.libClusteringData.transcription.EntitySet;
import fr.lium.experimental.spkDiarization.libClusteringData.transcription.Link;
import fr.lium.experimental.spkDiarization.libClusteringData.transcription.LinkSet;
import fr.lium.experimental.spkDiarization.libClusteringData.turnRepresentation.Turn;
import fr.lium.experimental.spkDiarization.libClusteringData.turnRepresentation.TurnSet;
import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.IOFile;
import fr.lium.spkDiarization.lib.SpkDiarizationLogger;
import fr.lium.spkDiarization.libModel.ModelScores;
import fr.lium.spkDiarization.parameter.ParameterSegmentationFile;
import fr.lium.spkDiarization.parameter.ParameterSegmentationFile.SegmentationFormat;

/**
 * Container for the storage of clusters. A clusters corresponds to a segmentation. This is a container is a map.
 */
public class ClusterSet implements Cloneable, Iterable<String> {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(ClusterSet.class.getName());

	/** container of the clusters. */
	protected TreeMap<String, Cluster> clusterMap;

	/** The head cluster set. */
	protected ClusterSet headClusterSet;

	/** The writing. */
	protected Cluster writing;

	/** The type. */
	protected String type = "speaker";

	/** Universal information storage Map. */
	private TreeMap<String, Object> informationMap;

	/**
	 * Instantiates a new cluster set.
	 */
	public ClusterSet() {
		clusterMap = new TreeMap<String, Cluster>();
		informationMap = new TreeMap<String, Object>();
		writing = null;
		headClusterSet = null;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ClusterSet) {
			ClusterSet other = (ClusterSet) obj;
			if (clusterGetSize() != other.clusterGetSize()) {
				return false;
			}
			for (String nameOther : other) {
				if (containsCluster(nameOther)) {
					Cluster otherCluster = other.getCluster(nameOther);
					Cluster cluster = getCluster(nameOther);
					if (cluster.equals(otherCluster) == false) {
						return false;
					}
				} else {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Sets the information.
	 * 
	 * @param key the key
	 * @param value the value
	 */
	public void setInformation(String key, Object value) {
		informationMap.put(key, value);
	}

	/**
	 * Sets the information.
	 * 
	 * @param key the key
	 * @param value the value
	 */
	public void setInformation(String key, String value) {
		informationMap.put(key, value);
	}

	/**
	 * Removes the information according the key.
	 * 
	 * @param key the key
	 */
	public void removeInformation(Object key) {
		informationMap.remove(key);
	}

	/**
	 * Gets the information indexed by the key.
	 * 
	 * @return the information tree map
	 */
	public TreeMap<String, Object> getInformation() {
		return informationMap;
	}

	/**
	 * Gets the information indexed by the key.
	 * 
	 * @param key the key
	 * 
	 * @return the information
	 */
	public Object getInformation(String key) {
		return informationMap.get(key);
	}

	/**
	 * Gets the Informations container.
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
	 * Adds the segment of the vector container .
	 * 
	 * @param vSeg the v seg
	 */
	public void addVector(ArrayList<Segment> vSeg) {
		// clearClusters();
		for (Segment segment : vSeg) {
			String name = segment.getClusterName();
			Cluster cluster = getOrCreateANewCluster(name);
			// problem if segments have different Gender for this cluster
			// A verifier rajout du getCluster()
			cluster.setGender(segment.getCluster().getGender());
			// problem if segments have different Band for this cluster
			cluster.setBandwidth(segment.getBandwidth());
			cluster.setChannel(segment.getBandwidth());
			cluster.addSegment(segment);
		}
	}

	/**
	 * Clear clusters.
	 */
	public void clearClusters() {
		clusterMap.clear();
	}

	/**
	 * Creates a deep copy of the cluster set: clusters in the new set are copies of the original clusters, not references.
	 * 
	 * @return the object
	 */
	@Override
	public ClusterSet clone() {
		ClusterSet result = null;
		try {
			result = (ClusterSet) (super.clone());
		} catch (CloneNotSupportedException e) {
			logger.log(Level.SEVERE, "", e);
			e.printStackTrace();
		}
		result.clusterMap = new TreeMap<String, Cluster>();
		for (String key : clusterMap.keySet()) {
			result.clusterMap.put(key, (clusterMap.get(key).clone()));
		}
		if (headClusterSet != null) {
			result.headClusterSet = headClusterSet.clone();
		}
		if (writing != null) {
			result.writing = writing.clone();
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<String> iterator() {
		return clusterMap.keySet().iterator();
	}

	/**
	 * Gets the size of the cluster container.
	 * 
	 * @return the int
	 */
	public int clusterGetSize() {
		return clusterMap.values().size();
	}

	/**
	 * Cluster set value iterator.
	 * 
	 * @return the iterator< cluster>
	 */
	public Iterator<Cluster> clusterSetValueIterator() {
		return clusterMap.values().iterator();
	}

	/**
	 * Cluster set value.
	 * 
	 * @return the collection< cluster>
	 */
	public Collection<Cluster> clusterSetValue() {
		return clusterMap.values();
	}

	/**
	 * Gets the first cluster.
	 * 
	 * @return the first cluster
	 */
	public Cluster getFirstCluster() {
		return clusterMap.values().iterator().next();
	}

	/**
	 * Gets the first cluster name.
	 * 
	 * @return the first cluster name
	 */
	public String getFirstClusterName() {
		return clusterMap.keySet().iterator().next();
	}

	/**
	 * Collapse segments form a cluster that are contiguous.
	 * 
	 * @return the array list
	 * @see Cluster#collapse()
	 */
	public ArrayList<Integer> collapse() {
		ArrayList<Integer> list = new ArrayList<Integer>();

		for (Cluster cluster : clusterMap.values()) {
			list.addAll(cluster.collapse(0));
		}
		return list;
	}

	/**
	 * Collapse segments form a cluster that are contiguous.
	 * 
	 * @param delay the delay
	 * @return the linked list
	 * @see Cluster#collapse()
	 */
	public LinkedList<Integer> collapse(int delay) {
		LinkedList<Integer> list = new LinkedList<Integer>();

		/*
		 * for (Cluster cluster : clusterMap.values()) { list.addAll(cluster.collapse(delay)); } return list;
		 */
		TurnSet turnSet = getTurns();
		for (Turn turn : turnSet) {
			Iterator<Segment> segmentIterator = turn.iterator();
			Segment previous, current;
			if (segmentIterator.hasNext()) {
				previous = segmentIterator.next();
				list.add(previous.getLength());
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
							Cluster cluster = current.getCluster();
							cluster.removeSegment(current);
							segmentIterator.remove();
						} else {
							previous = current;
						}
					} else {
						previous = current;
					}
				}
			}
		}
		return list;
	}

	/**
	 * Contains cluster.
	 * 
	 * @param key the key
	 * 
	 * @return true, if successful
	 */
	public boolean containsCluster(String key) {
		return clusterMap.containsKey(key);
	}

	/**
	 * Debug.
	 * 
	 * @param level the level
	 */
	public void debug(int level) {
		for (Cluster cluster : clusterMap.values()) {
			cluster.debug(level);
		}
	}

	/**
	 * Gets the cluster according the key.
	 * 
	 * @param key the key
	 * 
	 * @return the cluster
	 */
	public Cluster getCluster(String key) {
		return clusterMap.get(key);
	}

	/**
	 * Put cluster.
	 * 
	 * @param key the key
	 * @param value the value
	 * 
	 * @return the cluster
	 */
	public Cluster putCluster(String key, Cluster value) {
		return clusterMap.put(key, value);
	}

	/**
	 * Gets the clusters.
	 * 
	 * @return the clusters
	 */
	private TreeMap<String, Cluster> getClusters() {
		return this.clusterMap;
	}

	/**
	 * Gets the length.of all clusters
	 * 
	 * @return the length
	 */
	public int getLength() {
		int length = 0;
		for (Cluster cluster : clusterMap.values()) {
			length += cluster.getLength();
		}
		return length;
	}

	/**
	 * Gets the or create a new cluster.
	 * 
	 * @param key the name of the cluster
	 * 
	 * @return the or new cluster
	 */
	public Cluster getOrCreateANewCluster(String key) {
		Cluster cluster = clusterMap.get(key);
		if (cluster == null) {
			return createANewCluster(key);
		}
		return cluster;
	}

	/**
	 * Construct a SegLst in which all segments of #map are copied.
	 * 
	 * @return All the segments of the container
	 */
	public TreeSet<Segment> getSegments() {
		TreeSet<Segment> result = new TreeSet<Segment>();
		for (Cluster cluster : getClusters().values()) {
			for (Segment segment : cluster) {
				result.add(segment);
			}
		}
		return result;
	}

	/**
	 * Gets the list of the show.
	 * 
	 * @return the show name list
	 */
	public TreeSet<String> getShowNames() {
		TreeSet<String> result = new TreeSet<String>();
		for (Cluster cluster : getClusters().values()) {
			for (Segment segment : cluster) {
				result.add(segment.getShowName());
			}
		}
		return result;
	}

	/**
	 * The Class ReversSegmentComparator.
	 */
	public class ReversSegmentComparator implements Comparator<Segment> {

		/*
		 * (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(Segment o1, Segment o2) {
			return -1 * o1.compareTo(o2);
		}

	}

	/**
	 * Gets the segment TreeSet in a reverse order.
	 * 
	 * @return the segments reverse order
	 */
	public TreeSet<Segment> getSegmentsReverseOrder() {

		TreeSet<Segment> result = new TreeSet<Segment>(new ReversSegmentComparator());
		for (Cluster cluster : getClusters().values()) {
			for (Segment segment : cluster) {
				result.add(segment);
			}
		}
		return result;
	}

	/**
	 * Gets a turn set instance.
	 * 
	 * @return the turns
	 */
	public TurnSet getTurns() {
		TurnSet turns = new TurnSet();
		Cluster oldCluster = null;
		Turn currentTurn = null;
		for (Segment segment : getSegments()) {
			Cluster currentCluster = segment.getCluster();
			if (currentCluster != oldCluster) {
				currentTurn = new Turn();
				turns.add(currentTurn);
			}
			currentTurn.add(segment);
			oldCluster = currentCluster;
		}

		return turns;
	}

	// TODO to be remove
	/**
	 * Gets the vector representation of the clustering.
	 * 
	 * @return the vector representation
	 */
	public ArrayList<Segment> getSegmentVectorRepresentation() {
		TreeSet<Segment> segmentList = getSegments();
		ArrayList<Segment> segmentVector = new ArrayList<Segment>();
		for (Segment segment : segmentList) {
			segmentVector.add(segment);
		}
		return segmentVector;
	}

	/**
	 * Gets the cluster vector representation.
	 * 
	 * @return the cluster vector representation
	 */
	public ArrayList<Cluster> getClusterVectorRepresentation() {
		ArrayList<Cluster> vector = new ArrayList<Cluster>();
		for (String key : clusterMap.keySet()) {
			vector.add(getCluster(key));
		}
		return vector;
	}

	/**
	 * Merge two clusters: put the segments of the cluster name keyJ in the cluster name keyI.
	 * 
	 * @param keySource the name of the first cluster
	 * @param keyDestination the name of the second cluster
	 */
	public void mergeCluster(String keyDestination, String keySource) {
		Cluster clusterSource = getCluster(keySource);
		Cluster clusterDestination = getCluster(keyDestination);
		
		logger.info("--> MERGE: " + keyDestination + " in " + keySource);
		clusterDestination.addSegments(clusterSource.iterator());
		ModelScores modelScoresI = clusterDestination.getModelScores();
		ModelScores modelScoresJ = clusterSource.getModelScores();

		for (String name : modelScoresJ.keySet()) {
			if (modelScoresI.get(name) == null) {
				modelScoresI.put(name, modelScoresJ.get(name));
				if (SpkDiarizationLogger.DEBUG) logger.info("model score put name:" + name + " score=" + modelScoresJ.get(name));
			} else {
				double valueI = modelScoresI.get(name);
				double valueJ = modelScoresJ.get(name);
				double newScore = (valueI * valueJ) + (valueI * (1.0 - valueJ)) + ((1.0 - valueI) * valueJ);
				modelScoresI.put(name, newScore);
				if (SpkDiarizationLogger.DEBUG) logger.info("model score merge name:" + name + " s1=" + valueI + " s2=" + valueJ + " new=" + newScore);
			}
		}
		
		for(String name: clusterSource.getSpeakerNameSet()) {
			clusterDestination.getSpeakerNameSet().put(clusterSource.getSpeakerName(name));
		}
		//clusterDestination.getSpeakerNameSet().debug();
		removeCluster(keySource);
	}

	/**
	 * Merge two clusters: put the segments of the cluster name keyJ in the cluster name keyI.
	 * 
	 * @param type the type
	 * @param keyI the name of the first cluster
	 * @param keyJ the name of the second cluster
	 * @param mergeNumber the merge number
	 * @param score the score
	 */
	public void mergeClusterAndAddInformation(String type, String keyI, String keyJ, int mergeNumber, double score) {
		clusterMap.get(keyI).addSegments(clusterMap.get(keyJ).iterator());
		for (String key : clusterMap.get(keyJ).getInformation().keySet()) {
			if (key.contains("merge_")) {
				clusterMap.get(keyI).getInformation().put(key, clusterMap.get(keyJ).getInformation().get(key));
			}
		}
		clusterMap.get(keyI).getInformation().put("merge " + type + " " + mergeNumber, keyI + " in " + keyJ + " with "
				+ score);
		removeCluster(keyJ);
	}

	/**
	 * Create a New cluster with key as name.
	 * 
	 * @param key the key
	 * 
	 * @return the cluster
	 */
	public Cluster createANewCluster(String key) {
		if ((SpkDiarizationLogger.DEBUG) && (containsCluster(key))) {
			logger.finer("addCluster : cluster exist, key = " + key);
		}
		clusterMap.put(key, new Cluster(key));
		return getCluster(key);
	}

	/**
	 * Adds the cluster.
	 * 
	 * @param cluster the cluster
	 * @return the cluster
	 */
	public Cluster addCluster(Cluster cluster) {
		String key = cluster.getName();
		if ((SpkDiarizationLogger.DEBUG) && (containsCluster(key))) {
			logger.finer("addCluster : cluster exist, key = " + key);
		}
		clusterMap.put(key, cluster);
		return getCluster(key);
	}

	/**
	 * Read a list of segmentation files.
	 * 
	 * @param show the list of show to read
	 * @param parameter the param
	 * 
	 * @throws DiarizationException the clustering exception
	 * @throws Exception the exception
	 */
	public void read(String show, ParameterSegmentationFile parameter) throws DiarizationException, Exception {
		float rate = parameter.getRate();
		String filename = IOFile.getFilename(parameter.getMask(), show);
		File file = new File(filename);
		if (!file.exists()) {
			throw new FileNotFoundException("could not read file " + filename);
		}
		SegmentationFormat format = parameter.getFormat();
		if (format.equals(ParameterSegmentationFile.SegmentationFormat.FILE_BCK)) {
			readBck(file);
		} else if (format.equals(ParameterSegmentationFile.SegmentationFormat.FILE_CTL)) {
			readCTL(file, parameter.getEncoding(), rate);
		} else if (format.equals(ParameterSegmentationFile.SegmentationFormat.FILE_XML_EPAC)) {
			readXmlEPAC(file, parameter.getEncoding(), rate);
		} else if (format.equals(ParameterSegmentationFile.SegmentationFormat.FILE_XML_MEDIA)) {
			readXmlMEDIA(file, parameter.getEncoding(), rate);
		} else if (format.equals(ParameterSegmentationFile.SegmentationFormat.FILE_XML_REPERE)) {
			readXmlREPERE(file, parameter.getEncoding(), rate);
		} else {
			readSeg(file, parameter.getEncoding(), rate);
		}
	}

	/**
	 * Checks if line is empty.
	 * 
	 * @param line the line
	 * 
	 * @return true, if is empty line
	 */
	protected boolean isEmptyLine(String line) {
		char linechar = line.charAt(0);
		if (linechar == '\n') {
			return true;
		}
		if (linechar == '#') {
			return true;
		}
		if ((linechar == ';') && (line.charAt(1) == ';')) {
			return true;
		}
		return false;
	}

	/**
	 * Read xml for EPAC format.
	 * 
	 * @param f the f
	 * @param encoding the encoding
	 * @param rate the rate
	 * @throws Exception the exception
	 */
	public void readXmlEPAC(File f, Charset encoding, float rate) throws Exception {
		XmlEPACInputOutput xmlEPAC = new XmlEPACInputOutput();
		xmlEPAC.readXML(this, f, encoding, rate);
	}

	/**
	 * Read xml for REPERE format.
	 * 
	 * @param f the f
	 * @param encoding the encoding
	 * @param rate the rate
	 * @throws Exception the exception
	 */
	public void readXmlREPERE(File f, Charset encoding, float rate) throws Exception {
		XmlREPEREInputOutput xml = new XmlREPEREInputOutput();
		logger.warning("--> " + encoding.toString() + " rate=" + rate);
		xml.readXML(this, f, encoding, rate);
	}

	/**
	 * Read xml for MEDIA format.
	 * 
	 * @param f the f
	 * @param encoding the encoding
	 * @param rate the rate
	 * @throws Exception the exception
	 */
	public void readXmlMEDIA(File f, Charset encoding, float rate) throws Exception {
		XmlMEDIAInputOutput xmlMEDIA = new XmlMEDIAInputOutput();
		xmlMEDIA.readXML(this, f, encoding, rate);
	}

	/**
	 * Read a segmentation file.
	 * 
	 * @param f the f
	 * @param encoding the encoding
	 * @param rate the rate
	 * @throws Exception the exception
	 */
	public void readSeg(File f, Charset encoding, float rate) throws Exception {
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(f), encoding));
		readSegBuffer(bufferedReader, rate);
	}

	/**
	 * Read buffer (seg format).
	 * 
	 * @param bufferedReader the buffered reader
	 * @param rate the rate
	 * @throws Exception the exception
	 */
	public void readSegBuffer(BufferedReader bufferedReader, float rate) throws Exception {
		String line;
		String show = null;
		String name = null;
		while ((line = bufferedReader.readLine()) != null) {
			char[] linechar = line.toCharArray();
			if (linechar.length == 0) {
				continue; // empty line
			}
			if (linechar[0] == '\n') {
				continue; // empty line
			}
			if (linechar[0] == '#') {
				continue; // empty line
			}
			if ((linechar[0] == ';') && (linechar[1] == ';')) {
				continue; // rem line
			}
			String segmentChannel = "1";
			int segmentStart = 0;
			int segmentLen = 0;
			String segmentGender = null;
			String segmentBand = null;
			String segmentEnvironement = null;
			StringTokenizer stringTokenizer = new StringTokenizer(line, " ");
			int result = 0;
			while (stringTokenizer.hasMoreTokens()) {
				if (result == 0) {
					show = stringTokenizer.nextToken();
				} else if (result == 1) {
					segmentChannel = stringTokenizer.nextToken();
				} else if (result == 2) {
					segmentStart = Integer.parseInt(stringTokenizer.nextToken());
				} else if (result == 3) {
					segmentLen = Integer.parseInt(stringTokenizer.nextToken());
				} else if (result == 4) {
					segmentGender = stringTokenizer.nextToken();
				} else if (result == 5) {
					segmentBand = stringTokenizer.nextToken();
				} else if (result == 6) {
					segmentEnvironement = stringTokenizer.nextToken();
				} else if (result == 7) {
					name = stringTokenizer.nextToken();
					break;
				}
				result++;
			}
			if (result != 7) {
				throw new IOException("segmentation read error \n" + line + "\n ");
			}
			String key, value = "";
			String newShow = new String(show);
			Cluster cluster = getOrCreateANewCluster(name);
			cluster.setGender(segmentGender); // problem if segments have different Gender for this cluster
			cluster.setBandwidth(segmentBand); // problem if segments have different Band for this cluster
			cluster.setChannel(segmentChannel);

			Segment segment = new Segment(newShow, segmentStart, segmentLen, cluster, rate);

			while (stringTokenizer.hasMoreTokens()) {
				stringTokenizer.nextToken();
				key = stringTokenizer.nextToken();
				stringTokenizer.nextToken();
				value = stringTokenizer.nextToken();
				stringTokenizer.nextToken();
				segment.setInformation(key, value);
			}

			segment.setChannel(segmentChannel);
			segment.setBandwidth(segmentBand);
			segment.setEnvironment(segmentEnvironement);

			cluster.addSegment(segment);
			linechar[0] = '\0';
		}
	}

	/**
	 * Read bck format file.
	 * 
	 * @param f the f
	 */
	public void readBck(File f) {
		// TODO : A impl�menter
		logger.severe("readBck to be implemented");
		System.exit(1);
	}

	/**
	 * Read ctl format.
	 * 
	 * @param f the f
	 * @param encoding the encoding
	 * @param rate the rate
	 * @throws DiarizationException the diarization exception
	 * @throws NumberFormatException the number format exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void readCTL(File f, Charset encoding, float rate) throws DiarizationException, NumberFormatException, IOException {
		String line;
		char[] linechar = new char[5001];
		String show;
		String name;
		linechar[0] = '\0';
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(f), encoding));
		while ((line = bufferedReader.readLine()) != null) {
			if (linechar[0] == '\n') {
				continue;
			} // empty line
			if (linechar[0] == '#') {
				continue;
			} // empty line
			if ((linechar[0] == ';') && (linechar[1] == ';')) {
				continue;
			} // rem line
			int segmentStart, segmentEnd;
			String segmentGender;
			String segmentBand;
			String segmentBandF;

			int res = 0;

			StringTokenizer stringTokenizer = new StringTokenizer(line, " ");
			show = stringTokenizer.nextToken();
			res++;
			segmentStart = Integer.parseInt(stringTokenizer.nextToken());
			res++;
			segmentEnd = Integer.parseInt(stringTokenizer.nextToken());
			res++;

			StringTokenizer st_dash = new StringTokenizer(stringTokenizer.nextToken(), "-");
			st_dash.nextToken();
			res++;
			st_dash.nextToken();
			res++;
			st_dash.nextToken();
			res++;
			segmentBandF = st_dash.nextToken();
			res++;
			segmentGender = st_dash.nextToken();
			res++;
			name = st_dash.nextToken();
			res++;

			Cluster cluster = getCluster(name);
			if (cluster == null) {
				cluster = createANewCluster(name);
			}
			Segment segment = new Segment(show, segmentStart, segmentEnd - segmentStart, cluster, rate);
			segmentBand = Segment.bandwidthStrings[Segment.BandwidthType.BUNK.ordinal()];
			if (segmentBandF.equals(Segment.bandwidthNistStrings[Segment.BandwidthType.TEL.ordinal()])) {
				segmentBand = Segment.bandwidthStrings[Segment.BandwidthType.TEL.ordinal()];
			} else if (segmentBandF.equals(Segment.bandwidthNistStrings[Segment.BandwidthType.STUDIO.ordinal()])) {
				segmentBand = Segment.bandwidthStrings[Segment.BandwidthType.STUDIO.ordinal()];
			}
			segment.setBandwidth(segmentBand);

			cluster.setGender(segmentGender); // problem if segments have different Gender for this cluster
			cluster.setBandwidth(segmentBand); // problem if segments have different Band for this cluster
			cluster.addSegment(segment);
			if (res != 9) {
				throw new IOException("segmentation read error");
			}
			linechar[0] = '\0';
		}
	}

	/**
	 * Erase a cluster.
	 * 
	 * @param key the key
	 */
	public void removeCluster(String key) {
		clusterMap.remove(key);
	}

	/**
	 * Erase a cluster.
	 * 
	 * @param oldName the key
	 * @param newName the new name
	 */
	public void renameCluster(String oldName, String newName) {
		Cluster cluster = clusterMap.remove(oldName);
		cluster.setName(newName);
		clusterMap.put(newName, cluster);
	}

	/**
	 * transform the clusters To frames TreeMap.
	 * 
	 * @param cloneMode the clone mode
	 * @return the tree map< integer, segment>
	 * 
	 *         Warning: this fonction is very dangerous, it works only if the clusterSet contains only one records.
	 * @throws DiarizationException the diarization exception
	 */
	public TreeMap<Integer, Segment> getFeatureMap(boolean cloneMode) throws DiarizationException {
		TreeMap<Integer, Segment> segmentTreeMapResult = new TreeMap<Integer, Segment>();
		String showName = "";
		for (Cluster cluster : clusterMap.values()) {
			for (Segment segment : cluster) {
				int start = segment.getStart();
				int length = segment.getLength();
				for (int i = start; i < (start + length); i++) {
					if (showName.isEmpty()) {
						showName = segment.getShowName();
					} else {
						if (showName.equals(segment.getShowName()) == false) {
							throw new DiarizationException("clusterSet contains severals records");
						}
					}
					Segment newSegment = new Segment(segment.getShowName(), i, 1, cluster, segment.getRate());
					if (cloneMode == true) {
						newSegment = segment.clone();
						newSegment.setStart(i);
						newSegment.setLength(1);
					}
					// Plus utile, le genre porte sur le Cluster
					// newSegment.setGender(cluster.getGender());
					segmentTreeMapResult.put(i, newSegment);
				}
			}
		}
		return segmentTreeMapResult;
	}

	/**
	 * Gets the feature map.
	 * 
	 * @return the feature map
	 * @throws DiarizationException the diarization exception
	 */
	public TreeMap<Integer, Segment> getFeatureMap() throws DiarizationException {
		return getFeatureMap(false);
	}

	/**
	 * Write a segmentation.
	 * 
	 * @param showName the show name
	 * @param param the parameter instance
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ParserConfigurationException the parser configuration exception
	 * @throws SAXException the SAX exception
	 * @throws DiarizationException the diarization exception
	 * @throws TransformerException the transformer exception
	 */
	public void write(String showName, ParameterSegmentationFile param) throws IOException, ParserConfigurationException, SAXException, DiarizationException, TransformerException {
		String segOutFilename = IOFile.getFilename(param.getMask(), showName);
		logger.info("--> write ClusterSet : " + segOutFilename + " / " + showName);
		File f = new File(segOutFilename);
		SegmentationFormat format = param.getFormat();
		if (format.equals(ParameterSegmentationFile.SegmentationFormat.FILE_XML_EPAC)) {
			XmlEPACInputOutput xmlEPAC = new XmlEPACInputOutput();
			xmlEPAC.writeXML(this, f, param.getEncoding());
		} else if (format.equals(ParameterSegmentationFile.SegmentationFormat.FILE_XML_MEDIA)) {
			XmlMEDIAInputOutput xmlMEDIA = new XmlMEDIAInputOutput();
			xmlMEDIA.writeXML(this, f, param.getEncoding());
		} else if (param.getFormat().equals(ParameterSegmentationFile.SegmentationFormat.FILE_EGER_HYP)) {
			OutputStreamWriter dos = new OutputStreamWriter(new FileOutputStream(f), param.getEncoding());
			for (Cluster cluster : clusterMap.values()) {
				cluster.writeAsEGER(dos, type);
			}
			if (headClusterSet != null) {
				logger.info("save HEAD !");
				for (Cluster cluster : headClusterSet.clusterMap.values()) {
					cluster.writeAsEGER(dos, headClusterSet.type);
				}
			}
			// if (writing != null) {
			// writing.writeAsEGER(dos, "writing");
			// }
			dos.close();

			segOutFilename = IOFile.getFilename(param.getMask(), showName) + ".seg";
			f = new File(segOutFilename);
			dos = new OutputStreamWriter(new FileOutputStream(f), param.getEncoding());
			for (Cluster cluster : clusterMap.values()) {
				Set<Entry<String, Object>> set = getInformation().entrySet();
				for (Entry<String, Object> entry : set) {
					dos.write(";; clusterSet " + entry.getKey() + " " + entry.getValue().toString() + "\n");
				}
				cluster.writeAsSeg(dos);
			}
			dos.close();

		} else if (param.getFormat().equals(ParameterSegmentationFile.SegmentationFormat.FILE_CTL)) {
			OutputStreamWriter dos = new OutputStreamWriter(new FileOutputStream(f), param.getEncoding());
			for (Cluster cluster : clusterMap.values()) {
				cluster.writeAsCTL(dos);
			}
		} else {
			if (headClusterSet != null) {
				logger.info("save HEAD !");
				OutputStreamWriter dos = new OutputStreamWriter(new FileOutputStream(f), param.getEncoding());
				for (Cluster cluster : clusterMap.values()) {
					for (Segment segment : cluster) {
						segment.setChannel("speaker");
					}
					Set<Entry<String, Object>> set = getInformation().entrySet();
					for (Entry<String, Object> entry : set) {
						dos.write(";; clusterSet SPEAKER " + entry.getKey() + " " + entry.getValue().toString() + "\n");
					}
					cluster.writeAsSeg(dos);
				}

				for (Cluster cluster : headClusterSet.clusterMap.values()) {
					for (Segment segment : cluster) {
						segment.setChannel("head");
					}
					if (param.getFormat().equals(ParameterSegmentationFile.SegmentationFormat.FILE_CTL)) {
						cluster.writeAsCTL(dos);
					} else {
						Set<Entry<String, Object>> set = getInformation().entrySet();
						for (Entry<String, Object> entry : set) {
							dos.write(";; clusterSet HEAD " + entry.getKey() + " " + entry.getValue().toString() + "\n");
						}
						cluster.writeAsSeg(dos);
					}
				}
				if (getWriting() != null) {
					for (Segment segment : getWriting()) {
						segment.setChannel("writting");
					}
					getWriting().writeAsSeg(dos);
				}
				dos.close();
			} else {
				OutputStreamWriter dos = new OutputStreamWriter(new FileOutputStream(f), param.getEncoding());
				for (Cluster cluster : clusterMap.values()) {
					Set<Entry<String, Object>> set = getInformation().entrySet();
					for (Entry<String, Object> entry : set) {
						dos.write(";; clusterSet	 " + entry.getKey() + " " + entry.getValue().toString() + "\n");
					}
					cluster.writeAsSeg(dos);
				}
				dos.close();
			}
		}
	}

	/**
	 * Gets the cluster map.
	 * 
	 * @return the clusterMap
	 */
	public TreeMap<String, Cluster> getClusterMap() {
		return clusterMap;
	}

	/**
	 * Make head cluster set.
	 * 
	 * @return the cluster set
	 */
	public ClusterSet makeHeadClusterSet() {
		headClusterSet = new ClusterSet();
		headClusterSet.type = "head";
		return headClusterSet;
	}

	/**
	 * Make writing.
	 * 
	 * @return the cluster
	 */
	public Cluster makeWriting() {
		writing = new Cluster("writting");
		return writing;
	}

	/**
	 * Gets the head cluster set.
	 * 
	 * @return the videoClusterSet
	 */
	public ClusterSet getHeadClusterSet() {
		return headClusterSet;
	}

	/**
	 * Gets the writing.
	 * 
	 * @return the writing
	 */
	public Cluster getWriting() {
		return writing;
	}

	/**
	 * Sets the writing.
	 * 
	 * @param writing the writing to set
	 */
	public void setWriting(Cluster writing) {
		this.writing = writing;
	}

	/**
	 * Sets the head cluster set.
	 * 
	 * @param headClusterSet the headClusterSet to set
	 */
	public void setHeadClusterSet(ClusterSet headClusterSet) {
		this.headClusterSet = headClusterSet;
	}

	/**
	 * Gets the type.
	 * 
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the type.
	 * 
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * To speaker session.
	 * 
	 * @return the cluster set
	 */
	public ClusterSet toSpeakerSession() {
		ClusterSet resultClusterSet = new ClusterSet();

		for (String clusterName : clusterMap.keySet()) {
			Cluster cluster = getCluster(clusterName);
			for (Segment segment : cluster) {
				String showName = segment.getShowName();
				String clusteShowName = showName + "#_#" + clusterName;
				Cluster resultCluster = resultClusterSet.getOrCreateANewCluster(clusteShowName);
				resultCluster.setInformation("speaker", clusterName);
				resultCluster.setInformation("session", showName);
				resultCluster.addSegment(segment.clone());
			}
		}
		return resultClusterSet;
	}

}
