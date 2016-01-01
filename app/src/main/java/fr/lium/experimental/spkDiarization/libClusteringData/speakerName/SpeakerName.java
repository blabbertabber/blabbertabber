/**
 * 
 * <p>
 * SpeakerName
 * </p>
 * 
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
 * @version v3.0
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

package fr.lium.experimental.spkDiarization.libClusteringData.speakerName;

// TODO: Auto-generated Javadoc

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Class SpeakerName.
 */
public class SpeakerName implements Comparable<SpeakerName>, Cloneable {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(SpeakerName.class.getName());

	/** The name of the speaker. */
	private String name;

	/** The score of cluster. */
	private double scoreCluster;
	/** The score of speaker. */
	private double scoreSpeaker;

	/** The Number of score cluster. */
	private int numberOfScoreCluster;

	/** The number of score speaker. */
	private int numberOfScoreSpeaker;

	/** We will keep each score into this ArrayList It will avoid losing information when summing scores. */

	private ArrayList<Double> scoreClusterList;

	/**
	 * Instantiates a new speaker name.
	 * 
	 * @param name the name of the speaker
	 */
	public SpeakerName(String name) {
		super();
		this.name = name;
		this.scoreCluster = 0;
		this.scoreSpeaker = 1.0;
		numberOfScoreCluster = 0;
		numberOfScoreSpeaker = 0;
		scoreClusterList = new ArrayList<Double>();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected Object clone() throws CloneNotSupportedException {
		SpeakerName result = null;
		try {
			result = (SpeakerName) (super.clone());
		} catch (CloneNotSupportedException e) {
			logger.log(Level.SEVERE, "", e);
			e.printStackTrace();
		}
		result.scoreClusterList = new ArrayList<Double>();
		for (double d : scoreClusterList) {
			result.scoreClusterList.add(d);
		}

		return result;
	}

	/**
	 * set a new speaker name.
	 * 
	 * @param name the name of the speaker
	 * @param scoreCluster the score of the cluster
	 * @param scoreSpeaker the score of the speaker
	 */
	public void set(String name, double scoreCluster, double scoreSpeaker) {
		this.name = name;
		this.scoreCluster = scoreCluster;
		this.scoreSpeaker = scoreSpeaker;
		numberOfScoreCluster = 1;
		numberOfScoreSpeaker = 1;
	}

	/**
	 * Gets the name of the speaker.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the speaker.
	 * 
	 * @param name the new name
	 */
	protected void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the score.of the cluster
	 * 
	 * @return the score
	 */
	public double getScoreCluster() {
		return scoreCluster;
	}

	/**
	 * Sets the score.of the cluster
	 * 
	 * @param score the new score
	 */
	public void setScoreCluster(double score) {
		numberOfScoreCluster = 1;
		this.scoreCluster = score;
	}

	/**
	 * Increment score.of the cluster
	 * 
	 * @param score the increment value
	 */
	public void incrementScoreCluster(double score) {
		numberOfScoreCluster += 1;
		this.scoreCluster += score;
	}

	/**
	 * Gets the score.of the Speaker
	 * 
	 * @return the score
	 */
	public double getScoreSpeaker() {
		return scoreSpeaker;
	}

	/**
	 * Sets the score.of the Speaker
	 * 
	 * @param score the new score
	 */
	public void setScoreSpeaker(double score) {
		numberOfScoreSpeaker = 1;
		this.scoreSpeaker = score;
	}

	/**
	 * Increment score.of the cluster
	 * 
	 * @param score the increment value
	 */
	public void incrementScoreSpeaker(double score) {
		numberOfScoreSpeaker += 1;
		this.scoreSpeaker += score;
	}

	/**
	 * Add a new score for this speaker inside the cluster.
	 * 
	 * @param score The score to add, coming from the decision system (SCT for example)
	 */
	public void addScoreCluster(double score) {
		scoreClusterList.add(new Double(score));
	}

	/**
	 * Will return each score for this speaker name.
	 * 
	 * @return The score list, scores are mainly coming from the SCT
	 */
	public ArrayList<Double> getScoreClusterList() {
		return scoreClusterList;
	}

	/**
	 * Gets the product of scores.
	 * 
	 * @return the score
	 */
	public double getScore() {
		return scoreSpeaker * scoreCluster;
	}

	/**
	 * Gets the sum score.
	 * 
	 * @return the sum score
	 */
	public double getSumScore() {
		double sum = 0;
		for (Double v : scoreClusterList) {
			sum += v;
		}
		return sum;
	}

	/**
	 * print debug information.
	 */
	public void debug() {
		/*
		 * logger.finer("\t name=" + name + " score=" + getScore() + " scoreSpeaker=" + scoreSpeaker + " scoreCluster=" + scoreCluster + " produit=" + getScore() * getScore() + " numberOfScoreCluster=" + numberOfScoreCluster + " |");
		 */
		String ch = "";
		for (Double d : getScoreClusterList()) {
			ch += d.toString() + " ";
		}

		logger.finer("\t name=" + name + " score:" + getScore() + "(" + getScoreSpeaker() + "/" + getScoreCluster()
				+ ") score list=" + ch);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SpeakerName o) {
		if (scoreCluster > o.scoreCluster) {
			return -1;
		} else {
			if (scoreCluster == o.scoreCluster) {
				return 0;
			}
		}
		return 1;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (scoreCluster == ((SpeakerName) o).scoreCluster) {
			return true;
		}
		return false;
	}

}
