/**
 * 
 * <p>
 * IOFile
 * </p>
 * 
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
 * @author <a href="mailto:gael.salaun@univ-lemans.fr">Gael Salaun</a>
 * @author <a href="mailto:teva.merlin@lium.univ-lemans.fr">Teva Merlin</a>
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

package fr.lium.spkDiarization.lib;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

// TODO: Auto-generated Javadoc
/**
 * The Class IO File.
 */
public class IOFile {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(IOFile.class.getName());

	/** The input stream. */
	protected DataInputStream dataInputStream;

	/** The output stream. */
	protected DataOutputStream dataOutputStream;

	/** The url: the name of the file. */
	// protected URL url;
	protected InputStream inputStream;

	/** The filename: The name of the file. */
	protected String filename;

	/** The input output mode. */
	protected String mode;

	/** The needs swap (little / big endian). */
	protected boolean needsSwap;

	/**
	 * Instantiates a new iO file.
	 * 
	 * @param inputStream the input stream
	 * @param _swap the _swap
	 */
	public IOFile(InputStream inputStream, boolean _swap) {
		this.inputStream = inputStream;
		filename = "";
		mode = "rb";
		needsSwap = _swap;
	}

	/**
	 * Instantiates a new iO file.
	 * 
	 * @param inputStream the input stream
	 */
	public IOFile(InputStream inputStream) {
		this.inputStream = inputStream;
		filename = "";
		mode = "rb";
		needsSwap = false;
	}

	/**
	 * Instantiates a new IO file.
	 * 
	 * @param _filename the _filename
	 * @param _mode the _mode
	 * @param _swap the _swap
	 */
// public IOFile(URL url, boolean _swap) {
// this.url = url;
// filename = "";
// mode = "rb";
// needsSwap = _swap;
// }

	/**
	 * Instantiates a new IO file.
	 * 
	 * @param url the url
	 */
// public IOFile(URL url) {
// this.url = url;
// filename = "";
// mode = "rb";
// needsSwap = false;
// }

	/**
	 * Instantiates a new IO file.
	 * 
	 * @param _filename the _filename
	 * @param _mode the _mode
	 * @param _swap the _swap
	 */
	public IOFile(String _filename, String _mode, boolean _swap) {
// url = null;
		inputStream = null;
		filename = _filename;
		mode = _mode;
		needsSwap = _swap;
	}

	/**
	 * Instantiates a new IO file.
	 * 
	 * @param _filename the _filename
	 * @param _mode the _mode
	 */
	public IOFile(String _filename, String _mode) {
		inputStream = null;
		filename = _filename;
		mode = _mode;
		needsSwap = false;
	}

	/**
	 * Gets the filename.
	 * 
	 * @param mask the mask, %s is replaced by
	 * @param name the name of file
	 * 
	 * @return the filename
	 */
	public static String getFilename(String mask, String name) {
		return mask.replace("%s", name);
	}

	/**
	 * Gets the filename.
	 * 
	 * @param mask the mask
	 * @param iteration the iteration
	 * @return the filename
	 */
	public static String getFilename(String mask, Integer iteration) {
		return mask.replace("%i", iteration.toString());
	}

	/**
	 * Close the file descriptor.
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void close() throws IOException {
		if (mode.contains("r")) {
			dataInputStream.close();
			dataInputStream = null;
		} else {
			dataOutputStream.close();
		}
	}

	/**
	 * Get the length of the file.
	 * 
	 * @return The length of the file
	 */
	public long len() {
		return (new File(filename)).length();
	}

