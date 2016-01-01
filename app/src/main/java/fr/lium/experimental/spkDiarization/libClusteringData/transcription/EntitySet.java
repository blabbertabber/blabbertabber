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
 *          An entity set, ie a container of entity. This class is employed in conjunction of EntitySet, Link and LinkSet.
 */
package fr.lium.experimental.spkDiarization.libClusteringData.transcription;

import java.util.TreeSet;

/**
 * The Class EntitySet.
 */
public class EntitySet extends TreeSet<Entity> {

	/** The Constant TypePersonne. */
	public final static String TypePersonne = "entity.pers";

	/** The Constant TypeTargetSpeaker. */
	public final static String TypeTargetSpeaker = "entity.pers.target";

	/** The Constant TypeEster2Organization. */
	public final static String TypeEster2Organization = "entity.org";

	/** The Constant TypeEster2OrganizationStation. */
	public final static String TypeEster2OrganizationStation = "entity.org.station";

	/** The Constant TypeEtapeLocalizationTown. */
	public final static String TypeEtapeLocalizationTown = "entity.loc.adm.town";

	/** The Constant TypeEtapeLocalizationReg. */
	public final static String TypeEtapeLocalizationReg = "entity.loc.adm.reg";

	/** The Constant TypeEster2Localization. */
	public final static String TypeEster2Localization = "entity.gsp";

	/** The Constant TypeEster2Localization2. */
	public final static String TypeEster2Localization2 = "entity.loc";

	/** The Constant TypeEster2Time. */
	public final static String TypeEster2Time = "entity.time";

	/** The Constant TypeEster2TimeHours. */
	public final static String TypeEster2TimeHours = "entity.time.hours";

	/** The Constant TypeEster2TimeDate. */
	public final static String TypeEster2TimeDate = "entity.time.date";

	/** The Constant TypeEster2Amount. */
	public final static String TypeEster2Amount = "entity.amount";

	/** The Constant TypeEster2Fonction. */
	public final static String TypeEster2Fonction = "entity.func";

	/** The Constant TypeUnknown. */
	public final static String TypeUnknown = "entity.unk";

	/** The Constant TypeEster2Production. */
	public final static String TypeEster2Production = "entity.prod";

	/** The Constant TypeEtapeProductionMedia. */
	public final static String TypeEtapeProductionMedia = "entity.prod.media";

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Debug.
	 */
	public void debug() {
		for (Entity entity : this) {
			entity.debug();
		}
	}

}
