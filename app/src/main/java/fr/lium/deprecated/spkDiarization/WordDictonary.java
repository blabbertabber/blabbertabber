/**
 * 
 * <p>
 * WordDictonary
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
 *          not more use
 */

package fr.lium.deprecated.spkDiarization;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.TreeMap;

import fr.lium.spkDiarization.lib.IOFile;

/**
 * The Class WordDictonary.
 * 
 * @deprecated
 */
@Deprecated
public class WordDictonary {

	/** The number of elements. */
	protected int numberOfElements;

	/** The str to int. */
	protected TreeMap<String, Integer> strToInt;

	/** The int to str. */
	protected TreeMap<Integer, String> intToStr;

	/** The Constant Unknown. */
	public static final String Unknown = "UNK";

	/**
	 * Instantiates a new word dictonary.
	 */
	public WordDictonary() {
		strToInt = new TreeMap<String, Integer>();
		intToStr = new TreeMap<Integer, String>();
		numberOfElements = 0;
	}

	/**
	 * Adds the.
	 * 
	 * @param key the key
	 */
	public void add(String key) {
		strToInt.put(key, numberOfElements);
		intToStr.put(numberOfElements, key);
		if (numberOfElements < 40) {
			System.out.println(numberOfElements + " - " + key);
		}
		numberOfElements++;
	}

	/**
	 * Gets the index.
	 * 
	 * @param key the key
	 * @return the index
	 */
	public int getIndex(String key) {
		Integer idx = strToInt.get(key);
		if (idx == null) {
			return -1;
		}
		return idx;
	}

	/**
	 * Gets the label.
	 * 
	 * @param key the key
	 * @return the label
	 */
	public String getLabel(int key) {
		String word = intToStr.get(key);
		if (word == null) {
			return Unknown;
		}
		return word;
	}

	/**
	 * Read.
	 * 
	 * @param name the name
	 * @param mask the mask
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void read(String name, String mask) throws IOException {
		String filename = IOFile.getFilename(mask, name);
		System.out.println("## Opening : " + filename);
		// BufferedReader bufferedReader = new BufferedReader(new FileReader(filename));
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
		String line;
		while ((line = bufferedReader.readLine()) != null) {

			String[] tokens = line.split("\\s+");
			add(tokens[0]);
		}
	}

	/**
	 * Debug.
	 */
	public void debug() {
		System.out.print("debug[WordDictonary] \t ");
		Iterator<String> it = strToInt.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			int value = strToInt.get(key);
			System.out.println(key + "=" + value + " / " + intToStr.get(value));
		}
	}
}
