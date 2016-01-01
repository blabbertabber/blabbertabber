/**
 * 
 * <p>
 * FSMSausageNode
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

package fr.lium.deprecated.spkDiarization;

import fr.lium.experimental.spkDiarization.libClusteringData.transcription.Entity;

// TODO: Auto-generated Javadoc
/**
 * The Class FSMSausageNode.
 * 
 * @deprecated
 */
@Deprecated
public class FSMSausageNode {

	/** The word. */
	private String word;

	/** The type. */
	private String type;

	/** The entity. */
	private Entity entity;

	/** The probability. */
	private double probability;

	/** The id. */
	protected int id;

	/**
	 * Instantiates a new fSM sausage node.
	 * 
	 * @param id the id
	 * @param word the word
	 * @param probability the probability
	 * @param type the type
	 */
	public FSMSausageNode(int id, String word, double probability, String type) {
		this.word = word;
		this.probability = probability;
		this.type = type;
		this.id = id;
		entity = null;
	}

	/**
	 * Debug.
	 */
	public void debug() {
		System.err.print("\t\t<word  propability=\"" + probability + "\" word=\"" + word + "\" type=\"" + type);
		if (entity != null) {
			System.err.print(" entity=\"" + entity.getType() + "\"");
		}
		System.err.println("/> ");
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
	 * Gets the type.
	 * 
	 * @return the type
	 */
	public String getType() {
		return type;
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
	 * Gets the id entity.
	 * 
	 * @return the id entity
	 */
	public Entity getIdEntity() {
		return entity;
	}

	/**
	 * Sets the id entity.
	 * 
	 * @param entity the new id entity
	 */
	public void setIdEntity(Entity entity) {
		this.entity = entity;
	}

	/**
	 * Sets the word.
	 * 
	 * @param word the new word
	 */
	protected void setWord(String word) {
		this.word = word;
	}

	/**
	 * Sets the type.
	 * 
	 * @param type the new type
	 */
	protected void setType(String type) {
		this.type = type;
	}

	/**
	 * Sets the probability.
	 * 
	 * @param probability the new probability
	 */
	protected void setProbability(double probability) {
		this.probability = probability;
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
	 * @param id the new id
	 */
	protected void setId(int id) {
		this.id = id;
	}

	/**
	 * Put entity in word.
	 */
	public void putEntityInWord() {
		if (getIdEntity() != null) {
			word = entity.getType();
		}
	}
}
