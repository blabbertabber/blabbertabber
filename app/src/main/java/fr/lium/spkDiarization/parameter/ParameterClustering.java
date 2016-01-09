/**
 * <p>
 * ParameterClustering
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

public class ParameterClustering implements ParameterInterface {
    public static String[] ClustMethodString = {"l", "h", "c", "ce", "icr", "kl2", "h2", "gd", "gdgmm", "r", "t", "glr"};

    ;
    public static int ReferenceThreshold = -1;
    public static int ReferenceMethod = -1;
    public static int ReferenceMaximumOfMerge = -1;
    public static int ReferenceMinmumOfCluster = -1;
    public static int ReferenceMinimumOfClusterLength = -1;
    ClusteringMethod method; // Clustering method.
    private double threshold; // Clustering threshold.
    private int maximumOfMerge;
    private int minmumOfCluster;
    private int minimumOfClusterLength;
    public ParameterClustering(ArrayList<LongOpt> list, Parameter param) {
        threshold = 1.0;
        method = ClusteringMethod.CLUST_L_BIC;
        maximumOfMerge = Integer.MAX_VALUE;
        minmumOfCluster = 0;
        minimumOfClusterLength = Integer.MAX_VALUE;
        ReferenceThreshold = param.getNextOptionIndex();
        ReferenceMethod = param.getNextOptionIndex();
        ReferenceMaximumOfMerge = param.getNextOptionIndex();
        ReferenceMinmumOfCluster = param.getNextOptionIndex();
        ReferenceMinimumOfClusterLength = param.getNextOptionIndex();
        addOptions(list);
    }

    public boolean readParam(int option, String optarg) {
        if (option == ReferenceThreshold) {
            setThreshold(Double.parseDouble(optarg));
            return true;
        } else if (option == ReferenceMethod) {
            setMethod(optarg);
            return true;
        } else if (option == ReferenceMaximumOfMerge) {
            setMaximumOfMerge(Integer.parseInt(optarg));
            return true;
        } else if (option == ReferenceMinmumOfCluster) {
            setMinimumOfCluster(Integer.parseInt(optarg));
            return true;
        } else if (option == ReferenceMinimumOfClusterLength) {
            setMinimumOfClusterLength(Integer.parseInt(optarg));
            return true;
        }
        return false;
    }

    public void addOptions(ArrayList<LongOpt> list) {
        list.add(new LongOpt("cThr", 1, null, ReferenceThreshold));
        list.add(new LongOpt("cMethod", 1, null, ReferenceMethod));
        list.add(new LongOpt("cMaximumMerge", 1, null, ReferenceMaximumOfMerge));
        list.add(new LongOpt("cMinimumOfCluster", 1, null, ReferenceMinmumOfCluster));
        list.add(new LongOpt("cMinimumOfClusterLength", 1, null, ReferenceMinimumOfClusterLength));
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double clustThr) {
        this.threshold = clustThr;
    }

    public ClusteringMethod getMethod() {
        return method;
    }

    public void setMethod(String ch) {
        if (ch.equals(ClustMethodString[ClusteringMethod.CLUST_L_BIC.ordinal()])) {
            method = ClusteringMethod.CLUST_L_BIC;
        } else if (ch.equals(ClustMethodString[ClusteringMethod.CLUST_H_BIC.ordinal()])) {
            method = ClusteringMethod.CLUST_H_BIC;
        } else if (ch.equals(ClustMethodString[ClusteringMethod.CLUST_H_CLR.ordinal()])) {
            method = ClusteringMethod.CLUST_H_CLR;
        } else if (ch.equals(ClustMethodString[ClusteringMethod.CLUST_H_CE.ordinal()])) {
            method = ClusteringMethod.CLUST_H_CE;
        } else if (ch.equals(ClustMethodString[ClusteringMethod.CLUST_H_KL2.ordinal()])) {
            method = ClusteringMethod.CLUST_H_KL2;
        } else if (ch.equals(ClustMethodString[ClusteringMethod.CLUST_H_H2.ordinal()])) {
            method = ClusteringMethod.CLUST_H_H2;
        } else if (ch.equals(ClustMethodString[ClusteringMethod.CLUST_H_GD.ordinal()])) {
            method = ClusteringMethod.CLUST_H_GD;
        } else if (ch.equals(ClustMethodString[ClusteringMethod.CLUST_H_GDGMM.ordinal()])) {
            method = ClusteringMethod.CLUST_H_GDGMM;
        } else if (ch.equals(ClustMethodString[ClusteringMethod.CLUST_H_ICR.ordinal()])) {
            method = ClusteringMethod.CLUST_H_ICR;
        } else if (ch.equals(ClustMethodString[ClusteringMethod.CLUST_R_BIC.ordinal()])) {
            method = ClusteringMethod.CLUST_R_BIC;
        } else if (ch.equals(ClustMethodString[ClusteringMethod.CLUST_H_TScore.ordinal()])) {
            method = ClusteringMethod.CLUST_H_TScore;
        } else if (ch.equals(ClustMethodString[ClusteringMethod.CLUST_H_GLR.ordinal()])) {
            method = ClusteringMethod.CLUST_H_GLR;
        }
    }

    public int getMaximumOfMerge() {
        return maximumOfMerge;
    }

    public void setMaximumOfMerge(int clustNbMaxMerge) {
        this.maximumOfMerge = clustNbMaxMerge;
    }

    public int getMinimumOfCluster() {
        return minmumOfCluster;
    }

    public void setMinimumOfCluster(int clustNbMinClust) {
        this.minmumOfCluster = clustNbMinClust;
    }

    public int getMinimumOfClusterLength() {
        return minimumOfClusterLength;
    }

    public void setMinimumOfClusterLength(int clustMinLen) {
        this.minimumOfClusterLength = clustMinLen;
    }

    public void printMethod() {
        System.out.print("info[clust] \t --cMethod \t clustering method ");
        System.out.print("[" + ClustMethodString[ClusteringMethod.CLUST_L_BIC.ordinal()] + ", ");
        System.out.print(ClustMethodString[ClusteringMethod.CLUST_R_BIC.ordinal()] + ", ");
        System.out.print(ClustMethodString[ClusteringMethod.CLUST_H_BIC.ordinal()] + ", ");
        System.out.print(ClustMethodString[ClusteringMethod.CLUST_H_CLR.ordinal()] + ", ");
        System.out.print(ClustMethodString[ClusteringMethod.CLUST_H_CE.ordinal()] + ", ");
        System.out.print(ClustMethodString[ClusteringMethod.CLUST_H_KL2.ordinal()] + ", ");
        System.out.print(ClustMethodString[ClusteringMethod.CLUST_H_H2.ordinal()] + ", ");
        System.out.print(ClustMethodString[ClusteringMethod.CLUST_H_GD.ordinal()] + ", ");
        System.out.print(ClustMethodString[ClusteringMethod.CLUST_H_GDGMM.ordinal()] + ", ");
        System.out.print(ClustMethodString[ClusteringMethod.CLUST_H_ICR.ordinal()] + ", ");
        System.out.print(ClustMethodString[ClusteringMethod.CLUST_H_TScore.ordinal()] + ", ");
        System.out.print(ClustMethodString[ClusteringMethod.CLUST_H_GLR.ordinal()] + "] = ");
        System.out.println(ClustMethodString[method.ordinal()] + "(" + method.ordinal() + ")");
    }

    public void printThreshold() {
        System.out.print("info[ParameterClustering] \t --cThr \t clustering threshold = ");
        System.out.println(getThreshold());
    }

    public void printMaximumOfMerge() {
        System.out.print("info[ParameterClustering] \t --cMaximumMerge \t maximum number of merges = ");
        System.out.println(getMaximumOfMerge());
    }

    public void printMinmumOfCluster() {
        System.out.print("info[ParameterClustering] \t --cMinimumOfCluster \t minum number of speakers = ");
        System.out.println(getMinimumOfCluster());
    }

    public void printMinimumOfClusterLength() {
        System.out.print("info[ParameterClustering] \t --cMinimumLength \t minum length of cluster = ");
        System.out.println(getMinimumOfClusterLength());
    }

    // Type of clustering method.
    public enum ClusteringMethod {
        CLUST_L_BIC, // BIC linear left to right method, mono gaussian
        CLUST_H_BIC, // BIC hierarchical method, mono gaussian
        CLUST_H_CLR, // CLR method, multi gaussian
        CLUST_H_CE, // CE / NCLR method, see Solomonoff, multi gaussian
        CLUST_H_ICR, // ICR method, see Kyu J. Han
        CLUST_H_KL2, // Kulback Liebler
        CLUST_H_H2, // Holister
        CLUST_H_GD, // Divergence gaussian, LIMSI
        CLUST_H_GDGMM, // Mathieux Ben (IRISA), Divergence gaussian of LIMSI applied to a gmm (to check)
        CLUST_R_BIC, // BIC linear right to left method
        CLUST_H_TScore,
        // IEEE 2009, Cluster criterion Fonctions in spectral subspace and their application in speaker clustering, T.H. Nguyen, H. Li, E.S. Chng
        CLUST_H_GLR // GLR hierarchical method, mono gaussian
    }

}
