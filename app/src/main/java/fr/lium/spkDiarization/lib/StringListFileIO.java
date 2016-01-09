/**
 * <p>
 * StringListFileIO
 * </p>
 *
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
 * @author <a href="mailto:gael.salaun@univ-lemans.fr">Gael Salaun</a>
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
 * Reader for a string list file.
 */

package fr.lium.spkDiarization.lib;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

/**
 * The Class StringListFileIO.
 */
public class StringListFileIO {

    /**
     * Read a string list file.
     *
     * @param filename : the name of file to read.
     * @param gzip the gzip
     *
     * @return A StringLst.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static ArrayList<String> read(String filename, boolean gzip) throws IOException {
        ArrayList<String> lst = new ArrayList<String>();
        File f_test = new File(filename);

        if (!f_test.isDirectory()) {
            BufferedReader bufferedReader;
            if (gzip == true) {
                bufferedReader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(filename))));
            } else {
                bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
            }
            try {
                while (true) {
                    String ch = bufferedReader.readLine();
                    if (ch == null) {
                        break;
                    }
                    if (ch.equals("")) {
                        break;
                    }
                    lst.add(ch);
                }
            } catch (EOFException eof) {
            }
        }
        return lst;
    }
}
