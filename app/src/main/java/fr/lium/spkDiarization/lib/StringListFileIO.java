/**
 * 
 * <p>
 * StringListFileIO
 * </p>
 * 
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
 * @author <a href="mailto:gael.salaun@univ-lemans.fr">Gael Salaun</a>
 * @version v2.0
 * 
 *          Copyright (c) 2007-2009 Universite du Maine. All Rights Reserved. Use is subject to license terms.
 * 
 *          THIS SOFTWARE IS PROVIDED BY THE "UNIVERSITE DU MAINE" AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *          DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *          USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *          ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 *          Reader for a string list file.
 * 
 */

package fr.lium.spkDiarization.lib;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * The Class StringListFileIO.
 */
public class StringListFileIO {

	/**
	 * Read a string list file.
	 * 
	 * @param fileName : the name of file to read.
	 * @param gzip : true if the text file will be gziped
	 * 
	 * @return A StringLst.
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static ArrayList<String> read(String fileName, boolean gzip) throws IOException {
		ArrayList<String> stringList = new ArrayList<String>();
		File testTheFile = new File(fileName);

		if (!testTheFile.isDirectory()) {
			BufferedReader bufferedReader;
			if (gzip == true) {
				bufferedReader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(fileName))));
			} else {
				bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
			}
			try {
				while (true) {
					String str = bufferedReader.readLine();
					if (str == null) {
						break;
					}
					if (str.equals("")) {
						break;
					}
					stringList.add(str);
				}
				bufferedReader.close();
			} catch (EOFException e) {
			}
		}
		return stringList;
	}

	/**
	 * Write a string list.
	 * 
	 * @param fileName the name of the text file to write
	 * @param gzip : true if the text file will be gziped
	 * @param list the list of string to write
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void write(String fileName, boolean gzip, List<String> list) throws IOException {
		File testTheFile = new File(fileName);

		if (!testTheFile.isDirectory()) {
			BufferedWriter bufferedWriter;
			if (gzip == true) {
				bufferedWriter = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(fileName))));
			} else {
				bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName)));
			}
			for (String line : list) {
				bufferedWriter.write(line);
				bufferedWriter.newLine();
			}
			bufferedWriter.close();
		}
	}

}
