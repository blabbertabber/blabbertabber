/**
 * <p>
 * Entity
 * </p>
 *
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
 * @version v2.0
 * <p/>
 * Copyright (c) 2007-2009 Universite du Maine. All Rights Reserved. Use is subject to license terms.
 * <p/>
 * THIS SOFTWARE IS PROVIDED BY THE "UNIVERSITE DU MAINE" AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 * <p/>
 * An entity set, ie a container of entity.
 * This class is employed in conjunction of EntitySet, Link and LinkSet.
 */
package fr.lium.experimental.spkDiarization.libClusteringData.transcription;

import java.util.TreeSet;

/**
 * The Class EntitySet.
 */
public class EntitySet extends TreeSet<Entity> {

    public final static String TypePersonne = "entity.pers";
    public final static String TypeTargetSpeaker = "entity.pers.target";
    public final static String TypeOrganization = "entity.org";
    public final static String TypeOrganizationStation = "entity.org.station";
    public final static String TypeLocalization = "entity.gsp";
    public final static String TypeLocalization2 = "entity.loc";
    public final static String TypeTime = "entity.time";
    public final static String TypeTimeHours = "entity.time.hours";
    public final static String TypeTimeDate = "entity.time.date";
    public final static String TypeAmount = "entity.amount";
    public final static String TypeFonction = "entity.fonc";
    public final static String TypeUnknown = "entity.unk";
    public final static String TypeProduction = "entity.prod";

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
