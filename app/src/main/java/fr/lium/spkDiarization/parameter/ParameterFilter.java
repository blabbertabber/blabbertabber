/**
 * <p>
 * ParameterFilter
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

import java.util.ArrayList;

import gnu.getopt.LongOpt;

public class ParameterFilter implements ParameterInterface {
    public static int ReferenceSilenceMinimumLength = -1;
    public static int ReferenceSpeechMinimumLength = -1;
    public static int ReferenceSegmentPadding = -1;

    private int silenceMinimumLength;
    private int speechMinimumLength;
    private int segmentPadding;

    public ParameterFilter(ArrayList<LongOpt> list, Parameter param) {
        setSilenceMinimumLength(150);
        setSpeechMinimumLength(150);
        setSegmentPadding(25);
        ReferenceSilenceMinimumLength = param.getNextOptionIndex();
        ReferenceSpeechMinimumLength = param.getNextOptionIndex();
        ReferenceSegmentPadding = param.getNextOptionIndex();
        addOptions(list);
    }

    public boolean readParam(int option, String optarg) {
        if (option == ReferenceSilenceMinimumLength) {
            setSilenceMinimumLength(Integer.parseInt(optarg));
            return true;
        } else if (option == ReferenceSpeechMinimumLength) {
            setSpeechMinimumLength(Integer.parseInt(optarg));
            return true;
        } else if (option == ReferenceSegmentPadding) {
            setSegmentPadding(Integer.parseInt(optarg));
            return true;
        }
        return false;
    }

    public void addOptions(ArrayList<LongOpt> list) {
        list.add(new LongOpt("fltSegMinLenSil", 1, null, ReferenceSilenceMinimumLength));
        list.add(new LongOpt("fltSegMinLenSpeech", 1, null, ReferenceSpeechMinimumLength));
        list.add(new LongOpt("fltSegPadding", 1, null, ReferenceSegmentPadding));
    }

    public int getSilenceMinimumLength() {
        return silenceMinimumLength;
    }

    public void setSilenceMinimumLength(int segMinLenSil) {
        this.silenceMinimumLength = segMinLenSil;
    }

    public int getSpeechMinimumLength() {
        return speechMinimumLength;
    }

    public void setSpeechMinimumLength(int segMinLenSpeech) {
        this.speechMinimumLength = segMinLenSpeech;
    }

    public int getSegmentPadding() {
        return segmentPadding;
    }

    public void setSegmentPadding(int segPadding) {
        this.segmentPadding = segPadding;
    }

    public void printSilenceMinimumLength() {
        System.out.println("info[ParameterFilter] \t --fltSegMinLenSil = " + getSilenceMinimumLength());
    }

    public void printSpeechMinimumLength() {
        System.out.println("info[ParameterFilter] \t --fltSegMinLenSpeech = " + getSpeechMinimumLength());
    }

    public void printSegmentPadding() {
        System.out.println("info[ParameterFilter] \t --fltSegPadding = " + getSegmentPadding());
    }
}