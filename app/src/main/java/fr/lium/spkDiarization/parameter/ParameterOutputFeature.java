/**
 * <p>
 * ParameterOutputFeature
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

public class ParameterOutputFeature extends ParameterFeature {
    public static int ReferenceMask = -1;
    public static int ReferenceDescription = -1;
    public static int ReferenceMemoryOccupationRate = -1;

    public ParameterOutputFeature(ArrayList<LongOpt> list, Parameter param) {
        super();
        setFeatureMask("%s.out.mfcc");
        setType("Output");
        ReferenceMask = param.getNextOptionIndex();
        ReferenceDescription = param.getNextOptionIndex();
        ReferenceMemoryOccupationRate = param.getNextOptionIndex();
        addOptions(list);

    }

    public boolean readParam(int option, String optarg) {
        if (option == ReferenceMask) {
            setFeatureMask(optarg);
            return true;
        } else if (option == ReferenceDescription) {
            setFeaturesDescription(optarg);
            return true;
        } else if (option == ReferenceMemoryOccupationRate) {
            setMemoryOccupationRate(Double.valueOf(optarg));
            return true;
        }
        return false;
    }

    public void addOptions(ArrayList<LongOpt> list) {
        list.add(new LongOpt("f" + getType() + "Mask", 1, null, ReferenceMask));
        list.add(new LongOpt("f" + getType() + "Desc", 1, null, ReferenceDescription));
        list.add(new LongOpt("f" + getType() + "MemoryOccupationRate", 1, null, ReferenceMemoryOccupationRate));
    }

}