	/**
	 * Open the file descriptor.
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void open() throws IOException {

		if (mode.contains("r")) {
			try {
// if (url == null) {
				if (inputStream == null) {
					dataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(filename)));
				} else {
					dataInputStream = new DataInputStream(new BufferedInputStream(inputStream));
				}
			} catch (FileNotFoundException e) {
				logger.log(Level.SEVERE, "filename=" + filename + " inputStream=" + inputStream, e);
				e.printStackTrace();
				System.exit(-1);
			}
		} else {
			dataOutputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(filename)));
		}
	}

	/**
	 * Read char.
	 * 
	 * @return the char
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public char readChar() throws IOException {
		return dataInputStream.readChar();
	}

	/**
	 * Read byte.
	 * 
	 * @return the byte
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public byte readByte() throws IOException {
		return dataInputStream.readByte();
	}

	/**
	 * Read double.
	 * 
	 * @return the double
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public double readDouble() throws IOException {
		long v;
		v = dataInputStream.readLong();

		if (needsSwap) {
			v = Long.reverseBytes(v);
		}
		return Double.longBitsToDouble(v);
	}

	/**
	 * Read float.
	 * 
	 * @return the float
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public float readFloat() throws IOException {
		float v;
		v = dataInputStream.readFloat();
		if (needsSwap) {
			v = Float.intBitsToFloat(Integer.reverseBytes(Float.floatToRawIntBits(v)));
		}
		return v;
	}

	/**
	 * Read float array.
	 * 
	 * @param floats the floats
	 * @param number the number
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void readFloatArray(float[] floats, int number) throws IOException {
// try {
		for (int i = 0; i < number; i++) {
			floats[i] = readFloat();
		}
// } catch (EOFException eofe) {
// logger.log(Level.SEVERE, "", e);
// e.printStackTrace();
// }
	}

	/**
	 * Read int.
	 * 
	 * @return the int
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public int readInt() throws IOException {
		int v;
		v = dataInputStream.readInt();
		if (needsSwap) {
			v = Integer.reverseBytes(v);
		}
		return v;
	}

	/**
	 * Read a string finished by a enter character.
	 * 
	 * @return the string
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public String readLine() throws IOException {
		BufferedReader d = new BufferedReader(new InputStreamReader(dataInputStream));
		String result = d.readLine();
		d.close();
		return result;
	}

	/**
	 * Read long.
	 * 
	 * @return the long
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public long readLong() throws IOException {
		long v;
		v = dataInputStream.readLong();
		if (needsSwap) {
			v = Long.reverse(v);
		}
		return v;
	}

	/**
	 * Read short.
	 * 
	 * @return the short
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public short readShort() throws IOException {
		short v;
		v = dataInputStream.readShort();
		if (needsSwap) {
			v = Short.reverseBytes(v);
		}
		return v;
	}

	/**
	 * Read a string of length \e len.
	 * 
	 * @param len the len
	 * 
	 * @return the string
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public String readString(int len) throws IOException {
		byte[] bytes = new byte[len];
		for (int i = 0; i < len; i++) {
			bytes[i] = dataInputStream.readByte();
		}
		return new String(bytes, Charset.forName("UTF-8"));
	}

	/**
	 * Sets the swap.
	 * 
	 * @param _swap the new swap
	 */
	public void setSwap(boolean _swap) {
		needsSwap = _swap;
	}

	/**
	 * Write char.
	 * 
	 * @param v the v
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeChar(char v) throws IOException {
		dataOutputStream.writeChar(v);
	}

	/**
	 * Write double.
	 * 
	 * @param v the v
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeDouble(double v) throws IOException {
		if (needsSwap) {
			v = Double.longBitsToDouble(Long.reverseBytes(Double.doubleToLongBits(v)));
		}
		dataOutputStream.writeDouble(v);
	}

	/**
	 * Write float.
	 * 
	 * @param v the v
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeFloat(float v) throws IOException {
		if (needsSwap) {
			v = Float.intBitsToFloat(Integer.reverseBytes(Float.floatToIntBits(v)));
		}
		dataOutputStream.writeFloat(v);
	}

	/**
	 * Write float array.
	 * 
	 * @param floats the floats
	 * @param number the number
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeFloatArray(float[] floats, int number) throws IOException {
		for (int i = 0; i < number; i++) {
			writeFloat(floats[i]);
		}
	}

	/**
	 * Write int.
	 * 
	 * @param v the v
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeInt(int v) throws IOException {
		if (needsSwap) {
			v = Integer.reverseBytes(v);
		}
		dataOutputStream.writeInt(v);
	}

	/**
	 * Write short.
	 * 
	 * @param v the v
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeShort(short v) throws IOException {
		if (needsSwap) {
			v = Short.reverseBytes(v);
		}
		dataOutputStream.writeShort(v);
	}

	/**
	 * Write string.
	 * 
	 * @param v the v
	 * @param len the len
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeString(String v, int len) throws IOException {
		byte[] stringAsUTF8 = v.getBytes("UTF-8");
		dataOutputStream.write(stringAsUTF8, 0, len);
	}

	/**
	 * Write string.
	 * 
	 * @param v the v
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeString(String v) throws IOException {
		byte[] stringAsUTF8 = v.getBytes("UTF-8");
		dataOutputStream.write(stringAsUTF8, 0, stringAsUTF8.length);
	}

	/**
	 * Write string.
	 * 
	 * @param v the v
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeStringAndLenght(String v) throws IOException {
		byte[] stringAsUTF8 = v.getBytes("UTF-8");
		dataOutputStream.writeInt(stringAsUTF8.length);
		dataOutputStream.write(stringAsUTF8, 0, stringAsUTF8.length);
	}

}
