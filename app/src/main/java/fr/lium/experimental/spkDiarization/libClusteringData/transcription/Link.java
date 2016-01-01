/**
 * 
 * <p>
 * FSMSausage
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
 */
package fr.lium.experimental.spkDiarization.libClusteringData.transcription;

import java.io.Serializable;
import java.util.logging.Logger;

/**
 * The Class Link in a LinkSet. Class to store word.
 */
public class Link implements Cloneable, Serializable {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(Link.class.getName());

	/** The Constant DefaultType. */
	public static final String DefaultType = "wtoken";

	/** The Constant fillerType. */
	public static final String fillerType = "filler";

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The start index. */
	protected int start;

	/** The end index. */
	protected int end;

	/** The word. */
	protected String word;

	/** The type of word. */
	protected String type;

	/** The entity. */
	protected Entity entity;

	/** The probability. */
	protected double probability;

	/** The unique identifier in the link set. */
	protected int id;

	// protected String id;
	//

	/**
	 * Debug.
	 */
	public void debug() {
		if (entity != null) {
			String etype = entity.getType();
			logger.finer("\t<link id=\"" + id + "\" start=\"" + start + "\" end=\"" + end + "\" word=\"" + word
					+ "\" type=\"" + type + "\" probability=\"" + probability + "\" entity=\"" + etype + "\"/> ");
		} else {
			logger.finer("\t<link id=\"" + id + "\" start=\"" + start + "\" end=\"" + end + "\" word=\"" + word
					+ "\" type=\"" + type + "\" probability=\"" + probability + "\"/> ");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String result = "";
		if (entity != null) {
			result = "[" + entity.getType() + " " + word + "]";
		} else {
			result = word;
		}
		return result;
	}

	/**
	 * Instantiates a new link.
	 * 
	 * @param id the identifier of the link
	 * @param start the start position
	 * @param end the end position
	 * @param type the type
	 * @param probability the probability
	 * @param word the word
	 */
	public Link(int id, int start, int end, String type, double probability, String word) {
		super();
		this.start = start;
		this.end = end;
		this.word = word;
		this.type = type;
		this.probability = probability;
		this.id = id;
	}

	/**
	 * Find.
	 * 
	 * @param o the string to find
	 * 
	 * @return true, if successful
	 */
	public boolean find(String o) {
		if (o.startsWith("entity.")) {
			return haveEntity(o);
		}
		return o.equals(word);
	}

	/**
	 * Gets the start.
	 * 
	 * @return the start position
	 */
	public int getStart() {
		return start;
	}

	/**
	 * Sets the start.
	 * 
	 * @param start the start position to set
	 */
	public void setStart(int start) {
		this.start = start;
	}

	/**
	 * Gets the end.
	 * 
	 * @return the end position
	 */
	public int getEnd() {
		return end;
	}

	/**
	 * Sets the end.
	 * 
	 * @param end the end position to set
	 */
	public void setEnd(int end) {
		this.end = end;
	}

	/**
	 * Gets the word.
	 * 
	 * @return the word
	 */
	public String getWord() {
		return word;
	}

	/**
	 * Sets the word.
	 * 
	 * @param word the word to set
	 */
	public void setWord(String word) {
		this.word = word;
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
	 * Gets the entity.
	 * 
	 * @return the entity
	 */
	public Entity getEntity() {
		return entity;
	}

	/**
	 * Sets the entity.
	 * 
	 * @param entity the entity to set
	 */
	public void setEntity(Entity entity) {
		this.entity = entity;
	}

	/**
	 * Gets the probability.
	 * 
	 * @return the probability
	 */
	public double getProbability() {
		return probability;
	}

	/**
	 * Sets the probability.
	 * 
	 * @param probability the probability to set
	 */
	protected void setProbability(double probability) {
		this.probability = probability;
	}

	/**
	 * Gets the identifier.
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

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */

	@Override
	public Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		Link clone = (Link) super.clone();
		if (entity != null) {
			clone.entity = entity;
		}
		return clone;
	}

	/**
	 * Clone with entity.
	 * 
	 * @return the object
	 * @throws CloneNotSupportedException the clone not supported exception
	 */
	public Object cloneWithEntity() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		Link clone = (Link) super.clone();
		if (entity != null) {
			clone.entity = (Entity) entity.clone();
		}
		return clone;
	}

	/**
	 * Merge.
	 * 
	 * @param l the l
	 */
	public void merge(Link l) {
		word += " " + l.word;
		probability *= l.probability;
		end = Math.max(end, l.end);
		start = Math.min(start, l.start);
	}

	/**
	 * Have entity.
	 * 
	 * @return true, if successful
	 */
	public boolean haveEntity() {
		return getEntity() != null;
	}

	/**
	 * Have entity.
	 * 
	 * @param type the type
	 * 
	 * @return true, if successful
	 */
	public boolean haveEntity(String type) {
		if (haveEntity() == false) {
			return false;
		} else {
			return getEntity().getType().equals(type);
		}
	}
}
