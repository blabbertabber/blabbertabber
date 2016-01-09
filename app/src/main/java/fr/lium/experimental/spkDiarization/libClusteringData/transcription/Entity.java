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
 * An entity. This class is employed in conjunction of EntitySet, Link and LinkSet.
 */
package fr.lium.experimental.spkDiarization.libClusteringData.transcription;

import java.util.ArrayList;
import java.util.Iterator;

import fr.lium.spkDiarization.lib.DiarizationException;

/**
 * The Class Entity.
 */
public class Entity implements Comparable<Entity>, Iterable<Path>, Cloneable {
    /** type of entity*/
    protected String type;
    /** The link set. */
    protected LinkSet linkSet;
    /** path in the graph that identify the entity*/
    ArrayList<Path> listOfLink;

    /**
     * Instantiates a new entity.
     */
    public Entity() {
        type = "empty";
        listOfLink = new ArrayList<Path>();
    }

    /**
     * Instantiates a new entity.
     *
     * @param linkSet the link set
     * @param type the type
     *
     * @throws DiarizationException the diarization exception
     */
    public Entity(LinkSet linkSet, String type) throws DiarizationException {
        this.type = type;
        this.linkSet = linkSet;
        listOfLink = new ArrayList<Path>();
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        Entity copy = (Entity) super.clone();
        copy.listOfLink = new ArrayList<Path>();
        for (Path path : listOfLink) {
            copy.listOfLink.add((Path) path.clone());
        }
        return copy;
    }

    /**
     * Gets the type of the entity.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type of entity.
     *
     * @param type the new type
     */
    public void setType(String type) {
        this.type = type;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Entity o) {
        try {
            Path oPath = o.listOfLink.get(0);
            Path path = listOfLink.get(0);
            return path.compareTo(oPath);
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Debug.
     */
    public void debug() {
        System.err.println("<entity type=\"" + type + "\" >");
        for (Path path : this) {
            System.err.println("\t<path graph=\"" + path.getIdOfLinkSet() + "\" link=\"" + path.getIdLink() + "\" />");
        }
        System.err.println("</entity>");
    }

    /**
     * Adds the path.
     *
     * @param graph the graph
     * @param link the link
     *
     * @return true, if successful
     */
    public boolean addPath(int graph, int link) {
        return listOfLink.add(new Path(graph, link));
    }

    /* (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    public Iterator<Path> iterator() {
        return listOfLink.iterator();
    }

    /**
     * Gets the list of words.
     *
     * @return the list of words
     */
    public String getListOfWords() {
        String result = "";
        for (Path path : this) {
            if (result != "") {
                result += " ";
            }
            result += linkSet.getLink(path.idOfLink).word;
        }
        return result;
    }

}
