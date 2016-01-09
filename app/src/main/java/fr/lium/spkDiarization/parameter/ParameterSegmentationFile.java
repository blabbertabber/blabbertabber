/**
 * <p>
 * ParameterSegmentationFile
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
 * not more use
 */

package fr.lium.spkDiarization.parameter;

import java.nio.charset.Charset;
import java.util.StringTokenizer;

public abstract class ParameterSegmentationFile implements ParameterInterface {
    public static String[] SegmentationFormatString = {"seg", "bck", "ctl", "saus.seg", "seg.xml", "media.xml"};

    ;
    public static String[] SegmentationEncodingString = {"ISO-8859-1", "UTF8"};
    protected String type;
    private SegmentationFormat format;
    private Charset encoding;
    private String mask;
    public ParameterSegmentationFile() {
        setFormat(SegmentationFormat.FILE_SEG);
        encoding = Parameter.DefaultCharset;
        //setMask("%s.in.seg");
        setMask("");
        type = "seg";
    }

    public SegmentationFormat getFormat() {
        return format;
    }

    private void setFormat(SegmentationFormat format) {
        this.format = format;
    }

    public Charset getEncoding() {
        return encoding;
    }

    private void setEncoding(String encoding) {
        this.encoding = Charset.forName(encoding);
    }

    public String getMask() {
        return mask;
    }

    public void setMask(String mask) {
        this.mask = mask;
    }

    public void setFormatEncoding(String optarg) {
        StringTokenizer stok = new StringTokenizer(optarg, ",");
        int cpt = 0;
        while (stok.hasMoreTokens()) {
            String ch = stok.nextToken();
            cpt++;
            if (cpt == 1) {
                if (ch.equals(SegmentationFormatString[SegmentationFormat.FILE_SEG.ordinal()])) {
                    setFormat(SegmentationFormat.FILE_SEG);
                } else if (ch.equals(SegmentationFormatString[SegmentationFormat.FILE_BCK.ordinal()])) {
                    setFormat(SegmentationFormat.FILE_BCK);
                } else if (ch.equals(SegmentationFormatString[SegmentationFormat.FILE_CTL.ordinal()])) {
                    setFormat(SegmentationFormat.FILE_CTL);
                } else if (ch.equals(SegmentationFormatString[SegmentationFormat.FILE_XML_EPAC.ordinal()])) {
                    setFormat(SegmentationFormat.FILE_XML_EPAC);
                } else if (ch.equals(SegmentationFormatString[SegmentationFormat.FILE_XML_MEDIA.ordinal()])) {
                    setFormat(SegmentationFormat.FILE_XML_MEDIA);
                }
            }
            if (cpt == 2) {
                setEncoding(ch);
            }
        }

    }

    public void printMask() {
        System.out.print("info[ParameterSegmentationFile-" + type + "] \t --s" + type + "Mask \t segmentation mask = ");
        System.out.println(mask);
    }

    public void printEncodingFormat() {
        String formatList = SegmentationFormatString[0];
        for (int i = 1; i < SegmentationFormatString.length; i++) {
            formatList += "," + SegmentationFormatString[i];
        }
        String encodingList = SegmentationEncodingString[0];
        for (int i = 1; i < SegmentationEncodingString.length; i++) {
            encodingList += "," + SegmentationEncodingString[i];
        }

        System.out.print("info[ParameterSegmentationFile-" + type + " ([" + formatList + "],[" + encodingList + "])] \t --s" + type + "Format \t segmentation format = ");
        System.out.println(SegmentationFormatString[format.ordinal()] + "," + encoding.name());
    }

    public void print() {
        printMask();
        printEncodingFormat();
    }

    // Type of segmentation file.
    public enum SegmentationFormat {
        FILE_SEG, FILE_BCK, FILE_CTL, FILE_SAUSAGE, FILE_XML_EPAC, FILE_XML_MEDIA
    }
}