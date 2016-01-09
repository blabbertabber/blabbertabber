/**
 * <p>
 * LinkSet
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
 */

package fr.lium.experimental.spkDiarization.libClusteringData.transcription;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import fr.lium.spkDiarization.lib.DiarizationException;

/**
 * Link set containing the link. Store a word graph for the segment.
 *
 * @author meignier
 */

public class LinkSet implements Iterable<Link>, Cloneable {

    /** The list of link. */
    protected ArrayList<Link> list;

    /** The unique identifier of the LinkSet. */
    protected int id;

    /** The information. */
    protected TreeMap<String, Object> information;

    /**
     * Instantiates a new link set.
     *
     * @param id the id
     */
    public LinkSet(int id) {
        list = new ArrayList<Link>();
        this.id = id;
        this.information = new TreeMap<String, Object>();
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        LinkSet clone = (LinkSet) super.clone();
        clone.list = new ArrayList<Link>();
        clone.information = new TreeMap<String, Object>();

        for (Link link : list) {
            clone.list.add(link);
        }
        /*
		for (String key : information.descendingKeySet()) {
			Object value = information.get(key);
			clone.information.put(key, value);
		}
		*/

        Set<String> keys = information.keySet();
        for (String key : keys) {
            Object value = information.get(key);
            clone.information.put(key, value);
        }

        return clone;
    }

    /**
     * Sets an information.
     *
     * @param key the key
     * @param value the value
     */
    public void setInformation(String key, Object value) {
        this.information.put(key, value);
    }

    /**
     * Gets the information.
     *
     * @return the information
     */
    public TreeMap<String, Object> getInformation() {
        return this.information;
    }

    /**
     * Gets the information.
     *
     * @param key the key
     *
     * @return the information
     */
    public Object getInformation(Object key) {
        return this.information.get(key);
    }

    /**
     * Gets the information.
     *
     * @param key the key
     *
     * @return the information
     */
    public String getInformation(String key) {
        if (this.information.get(key) != null) {
            return this.information.get(key).toString();
        } else {
            return null;
        }
    }

    /**
     * Size.
     *
     * @return the int
     */
    public int size() {
        return list.size();
    }

    /**
     * New link.
     *
     * @param idLink the identifier of link
     * @param linkStart the link start position
     * @param linkEnd the link end position
     * @param linkType the link type
     * @param linkProba the link probability
     * @param word the word
     *
     * @return the link
     */
    public Link newLink(int idLink, int linkStart, int linkEnd, String linkType, double linkProba, String word) {
        Link link = new Link(idLink, linkStart, linkEnd, linkType, linkProba, word);
        list.add(link);
        return link;
    }

    /**
     * Adds the link at position #index.
     *
     * @param index the index
     * @param link the link
     */
    public void add(int index, Link link) {
        list.add(index, link);
    }

    /**
     * Adds the link at the and of the container.
     *
     * @param link the link
     *
     * @return true, if successful
     */
    public boolean add(Link link) {
        return list.add(link);
    }

    /**
     * Gets the last link.
     *
     * @return the last link
     */
    public Link getLastLink() {
        return list.get(list.size() - 1);
    }

    /**
     * Gets the link at position #index.
     *
     * @param index the index
     *
     * @return the link
     */
    public Link getLink(int index) {
        return list.get(index);
    }

    /**
     * Find token.
     *
     * @param tokens the tokens
     * @param index the index
     * @param regex the regex
     *
     * @return the int
     *
     * @throws DiarizationException the diarization exception
     */
    protected int findToken(String[] tokens, int index, String regex) throws DiarizationException {
        while (tokens[index].matches(regex) == false) {
            System.err.println("token : " + index + " = " + tokens[index]);
            index++;
            if (tokens.length == index) {
                throw new DiarizationException("LinkSet: findToken() token not found, " + regex);
            }
        }
        System.err.println("token find (" + regex + ") : " + index + " = " + tokens[index]);
        return index;
    }

    /**
     * Debug.
     */
    public void debug() {
        int size = list.size();
        System.err.println("<linkSet count=\"" + size + "\"> ");
        for (int i = 0; i < size; i++) {
            list.get(i).debug();
        }
        Iterator<String> it = this.information.keySet().iterator();
        if (information.size() > 0) {
            System.err.println("\t<infos>");
            while (it.hasNext()) {
                String key = it.next().toString();
                System.err.println("\t\t<info value=\"" + key + "\" />");
            }
            System.err.println("\t</infos>");
        }
        System.err.println(" </linkSet>");

    }

    public String SCTTrainInformation(String show, int start, int index) {
        String key = show + "#" + start + "#" + index;
        String words = "";
        String questions = "";
        int size = list.size();

        for (int i = 0; i < size; i++) {
            Link link = list.get(i);
            if (link.haveEntity() == true) {
                words = words + link.entity.getType() + " ";
            } else {
                words = words + link.word + " ";
            }
        }

        for (String info : information.keySet()) {
            questions = questions + info + " ";
        }

        String res = key + " " + words;
        if (questions.equals("") == false) {
            res += "\n& " + questions;
        }

        return res;
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

    /* (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<Link> iterator() {
        return list.iterator();
    }

    /**
     * Removes the.
     *
     * @param i the i
     */
    public void remove(int i) {
        list.remove(i);
    }

}
