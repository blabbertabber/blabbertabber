/**
 * <p>
 * IOFile
 * </p>
 *
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
 * @author <a href="mailto:gael.salaun@univ-lemans.fr">Gael Salaun</a>
 * @author <a href="mailto:teva.merlin@lium.univ-lemans.fr">Teva Merlin</a>
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

package fr.lium.spkDiarization.lib;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

// TODO: Auto-generated Javadoc

/**
 * The Class IO File.
 */
public class IOFile {

    /** The input stream. */
    protected DataInputStream inputStream;

    /** The output stream. */
    protected DataOutputStream outputStream;

    /** The url: the name of the file.*/
    protected URL url;

    /** The filename: The name of the file. */
    protected String filename;

    /** The input output mode. */
    protected String mode;

    /** The needs swap (little / big endian). */
    protected boolean needsSwap;

    /**
     * Instantiates a new IO file.
     *
     * @param url the url
     * @param _swap the _swap
     */
    public IOFile(URL url, boolean _swap) {
        this.url = url;
        filename = "";
        mode = "rb";
        needsSwap = _swap;
    }

    /**
     * Instantiates a new IO file.
     *
     * @param url the url
     */
    public IOFile(URL url) {
        this.url = url;
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
    public IOFile(String _filename, String _mode, boolean _swap) {
        url = null;
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
     * Close the file descriptor.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void close() throws IOException {
        if (mode.contains("r")) {
            inputStream.close();
            inputStream = null;
        } else {
            outputStream.close();
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
                if (url == null) {
                    inputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(filename)));
                } else {
                    inputStream = new DataInputStream(new BufferedInputStream(url.openStream()));
                }
            } catch (FileNotFoundException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
                System.exit(-1);
            }
        } else {
            outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(filename)));
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
        byte[] bytes = new byte[1];
        bytes[0] = inputStream.readByte();
        return (new String(bytes)).charAt(0);
    }

    /**
     * Read double.
     *
     * @return the double
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public double readDouble() throws IOException {
        double v;
        v = inputStream.readDouble();
        if (needsSwap) {
            v = Double.longBitsToDouble(Long.reverseBytes(Double.doubleToLongBits(v)));
        }
        return v;
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
        v = inputStream.readFloat();
        if (needsSwap) {
            v = Float.intBitsToFloat(Integer.reverseBytes(Float.floatToIntBits(v)));
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
        try {
            for (int i = 0; i < number; i++) {
                floats[i] = readFloat();
            }
        } catch (EOFException eofe) {
        }
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
        v = inputStream.readInt();
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
        BufferedReader d = new BufferedReader(new InputStreamReader(inputStream));
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
        v = inputStream.readLong();
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
        v = inputStream.readShort();
        if (needsSwap) {
            v = Short.reverseBytes(v);
        }
        return v;
    }

    /**
     * Read a string finished by a white-space (ie space, enter, tab).
     *
     * @return the string
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public String readString() throws IOException {
        String result = "";
        result = inputStream.readUTF();
        return result;
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
            bytes[i] = inputStream.readByte();
        }
        return new String(bytes);
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
        outputStream.writeByte(v);
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
        outputStream.writeDouble(v);
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
        outputStream.writeFloat(v);
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
        outputStream.writeInt(v);
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
        outputStream.writeShort(v);
    }

    /**
     * Write string.
     *
     * @param v the v
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void writeString(String v) throws IOException {
        outputStream.writeBytes(v);
    }

}
