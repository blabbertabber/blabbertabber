/**
 * <p>
 * Clusters
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
 */

package fr.lium.spkDiarization.libClusteringData;

import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;

import edu.thesis.xml.transform.TransformerException;
import fr.lium.experimental.spkDiarization.libClusteringData.transcription.Entity;
import fr.lium.experimental.spkDiarization.libClusteringData.transcription.EntitySet;
import fr.lium.experimental.spkDiarization.libClusteringData.transcription.Link;
import fr.lium.experimental.spkDiarization.libClusteringData.transcription.LinkSet;
import fr.lium.experimental.spkDiarization.libClusteringData.turnRepresentation.Turn;
import fr.lium.experimental.spkDiarization.libClusteringData.turnRepresentation.TurnSet;
import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.IOFile;
import fr.lium.spkDiarization.parameter.ParameterSegmentationFile;
import fr.lium.spkDiarization.parameter.ParameterSegmentationFile.SegmentationFormat;

//import fr.lium.experimental.EPAC.xml.XmlEPACInputOutput;
//import fr.lium.experimental.MEDIA.xml.XmlMEDIAInputOutput;

/**
 * Container for the storage of clusters. A clusters corresponds to a segmentation. This is a container is a map.
 */
public class ClusterSet implements Cloneable, Iterable<String> {

    /** container of the clusters.*/
    protected TreeMap<String, Cluster> clusterMap;

    /** The information. */
    private TreeMap<String, Object> information;

    public ClusterSet() {
        clusterMap = new TreeMap<String, Cluster>();
        information = new TreeMap<String, Object>();
    }

    /* (non-Javadoc)
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
        information.put(key, value);
    }

    /**
     * Sets the information.
     *
     * @param key the key
     * @param value the value
     */
    public void setInformation(String key, String value) {
        information.put(key, value);
    }

    /**
     * Removes the information according the key.
     *
     * @param key the key
     */
    public void removeInformation(Object key) {
        information.remove(key);
    }

    /**
     * Gets the information indexed by the key.
     *
     * @param key the key
     *
     * @return the information
     */
    public String getInformation(String key) {
        return information.get(key).toString();
    }

    /**
     * Gets the Informations container.
     *
     * @return the informations
     */
    public String getInformations() {
        String result = "";
        for (String key : information.keySet()) {
            result = result + " [ " + key + " = " + information.get(key) + " ]";
        }
        return result;
    }

