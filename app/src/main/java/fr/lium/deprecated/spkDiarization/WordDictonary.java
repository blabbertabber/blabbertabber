/**
 * <p>
 * WordDictonary
 * </p>
 *
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
 * @author <a href="mailto:vincent.jousse@lium.univ-lemans.fr">Vincent Jousse</a>
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
 * not more use
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
 * @deprecated
 */
public class WordDictonary {
    public static final String Unknown = "UNK";
    protected int numberOfElements;
    protected TreeMap<String, Integer> strToInt;
    protected TreeMap<Integer, String> intToStr;

    public WordDictonary() {
        strToInt = new TreeMap<String, Integer>();
        intToStr = new TreeMap<Integer, String>();
        numberOfElements = 0;
    }

    public void add(String key) {
        strToInt.put(key, numberOfElements);
        intToStr.put(numberOfElements, key);
        if (numberOfElements < 40) {
            System.out.println(numberOfElements + " - " + key);
        }
        numberOfElements++;
    }

    public int getIndex(String key) {
        Integer idx = strToInt.get(key);
        if (idx == null) {
            return -1;
        }
        return idx;
    }

    public String getLabel(int key) {
        String word = intToStr.get(key);
        if (word == null) {
            return Unknown;
        }
        return word;
    }

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
