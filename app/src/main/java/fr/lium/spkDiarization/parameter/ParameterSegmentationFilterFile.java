/**
 * <p>
 * ParameterSegmentationFilterFile
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

public class ParameterSegmentationFilterFile extends ParameterSegmentationFile {
    public static int ReferenceMask = -1;
    public static int ReferenceEncodingFormat = -1;
    public static int ReferenceClusterFilterName = -1;

    private String clusterFilterName; // list of cluster name

    public ParameterSegmentationFilterFile(ArrayList<LongOpt> list, Parameter param) {
        super();
        // TODO Auto-generated constructor stub
        setMask("%s.flt.seg");
        clusterFilterName = "j";
        type = "Filter";
        ReferenceMask = param.getNextOptionIndex();
        ReferenceEncodingFormat = param.getNextOptionIndex();
        ReferenceClusterFilterName = param.getNextOptionIndex();
        addOptions(list);
    }

    public boolean readParam(int option, String optarg) {
        if (option == ReferenceMask) {
            setMask(optarg);
            return true;
        } else if (option == ReferenceEncodingFormat) {
            setFormatEncoding(optarg);
            return true;
        } else if (option == ReferenceClusterFilterName) {
            setClusterFilterName(optarg);
            return true;
        }
        return false;
    }

    public void addOptions(ArrayList<LongOpt> list) {
        list.add(new LongOpt("sFilterFormat", 1, null, ReferenceEncodingFormat));
        list.add(new LongOpt("sFilterMask", 1, null, ReferenceMask));
        list.add(new LongOpt("sFilterClusterName", 1, null, ReferenceClusterFilterName));
    }

    public String getClusterFilterName() {
        return clusterFilterName;
    }

    public void setClusterFilterName(String clusterFilterName) {
        this.clusterFilterName = clusterFilterName;
    }

    public void printFilterClusterName() {
        System.out.print("info[ParameterFilterClusterName] \t --sFilterClusterName \t name of the filterCluster = ");
        System.out.println(clusterFilterName);
    }

}