    /**
     * Adds the segment of the vector container .
     *
     * @param vSeg the v seg
     */
    public void addVector(ArrayList<Segment> vSeg) {
        clearClusters();
        for (Segment segment : vSeg) {
            String name = segment.getClusterName();
            Cluster cluster = getOrCreateANewCluster(name);
            // problem if segments have different Gender for this cluster
            // A verifier rajout du getCluster()
            cluster.setGender(segment.getCluster().getGender());
            // problem if segments have different Band for this cluster
            cluster.setBandwidth(segment.getBand());
            cluster.setChannel(segment.getBand());
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
    public Object clone() {
        ClusterSet result = null;
        try {
            result = (ClusterSet) (super.clone());
        } catch (CloneNotSupportedException e) {
        }
        result.clusterMap = new TreeMap<String, Cluster>();
        for (String key : clusterMap.keySet()) {
            result.clusterMap.put(key, (Cluster) (clusterMap.get(key).clone()));
        }
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
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
     * Collapse segments form a cluster that are contiguous
     *
     * @see Cluster#collapse()
     */
    public void collapse() {
        for (Cluster cluster : clusterMap.values()) {
            cluster.collapse(0);
        }
    }

    /**
     * Collapse segments form a cluster that are contiguous
     *
     * @see Cluster#collapse()
     */
    public void collapse(int delay) {
        System.err.println("***** colappse");
        TurnSet turnSet = getTurns();
        for (Turn turn : turnSet) {
            Iterator<Segment> segmentIterator = turn.iterator();
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
                        if ((previousEnd + delay) >= currentStart) {
                            previous.setLength(currentStart - previousStart + currentLength);
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
     * Construct a SegLst in which all segments of #map are copied
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

    // TODO to be remove

    public ArrayList<Cluster> getClusterVectorRepresentation() {
        ArrayList<Cluster> vector = new ArrayList<Cluster>();
        for (String key : clusterMap.keySet()) {
            vector.add(getCluster(key));
        }
        return vector;
    }

    /**
     * Merge two clusters: put the segments of the cluster name keyJ in the cluster name keyI
     *
     * @param keyI the name of the first cluster
     * @param keyJ the name of the second cluster
     */
    public void mergeCluster(String keyI, String keyJ) {
        clusterMap.get(keyI).addSegments(clusterMap.get(keyJ).iterator());
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
        if (containsCluster(key)) {
            System.err.println("[ClusterSet] \t addCluster : cluster exist, key = " + key);
        }
        clusterMap.put(key, new Cluster(key));
        return getCluster(key);
    }

    /**
     * Read a list of segmentation files.
     *
     * @param lst the list of show to read
     * @param param the param
     *
     * @throws DiarizationException the clustering exception
     * @throws Exception the exception
     */
    public void read(ArrayList<String> lst, ParameterSegmentationFile param) throws DiarizationException, Exception {
        for (String string : lst) {
            String filename = IOFile.getFilename(param.getMask(), string);
            File file = new File(filename);
            if (!file.exists()) {
                throw new DiarizationException("clusters: read(lst) could not read file " + filename);
            }
            SegmentationFormat format = param.getFormat();
            if (format.equals(ParameterSegmentationFile.SegmentationFormat.FILE_BCK)) {
                readBck(file);
            } else if (format.equals(ParameterSegmentationFile.SegmentationFormat.FILE_CTL)) {
                readCTL(file, param.getEncoding());
            } /*else if (format.equals(ParameterSegmentationFile.SegmentationFormat.FILE_XML_EPAC)) {
                readXmlEPAC(file, param.getEncoding());
			} else if (format.equals(ParameterSegmentationFile.SegmentationFormat.FILE_XML_MEDIA)) {
				System.err.println("*****MEDIA ****");
				readXmlMEDIA(file, param.getEncoding());
				System.err.println("*****MEDIA ****");
			}*/ else {
                read(file, param.getEncoding());
            }
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
     * Read a segmentation file.
     *
     * @param f the f
     * @param encoding the encoding
     *
     * @throws Exception the exception
     */
    public void read(File f, Charset encoding) throws Exception {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(f), encoding));
        readBuffer(bufferedReader);
    }

    /**
     * Read xml for EPAC format.
     *
     * @param f the f
     * @param encoding the encoding
     *
     * @throws Exception the exception
     */
	/*public void readXmlEPAC(File f, Charset encoding) throws Exception {
		XmlEPACInputOutput xmlEPAC = new XmlEPACInputOutput();
		xmlEPAC.readXML(this, f, encoding);
	}*/

    /**
     * Read xml for MEDIA format.
     *
     * @param f the f
     * @param encoding the encoding
     *
     * @throws Exception the exception
     */
	/*public void readXmlMEDIA(File f, Charset encoding) throws Exception {
		XmlMEDIAInputOutput xmlMEDIA = new XmlMEDIAInputOutput();
		xmlMEDIA.readXML(this, f, encoding);
	}*/

    /**
     * Read buffer (seg format).
     *
     * @param bufferedReader the buffered reader
     *
     * @throws Exception the exception
     */
    public void readBuffer(BufferedReader bufferedReader) throws Exception {
        String line;
        String show = null;
        String name = null;
        while ((line = bufferedReader.readLine()) != null) {
            char[] linechar = line.toCharArray();
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
                throw new DiarizationException("clusters: read() error seg segmentation read error \n" + line + "\n ");
            }
            String key, value = "";
            String newShow = new String(show);
            Cluster cluster = getOrCreateANewCluster(name);
            cluster.setGender(segmentGender); // problem if segments have different Gender for this cluster
            cluster.setBandwidth(segmentBand); // problem if segments have different Band for this cluster
            cluster.setChannel(segmentChannel);

            Segment segment = new Segment(newShow, segmentStart, segmentLen, cluster);

            while (stringTokenizer.hasMoreTokens()) {
                stringTokenizer.nextToken();
                key = stringTokenizer.nextToken();
                stringTokenizer.nextToken();
                value = stringTokenizer.nextToken();
                stringTokenizer.nextToken();
                segment.setInformation(key, value);
            }

            segment.setChannel(segmentChannel);
            segment.setBand(segmentBand);
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
        System.out.println("readBck to be implemented");
        System.exit(1);
    }

    /**
     * Read ctl format.
     *
     * @param f the f
     * @param encoding the encoding
     *
     * @throws DiarizationException the diarization exception
     * @throws NumberFormatException the number format exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void readCTL(File f, Charset encoding) throws DiarizationException, NumberFormatException, IOException {
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
            Segment segment = new Segment(show, segmentStart, segmentEnd - segmentStart, cluster);
            segmentBand = Segment.bandwidthStrings[Segment.BandwidthType.BUNK.ordinal()];
            if (segmentBandF.equals(Segment.bandwidthNistStrings[Segment.BandwidthType.TEL.ordinal()])) {
                segmentBand = Segment.bandwidthStrings[Segment.BandwidthType.TEL.ordinal()];
            } else if (segmentBandF.equals(Segment.bandwidthNistStrings[Segment.BandwidthType.STUDIO.ordinal()])) {
                segmentBand = Segment.bandwidthStrings[Segment.BandwidthType.STUDIO.ordinal()];
            }
            segment.setBand(segmentBand);

            cluster.setGender(segmentGender); // problem if segments have different Gender for this cluster
            cluster.setBandwidth(segmentBand); // problem if segments have different Band for this cluster
            cluster.addSegment(segment);
            if (res != 9) {
                throw new DiarizationException("clusters: readCtl() error seg segmentation read error");
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
     * transform the clusters To frames TreeMap.
     *
     * @return the tree map< integer, segment>
     *
     * Warning: this fonction is very dangerous, it works only if the clusterSet contains only one records.
     * @throws DiarizationException
     */
    public TreeMap<Integer, Segment> toFrames() throws DiarizationException {
        TreeMap<Integer, Segment> segmentTreeMapResult = new TreeMap<Integer, Segment>();
        String showName = "";
        for (Cluster cluster : clusterMap.values()) {
            for (Segment segment : cluster) {
                int start = segment.getStart();
                int length = segment.getLength();
                for (int i = start; i < (start + length); i++) {
                    if (showName.length() == 0) {
                        showName = segment.getShowName();
                    } else {
                        if (showName.equals(segment.getShowName()) == false) {
                            throw new DiarizationException("ERROR clusterSet, toFrames: clusterSet contains severals records");
                        }
                    }
                    Segment newSegment = new Segment(segment.getShowName(), i, 1, cluster);
                    // Plus utile, le genre porte sur le Cluster
                    // newSegment.setGender(cluster.getGender());
                    segmentTreeMapResult.put(i, newSegment);
                }
            }
        }
        return segmentTreeMapResult;
    }

    /**
     * Write a segmentation.
     *
     * @param showName the show name
     * @param param the parameter instance
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws TransformerException the transformer exception
     * @throws DiarizationException the diarization exception
     * @throws SAXException the SAX exception
     * @throws ParserConfigurationException the parser configuration exception
     */
    public void write(String showName, ParameterSegmentationFile param) throws IOException, ParserConfigurationException, SAXException, DiarizationException,
            TransformerException {

		/* Original code - cutting out since we can't do XML stuff ...
		String segOutFilename = IOFile.getFilename(param.getMask(), showName);
		File f = new File(segOutFilename);
		SegmentationFormat format = param.getFormat();

		if (format.equals(ParameterSegmentationFile.SegmentationFormat.FILE_XML_EPAC)) {
			XmlEPACInputOutput xmlEPAC = new XmlEPACInputOutput();
			xmlEPAC.writeXML(this, f, param.getEncoding());
		} else if (format.equals(ParameterSegmentationFile.SegmentationFormat.FILE_XML_MEDIA)) {
			XmlMEDIAInputOutput xmlMEDIA = new XmlMEDIAInputOutput();
			xmlMEDIA.writeXML(this, f, param.getEncoding());
		}

			OutputStreamWriter dos = new OutputStreamWriter(new FileOutputStream(f), param.getEncoding());
			int cpt = 0;
			for (Cluster cluster : clusterMap.values()) {
				if (param.getFormat().equals(ParameterSegmentationFile.SegmentationFormat.FILE_CTL)) {
					cluster.writeAsCTL(dos);
				} else {
					cluster.write(dos);
				}
				cpt++;
			}
			dos.close();
		*/

        String segOutFilename = IOFile.getFilename(param.getMask(), showName);
        File f = new File(segOutFilename);

		/* unread ...
		SegmentationFormat format = param.getFormat();
		*/

        OutputStreamWriter dos = new OutputStreamWriter(new FileOutputStream(f), param.getEncoding());
        int cpt = 0;
        for (Cluster cluster : clusterMap.values()) {
            if (param.getFormat().equals(ParameterSegmentationFile.SegmentationFormat.FILE_CTL)) {
                cluster.writeAsCTL(dos);
            } else {
                cluster.write(dos);
            }
            cpt++;
        }
        dos.close();

    }

    /**
     * The Class ReversSegmentComparator.
     */
    public class ReversSegmentComparator implements Comparator<Segment> {

        /* (non-Javadoc)
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(Segment o1, Segment o2) {
            return -1 * o1.compareTo(o2);
        }

    }

}
