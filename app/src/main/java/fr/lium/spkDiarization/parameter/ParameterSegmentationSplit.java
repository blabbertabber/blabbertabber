/**
 * <p>
 * ParameterSegmentationSplit
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

public class ParameterSegmentationSplit implements ParameterInterface {
    public static int ReferenceSegmentMaximumLength = -1;
    public static int ReferenceSegmentMinimumLength = -1;

    private int segmentMaximumLength;
    private int segmentMinimumLength;

    public ParameterSegmentationSplit(ArrayList<LongOpt> list, Parameter param) {
        setSegmentMaximumLength(2000);
        setSegmentMinimumLength(200);
        ReferenceSegmentMaximumLength = param.getNextOptionIndex();
        ReferenceSegmentMinimumLength = param.getNextOptionIndex();
        addOptions(list);
    }

    public boolean readParam(int option, String optarg) {
        if (option == ReferenceSegmentMaximumLength) {
            setSegmentMaximumLength(Integer.parseInt(optarg));
            return true;
        } else if (option == ReferenceSegmentMinimumLength) {
            setSegmentMinimumLength(Integer.parseInt(optarg));
            return true;
        }
        return false;
    }

    public void addOptions(ArrayList<LongOpt> list) {
        list.add(new LongOpt("sSegMaxLen", 1, null, ReferenceSegmentMaximumLength));
        list.add(new LongOpt("sSegMinLen", 1, null, ReferenceSegmentMinimumLength));
    }

    public int getSegmentMaximumLength() {
        return segmentMaximumLength;
    }

    public void setSegmentMaximumLength(int segMaxLen) {
        this.segmentMaximumLength = segMaxLen;
    }

    public int getSegmentMinimumLength() {
        return segmentMinimumLength;
    }

    public void setSegmentMinimumLength(int segMinLen) {
        this.segmentMinimumLength = segMinLen;
    }

    public void printSegmentMaximumLength() {
        System.out.println("info[ParameterSegmentationSplit] \t --sSegMaxLen = " + getSegmentMaximumLength());
    }

    public void printSegmentMinimumLength() {
        System.out.println("info[ParameterSegmentationSplit] \t --sSegMinLen = " + getSegmentMinimumLength());
    }
}
