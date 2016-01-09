/**
 * <p>
 * Cluster
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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;

import fr.lium.experimental.spkDiarization.libClusteringData.speakerName.SpeakerName;
import fr.lium.experimental.spkDiarization.libClusteringData.speakerName.SpeakerNameSet;
import fr.lium.experimental.spkDiarization.libClusteringData.transcription.Entity;
import fr.lium.experimental.spkDiarization.libClusteringData.transcription.EntitySet;
import fr.lium.experimental.spkDiarization.libClusteringData.transcription.Link;
import fr.lium.experimental.spkDiarization.libClusteringData.transcription.LinkSet;

/**
 * Container for the storage of segments. A cluster generally corresponds to speaker.
 */
public class Cluster implements Comparable<Cluster>, Cloneable, Iterable<Segment> {
    /**number of gender*/
    public static final int numberOfGenders = 4;
    /** Array of string corresponding to a Gender enum*/
    public static final String[] genderStrings = {"U", "M", "F", "C"};
    protected TreeSet<Segment> segmentSet; // sorted list (a set) of segments, sorted by start time
    /** gender of the speaker */
    protected String gender;

    //protected double score;
    /** bandwidth of the speaker */
    protected String bandwidth;
    /** channel of the audio, set to 1, the first*/
    protected String channel;
    /** output name of the cluster/speaker, key in ClusterSet*/
    protected String name;
    /** Real name of speaker storage*/
    SpeakerNameSet speakerNameSet;
    /** Universal information storage Map*/
    private TreeMap<String, Object> information;
    /** user data information storage */
    private Object userData;
    /** user data information storage 2*/
    private Object sortedUserData;

    /**
     * The Constructor.
     *
     * @param _name of the cluster
     */
    public Cluster(String _name) {
        gender = genderStrings[0];
        name = _name;
        segmentSet = new TreeSet<Segment>();
        information = new TreeMap<String, Object>();
        bandwidth = Segment.bandwidthStrings[0];
        speakerNameSet = new SpeakerNameSet();
    }

