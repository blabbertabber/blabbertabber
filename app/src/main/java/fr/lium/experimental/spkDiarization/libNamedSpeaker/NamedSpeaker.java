/**
 * 
 * <p>
 * NamedSpeaker
 * </p>
 * 
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
 * @author <a href="mailto:vincent.jousse@lium.univ-lemans.fr">Vincent Jousse</a>
 * @version v2.0
 * 
 *          Copyright (c) 2007-2009 Universite du Maine. All Rights Reserved. Use is subject to license terms.
 * 
 *          THIS SOFTWARE IS PROVIDED BY THE "UNIVERSITE DU MAINE" AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *          DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *          USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *          ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 *          Speaker identification by the name of the speaker.
 * 
 *          see : Vincent Jousse, Simon Petitrenaud, Sylvain Meignier, Yannick Esteve, Christine Jacquin (2009), Automatic named identification of speakers using diarization and asr systems, In: ICASSP 2009, 19-24 avril 2009, Taipei (Taiwan).
 */
package fr.lium.experimental.spkDiarization.libNamedSpeaker;

/**
 * The Class NamedSpeaker.
 */
public class NamedSpeaker {

	/** The cluster name. */
	private String clusterName;

	/** The count. */
	private int count;

	/** The score. */
	private double score;

	/** The repartition. */
	private double repartition;

	/** The combined score. */
	private double combinedScore;

	/**
	 * Instantiates a new named speaker.
	 */
	public NamedSpeaker() {
		super();
		this.clusterName = "empty";
		this.count = 0;
		this.score = 0;
		this.combinedScore = 0;
		this.repartition = 0;
	}

	/**
	 * Instantiates a new named speaker.
	 * 
	 * @param clusterName the cluster name
	 * @param count the count
	 * @param score the score
	 */
	public NamedSpeaker(String clusterName, int count, double score) {
		super();
		this.clusterName = clusterName;
		this.count = count;
		this.score = score;
	}

	/**
	 * Adds the score.
	 * 
	 * @param score the score
	 */
	public void addScore(double score) {
		this.score += score;
		count++;
	}

	/**
	 * Gets the cluster name.
	 * 
	 * @return the cluster name
	 */
	public String getClusterName() {
		return clusterName;
	}

	/**
	 * Gets the count.
	 * 
	 * @return the count
	 */
	public int getCount() {
		return count;
	}

	/**
	 * Gets the score.
	 * 
	 * @return the score
	 */
	public double getScore() {
		return score;
	}

	/**
	 * Sets the cluster name.
	 * 
	 * @param clusterName the new cluster name
	 */
	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	/**
	 * Sets the count.
	 * 
	 * @param count the new count
	 */
	public void setCount(int count) {
		this.count = count;
	}

	/**
	 * Sets the score.
	 * 
	 * @param score the new score
	 */
	public void setScore(double score) {
		this.score = score;
	}

	/**
	 * Sets the repartition.
	 * 
	 * @param repartition the new repartition
	 */
	public void setRepartition(double repartition) {
		this.repartition = repartition;
	}

	/**
	 * Gets the repartition.
	 * 
	 * @return the repartition
	 */
	public double getRepartition() {
		return this.repartition;
	}

	/**
	 * Sets the combined score.
	 * 
	 * @param combinedScore the new combined score
	 */
	public void setCombinedScore(double combinedScore) {
		this.combinedScore = combinedScore;
	}

	/**
	 * Gets the combined score.
	 * 
	 * @return the combined score
	 */
	public double getCombinedScore() {
		return this.combinedScore;
	}

}
