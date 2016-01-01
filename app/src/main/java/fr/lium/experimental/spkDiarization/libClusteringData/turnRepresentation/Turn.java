/**
 * 
 * <p>
 * Turn
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
 *          A Turn is a set of consecutive segments owned by the same speaker.
 */
package fr.lium.experimental.spkDiarization.libClusteringData.turnRepresentation;

import java.util.ArrayList;
import java.util.Iterator;

import fr.lium.experimental.spkDiarization.libClusteringData.transcription.Link;
import fr.lium.experimental.spkDiarization.libClusteringData.transcription.LinkSet;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.Segment;

// TODO: Auto-generated Javadoc
/**
 * The Class Turn. A Turn is a list of temporal consecutive segment of the same speaker.
 */
public class Turn implements Comparable<Turn>, Iterable<Segment> {

	/** The segment container. */
	ArrayList<Segment> segments;

	/**
	 * Instantiates a new turn.
	 */
	public Turn() {
		segments = new ArrayList<Segment>();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Turn o) {
		return first().compareTo(o.first());
	};

	/**
	 * Gets the segment at position #i .
	 * 
	 * @param i the index
	 * 
	 * @return the segment
	 */
	public Segment get(int i) {
		return segments.get(i);
	}

	/**
	 * Size.
	 * 
	 * @return the int
	 */
	public int size() {
		return segments.size();
	}

	/**
	 * Return the first segment.
	 * 
	 * @return the segment
	 */
	public Segment first() {
		return segments.get(0);
	}

	/**
	 * Return the last segment.
	 * 
	 * @return the segment
	 */
	public Segment last() {
		return segments.get(segments.size() - 1);
	}

	/**
	 * Gets the cluster link to segments.
	 * 
	 * @return the cluster
	 */
	public Cluster getCluster() {
		return first().getCluster();
	}

	/**
	 * Adds the a segment.
	 * 
	 * @param segment the segment
	 * 
	 * @return true, if successful
	 */
	public boolean add(Segment segment) {
		return segments.add(segment);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Segment> iterator() {
		return segments.iterator();
	}

	/**
	 * Gets the collapsed link set into a link stored in the LinkSet result.
	 * 
	 * @return the collapsed link set
	 * 
	 * @throws CloneNotSupportedException the clone not supported exception
	 */
	public LinkSet getCollapsedLinkSet() throws CloneNotSupportedException {
		LinkSet result = new LinkSet(0);
		for (Segment segment : this) {
			result.getInformation().putAll(segment.getTranscription().getLinkSet().getInformation());
			for (Link link : segment.getTranscription().getLinkSet()) {
				result.add((Link) link.clone());
			}
		}
		int i = 0;
		for (Link link : result) {
			link.setId(i);
			link.setStart(i);
			link.setEnd(++i);
		}
		return result;

	}

}
