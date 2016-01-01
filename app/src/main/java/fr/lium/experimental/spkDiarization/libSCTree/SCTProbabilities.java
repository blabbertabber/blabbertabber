/**
 * 
 * <p>
 * SCTProbabilities
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

import java.util.TreeMap;
import java.util.logging.Logger;

import fr.lium.experimental.spkDiarization.libNamedSpeaker.SpeakerNameUtils;

/**
 * The Class SCTProbabilities.
 */
public class SCTProbabilities extends TreeMap<String, Double> {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(SCTProbabilities.class.getName());

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Debug.
	 */
	public void debug() {
		logger.fine("cur=" + get(SpeakerNameUtils.CURRENT) + " prev=" + get(SpeakerNameUtils.PREVIOUS) + " next="
				+ get(SpeakerNameUtils.NEXT));
	}

	/**
	 * Gets the max key.
	 * 
	 * @return the max key
	 */
	public String getMaxKey() {

		String key = SpeakerNameUtils.CURRENT;
		Double value = this.get(SpeakerNameUtils.CURRENT);

		if (this.get(SpeakerNameUtils.NEXT) > value) {
			value = this.get(SpeakerNameUtils.NEXT);
			key = SpeakerNameUtils.NEXT;
		}

		if (this.get(SpeakerNameUtils.PREVIOUS) > value) {
			value = this.get(SpeakerNameUtils.PREVIOUS);
			key = SpeakerNameUtils.PREVIOUS;
		}

		if (this.get(SpeakerNameUtils.OTHER) > value) {
			value = this.get(SpeakerNameUtils.OTHER);
			key = SpeakerNameUtils.OTHER;
		}

		if (SpeakerNameUtils.getNbOfLabel() > 4) {

			if (this.get(SpeakerNameUtils.INSHOW) > value) {
				value = this.get(SpeakerNameUtils.INSHOW);
				key = SpeakerNameUtils.INSHOW;
			}
		}

		return key;

	}
}