    /**
     * Gets the information.
     *
     * @return th information TreeMap
     */
    public TreeMap<String, Object> getInformation() {
        return information;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Cluster) {
            Cluster other = (Cluster) obj;
            boolean segEqual = other.segmentSet.equals(segmentSet);
            if ((segEqual == true) && (other.gender == gender) && (other.channel == channel) && (other.name == name) && (other.bandwidth == bandwidth)) {
                return true;
            } /*
             * else { System.err.printf("+ cluster no equal seg:"+(segEqual == true)+ " gender:"+(other.gender == gender)+ " chanel:"+(other.chanel == chanel)+
			 * " name:"+(other.name == name)+ " band:"+(other.band == band)); }
			 */
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
    public void normalizeSpeakerNameSet() {
        speakerNameSet.normalizeScoreCluster();
    }

    /**
     * Compute belief functions of the scoreCluster of speaker name set.
     */
    public void computeBeliefFunctions() throws Exception {
        speakerNameSet.computeBeliefFunctions();
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
    public Object clone() {
        Cluster result = null;
        try {
            result = (Cluster) (super.clone());
        } catch (CloneNotSupportedException e) {
        }
        result.segmentSet = new TreeSet<Segment>();
        for (Segment segment : segmentSet) {
            result.segmentSet.add((Segment) (segment.clone()));
        }
        result.information = new TreeMap<String, Object>();
        for (String key : information.keySet()) {
            result.putInformation(key, information.get(key));
        }
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
                Segment newSegment = (Segment) (segment.clone());
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
     */
    public void collapse(int delay) {
        Iterator<Segment> segmentIterator = iterator();
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

    /**
     * Collapse segments.
     */
    public void collapse() {
        collapse(0);
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Cluster aCluster) {
        Cluster cluster = aCluster;
        return (segmentSet.first().compareTo(cluster.segmentSet.first()));
    }

    /**
     * Information of the cluster.
     *
     * @param level the level
     */
    public void debug(int level) {
        System.out.print("debug[Cluster] \t name= " + getName());
        System.out.print(" gender= " + getGender());
        System.out.print(" channel= " + getChannel());
        System.out.print(" Length= " + getLength());
        if (level > 0) {
            System.out.println(" info= " + getInformations());
            speakerNameSet.debug();
        }
        for (Segment segment : segmentSet) {
            segment.debug(level);
        }
		/*
		 * Iterator<Segment> segmentIterator = segments.iterator(); while (segmentIterator.hasNext()) { segmentIterator.next().debug(); }
		 */
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
     * Sets the bandwidth value.
     *
     * @param bandwidth the new bandwidth
     */
    public void setBandwidth(String bandwidth) {
        this.bandwidth = bandwidth;
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
     * Sets the channel of the cluster.
     *
     * @param chanel the new channel
     */
    public void setChannel(String chanel) {
        this.channel = chanel;
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
     * Set the gender of the cluster
     *
     * @param c the gender
     */
    public void setGender(String c) {
        this.gender = c;
    }

    /**
     * Gets the information.
     *
     * @param key the key in information Map
     *
     * @return the information
     */
    public String getInformation(String key) {
        return information.get(key).toString();
    }

    /**
     * Gets the information Map instance.
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
     * Get the cluster name
     *
     * @return the name of the cluster
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set the cluster name
     *
     * @param newName the name of the cluster
     */
    public void setName(String newName) {
        this.name = newName;
    }

    /**
     * Get the container Useful to add and delete segments
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
     * Sets the user data.
     *
     * @param userData the new user data
     */
    public void setUserData(Object userData) {
        this.userData = userData;
    }

    /**
     * Gets the sorted user data Object.
     *
     * @return the sorted user data
     */
    public Object getSortedUserData() {
        return sortedUserData;
    }

    /**
     * Sets the sorted user data.
     *
     * @param sortedUserData the new sorted user data
     */
    public void setSortedUserData(Object sortedUserData) {
        this.sortedUserData = sortedUserData;
    }

    /* (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
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
        information.remove(key);
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
     * Puts Information (key, information).
     *
     * @param key the key
     * @param value the value
     */
    public void putInformation(String key, Object value) {
        information.put(key, value);
    }

    /**
     * Put information (key, information).
     *
     * @param key the key
     * @param value the value
     */
    public void putInformation(String key, String value) {
        information.put(key, value);
    }

    /**
     * Write the cluster and the segments to a \e *FILE
     *
     * @param dos the DataOutPutstream to write to
     * @throws IOException
     */
    public void write(OutputStreamWriter dos) throws IOException {
        String line = getInformations();
        if (line != "") {
            line = ";; cluster:" + name + getInformations() + "\n";
            dos.write(line);
        }
        for (Segment segment : segmentSet) {
            line = segment.getShowName() + " " + segment.getChannel() + " " + segment.getStart();
            line += " " + segment.getLength() + " " + gender + " " + segment.getBand();
            line += " " + segment.getEnv() + " " + name + segment.getInformations() + "\n";
            dos.write(line);
        }
    }

    /**
     * Write the cluster and the segments to a CTL \e *FILE
     *
     * @param dos the DataOutPutstream to write to
     * @throws IOException
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
            float start = (float) seg.getStart() / 100;
            float end = (float) (seg.getStart() + seg.getLength()) / 100;
            String band = Segment.bandwidthNistStrings[0];
            if (seg.getBand() == Segment.bandwidthStrings[1]) {
                band = Segment.bandwidthNistStrings[1];
            } else if (seg.getBand() == Segment.bandwidthStrings[2]) {
                band = Segment.bandwidthNistStrings[2];
            }

            String line = seg.getShowName() + " " + seg.getStart() + " " + (seg.getStart() + seg.getLength());
            line += " " + seg.getShowName() + "-" + df.format(start) + "-" + df.format(end) + "-" + band;
            line += "-" + this.gender + "-" + name + "\n";
            dos.write(line);
        }
    }
}
