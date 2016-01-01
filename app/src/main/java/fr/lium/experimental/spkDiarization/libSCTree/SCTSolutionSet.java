/**
 * 
 * <p>
 * SCTSolutionSet
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

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * The Class SCTSolutionSet.
 */
public class SCTSolutionSet extends ArrayList<SCTSolution> {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(SCTSolutionSet.class.getName());

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Adds the new solution.
	 * 
	 * @param size the size
	 */
	void addNewSolution(int size) {
		add(new SCTSolution(size));
	}

	/**
	 * Debug.
	 */
	public void debug() {
		logger.finer("SCTSolutionSet size:" + size());
		for (int i = 0; i < size(); i++) {
			get(i).debug();
		}
	}

}
