/**
 * 
 * <p>
 * Entity
 * </p>
 * 
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
 * @version v2.0
 * 
 *          Copyright (c) 2007-2009 Universite du Maine. All Rights Reserved. Use is subject to license terms.
 * 
 *          THIS SOFTWARE IS PROVIDED BY THE "UNIVERSITE DU MAINE" AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *          DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *          USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *          ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 *          An entity. This class is employed in conjunction of EntitySet, Link and LinkSet.
 */
package fr.lium.experimental.spkDiarization.libClusteringData.transcription;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.lium.experimental.spkDiarization.libSCTree.SCTProbabilities;
import fr.lium.spkDiarization.lib.DiarizationException;

/**
 * The Class Entity.
 */
public class Entity implements Comparable<Entity>, Iterable<Path>, Cloneable {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(Entity.class.getName());

	/** type of entity. */
	protected String type;

	/** The id. */
	protected int id;

	/** path in the graph that identify the entity. */
	ArrayList<Path> listOfLink;

	/** The link set. */
	protected LinkSet linkSet;

	/** The scores. */
	protected TreeMap<String, SCTProbabilities> scores;

	/**
	 * Instantiates a new entity.
	 * 
	 * @param linkSet the link set
	 * @param type the type
	 * @param id the id
	 * @throws DiarizationException the diarization exception
	 */
	public Entity(LinkSet linkSet, String type, int id) throws DiarizationException {
		this.type = type;
		this.linkSet = linkSet;
		this.id = id;
		listOfLink = new ArrayList<Path>();
		scores = new TreeMap<String, SCTProbabilities>();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected Object clone() throws CloneNotSupportedException {
		Entity copy = (Entity) super.clone();
		copy.listOfLink = new ArrayList<Path>();
		for (Path path : listOfLink) {
			copy.listOfLink.add((Path) path.clone());
		}
		return copy;
	}

	/**
	 * Start.
	 * 
	 * @return the int
	 */
	public int start() {
		return listOfLink.get(0).idOfLink;
	}

	/**
	 * Last.
	 * 
	 * @return the int
	 */
	public int last() {
		return listOfLink.get(listOfLink.size() - 1).idOfLink;
	}

	/**
	 * Checks if is person.
	 * 
	 * @return true, if is person
	 */
	public boolean isPerson() {
		if (type.equals(EntitySet.TypePersonne)) {
			return true;
		}
		return false;
	}

	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets the id.
	 * 
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Gets the type of the entity.
	 * 
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the type of entity.
	 * 
	 * @param type the new type
	 */
	public void setType(String type) {
		this.type = type;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Entity o) {
		try {
			Path oPath = o.listOfLink.get(0);
			Path path = listOfLink.get(0);
			return path.compareTo(oPath);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "", e);
			e.printStackTrace();
			return -1;
		}
	}

	/**
	 * Debug.
	 */
	public void debug() {
		logger.finer("<entity type=\"" + type + "\" >");
		for (Path path : this) {
			logger.finer("\t<path graph=\"" + path.getIdOfLinkSet() + "\" link=\"" + path.getIdLink() + "\" />");
		}

		for (String name : scores.keySet()) {
			String ch = "\t<scores name=\"" + name + "\" ";
			SCTProbabilities score = scores.get(name);
			for (String key : score.keySet()) {
				ch += "key=" + key + " value=" + score.get(key) + " ";
			}
			ch += " />";
			logger.finer(ch);
		}

		logger.finer("</entity>");
	}

	/**
	 * Adds the path.
	 * 
	 * @param graph the graph
	 * @param link the link
	 * 
	 * @return true, if successful
	 */
	public boolean addPath(int graph, int link) {
		return listOfLink.add(new Path(graph, link));
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Path> iterator() {
		return listOfLink.iterator();
	}

	/**
	 * Gets the list of words.
	 * 
	 * @return the list of words
	 */
	public String getListOfWords() {
		String result = "";
		for (Path path : this) {
			if (result != "") {
				result += " ";
			}
			result += linkSet.getLink(path.idOfLink).word;
		}
		return result;
	}

	/**
	 * Gets the scores.
	 * 
	 * @return the scores
	 */
	public TreeMap<String, SCTProbabilities> getScores() {
		return scores;
	}

	/**
	 * Gets the score.
	 * 
	 * @param name the name
	 * @return the probabilites
	 */
	public SCTProbabilities getScore(String name) {
		if (scores.containsKey(name) == true) {
// logger.info("return");
			return scores.get(name);
		} else {
// logger.info("create");
			scores.put(name, new SCTProbabilities());
			return scores.get(name);
		}
	}

}
