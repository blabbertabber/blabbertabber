/**
 * <p>
 * ParameterMAP
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
import java.util.StringTokenizer;

import gnu.getopt.LongOpt;

public class ParameterMAP implements ParameterInterface {
    public static int ReferenceMAPControl = -1;
    public static String[] MAPMethodString = {"std", "linear", "vpmap"};

    ;
    private MAPMethod method; // type of MAP method.
    private double prior; // Prior parameter of MAP.
    private boolean weightAdaptation; // MAP: adaptation of the weights.
    private boolean meanAdaptatation; // MAP: adaptation of the means.
    private boolean covarianceAdaptation; // MAP: adaptation of the covariances.
    private String mapControl; // input MAP control parameters
    public ParameterMAP(ArrayList<LongOpt> list, Parameter param) {
        setMAPControl(MAPMethodString[MAPMethod.MAP_STD.ordinal()] + ",15,0:1:0");
        ReferenceMAPControl = param.getNextOptionIndex();
        addOptions(list);
    }

    public boolean readParam(int option, String optarg) {
        if (option == ReferenceMAPControl) {
            setMAPControl(optarg);
            return true;
        }
        return false;
    }

    public void addOptions(ArrayList<LongOpt> list) {
        list.add(new LongOpt("mapCtrl", 1, null, ReferenceMAPControl));
    }

    public MAPMethod getMethod() {
        return method;
    }

    protected void setMethod(MAPMethod method) {
// System.err.println("info[ParameterMAP] \t method:" + method);
        this.method = method;
    }

    public double getPrior() {
        return prior;
    }

    protected void setPrior(double prior) {
        this.prior = prior;
    }

    public boolean isWeightAdaptation() {
        return weightAdaptation;
    }

    protected void setWeightAdaptation(boolean weightAdaptation) {
        this.weightAdaptation = weightAdaptation;
    }

    public boolean isMeanAdaptatation() {
        return meanAdaptatation;
    }

    protected void setMeanAdaptatation(boolean meanAdaptatation) {
        this.meanAdaptatation = meanAdaptatation;
    }

    public boolean isCovarianceAdaptation() {
        return covarianceAdaptation;
    }

    protected void setCovarianceAdaptation(boolean covarianceAdaptation) {
        this.covarianceAdaptation = covarianceAdaptation;
    }

    public String getMAPControl() {
        return mapControl;
    }

    public void setMAPControl(String mapControl) {
        this.mapControl = mapControl;
        String ch = "";
        int mW;
        int mM;
        int mC;
        mW = mM = mC = 0;
        double p;
        p = 15.0;
        StringTokenizer stok350 = new StringTokenizer(mapControl, ",");
        int cpt350 = 0;
        int nb = 0;
        while (stok350.hasMoreTokens()) {
            String token = stok350.nextToken();
            if (cpt350 == 0) {
                ch = token;
                nb++;
            } else if (cpt350 == 1) {
                p = Double.parseDouble(token);
                nb++;
            } else if (cpt350 == 2) {
                StringTokenizer stok2 = new StringTokenizer(token, ":");
                int cpt2 = 0;
                while (stok2.hasMoreTokens()) {
                    if (cpt2 == 0) {
                        mW = Integer.parseInt(stok2.nextToken());
                        nb++;
                    } else if (cpt2 == 1) {
                        mM = Integer.parseInt(stok2.nextToken());
                        nb++;
                    } else if (cpt2 == 2) {
                        mC = Integer.parseInt(stok2.nextToken());
                        nb++;
                    }
                    cpt2++;
                }
            }
            cpt350++;
        }
        if (nb > 0) {
            if (ch.equals(MAPMethodString[ParameterMAP.MAPMethod.MAP_STD.ordinal()])) {
                setMethod(ParameterMAP.MAPMethod.MAP_STD);
            } else if (ch.equals(MAPMethodString[ParameterMAP.MAPMethod.MAP_LIN.ordinal()])) {
                setMethod(ParameterMAP.MAPMethod.MAP_LIN);
            } else if (ch.equals(MAPMethodString[ParameterMAP.MAPMethod.VPMAP.ordinal()])) {
                setMethod(ParameterMAP.MAPMethod.VPMAP);
            } else {
                System.err.println("info[ParameterMAP] \t unknown method=" + ch);
            }
            if (nb > 1) {
                setPrior(p);
                if (nb > 2) {
                    setWeightAdaptation(false);
                    setCovarianceAdaptation(false);
                    setMeanAdaptatation(false);
                    if (mW != 0) {
                        setWeightAdaptation(true);
                    }
                    if (mM != 0) {
                        setMeanAdaptatation(true);
                    }
                    if (mC != 0) {
                        setCovarianceAdaptation(true);
                    }
                }
            }
        }
    }

    public void print() {
        System.out.print("info[ParameterMAP] \t --mapCtrl \t MAP control (method,prior,w:m:c) = ");
        System.out.println(getMAPControl());
        System.out.println("info[ParameterMAP] \t \t Method = " + getMethod().ordinal());
        System.out.println("info[ParameterMAP] \t \t prior = " + getPrior());
        System.out.println("info[ParameterMAP] \t \t weight adaptation [0,1] = " + isWeightAdaptation());
        System.out.println("info[ParameterMAP] \t \t mean adaptation [0,1] = " + isMeanAdaptatation());
        System.out.println("info[ParameterMAP] \t \t covariance adaptation [0,1] = " + isCovarianceAdaptation());
    }

    // Type of MAP training method of GMM.
    public enum MAPMethod {
        MAP_STD, MAP_LIN, VPMAP
    }
}
