/**
 * 
 * <p>
 * SCTSolution
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
 */

package fr.lium.experimental.spkDiarization.libSCTree;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Class SCTSolution.
 */
public class SCTSolution implements Cloneable {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(SCTSolution.class.getName());

	/** The used. */
	boolean[] used;

	/** The current. */
	int current;

	/** The probabilities. */
	SCTProbabilities probabilities;

	/** The gap before. */
	boolean[] gapBefore;

	/** The gap after. */
	boolean[] gapAfter;

	/** The size. */
	int size;

	/**
	 * Instantiates a new sCT solution.
	 * 
	 * @param size the size
	 */
	public SCTSolution(int size) {
		this.size = size;
		used = new boolean[size];
		gapBefore = new boolean[size];
		gapAfter = new boolean[size];
		for (int i = 0; i < size; i++) {
			used[i] = false;
			gapBefore[i] = false;
			gapAfter[i] = false;
		}
		current = -1;
		probabilities = null;
	}

	/**
	 * Gets the size.
	 * 
	 * @return the size
	 */
	public int getSize() {
		return size;
	}

	/**
	 * Checks if is used.
	 * 
	 * @param key the key
	 * @return true, if is used
	 */
	public boolean isUsed(int key) {
		return (used[key]);
	}

	/**
	 * Gets the probabilities.
	 * 
	 * @return the probabilities
	 */
	public SCTProbabilities getProbabilities() {
		return probabilities;
	}

	/**
	 * Sets the probabilities.
	 * 
	 * @param probabilities the new probabilities
	 */
	public void setProbabilities(SCTProbabilities probabilities) {
		this.probabilities = probabilities;
	}

	/**
	 * Checks if is closed.
	 * 
	 * @return true, if is closed
	 */
	public boolean isClosed() {
		return (probabilities != null);
	}

	/**
	 * Gets the current.
	 * 
	 * @return the current
	 */
	protected int getCurrent() {
		return current;
	}

	/**
	 * Sets the current.
	 * 
	 * @param index the index
	 * @param usage the usage
	 * @param gapBefore the gap before
	 * @param gapAfter the gap after
	 */
	protected void setCurrent(int index, boolean usage, boolean gapBefore, boolean gapAfter) {
		current = index;
		used[current] = usage;
		this.gapAfter[current] = gapAfter;
		this.gapBefore[current] = gapBefore;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected Object clone() {
		SCTSolution result = null;
		try {
			result = (SCTSolution) (super.clone());
		} catch (CloneNotSupportedException e) {
			logger.log(Level.SEVERE, "", e);
			e.printStackTrace();
			return null;
		}
		result.used = new boolean[size];
		result.gapAfter = new boolean[size];
		result.gapBefore = new boolean[size];
		for (int i = 0; i < used.length; i++) {
			result.used[i] = used[i];
			result.gapAfter[i] = gapAfter[i];
			result.gapBefore[i] = gapBefore[i];
		}

		return result;
	}

	/**
	 * Debug.
	 */
	public void debug() {
		logger.finer("SCTSolution current:" + current);
		logger.finer("SCTSolution solution: ");
		for (int i = 0; i < used.length; i++) {
			logger.finer(" " + i + "=" + used[i]);
		}
		logger.finer(probabilities.toString());
		logger.finer("SCTSolution -------------------");

	}

	/**
	 * Checks if is before gap.
	 * 
	 * @param i the i
	 * @return the beforeGap
	 */
	public boolean isBeforeGap(int i) {
		return gapBefore[i];
	}

	/**
	 * Sets the before gap.
	 * 
	 * @param i the i
	 * @param beforeGap the beforeGap to set
	 */
	public void setBeforeGap(int i, boolean beforeGap) {
		this.gapBefore[i] = beforeGap;
	}

	/**
	 * Checks if is after gap.
	 * 
	 * @param i the i
	 * @return the afterGap
	 */
	public boolean isAfterGap(int i) {
		return gapAfter[i];
	}

	/**
	 * Sets the after gap.
	 * 
	 * @param i the i
	 * @param afterGap the afterGap to set
	 */
	public void setAfterGap(int i, boolean afterGap) {
		this.gapAfter[i] = afterGap;
	}

}
