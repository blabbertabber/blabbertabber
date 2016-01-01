/**
 * 
 * <p>
 * Pair
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

/**
 * The Class Path. Path of an entity in a linkSet.
 */
public class Path implements Cloneable {

	/** The id of link set. */
	protected int idOfLinkSet;

	/** The id of link. */
	protected int idOfLink;

	/**
	 * Instantiates a new path.
	 * 
	 * @param idOfLinkSet the id of link set
	 * @param idOfLink the id of link
	 */
	public Path(int idOfLinkSet, int idOfLink) {
		super();
		this.idOfLinkSet = idOfLinkSet;
		this.idOfLink = idOfLink;
	}

	/**
	 * Gets the id of link set.
	 * 
	 * @return the id of link set
	 */
	public int getIdOfLinkSet() {
		return idOfLinkSet;
	}

	/**
	 * Sets the id of link set.
	 * 
	 * @param idOfLinkSet the new id of link set
	 */
	protected void setIdOfLinkSet(int idOfLinkSet) {
		this.idOfLinkSet = idOfLinkSet;
	}

	/**
	 * Gets the id link.
	 * 
	 * @return the id link
	 */
	public int getIdLink() {
		return idOfLink;
	}

	/**
	 * Sets the id link.
	 * 
	 * @param idLink the new id link
	 */
	protected void setIdLink(int idLink) {
		this.idOfLink = idLink;
	}

	/**
	 * Compare to.
	 * 
	 * @param path the path
	 * 
	 * @return the int
	 */
	public int compareTo(Path path) {
		if (path == null) {
			return -1;
		} else {
			if (idOfLinkSet > path.idOfLinkSet) {
				return 1;
			} else if (idOfLinkSet < path.idOfLinkSet) {
				return -1;
			} else {
				if (idOfLink > path.idOfLink) {
					return 1;
				} else if (idOfLink < path.idOfLink) {
					return -1;
				} else {
					return 0;
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}

}
