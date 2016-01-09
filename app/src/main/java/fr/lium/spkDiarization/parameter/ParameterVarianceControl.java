/**
 * <p>
 * ParameterVarianceControl
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

public class ParameterVarianceControl implements ParameterInterface {
    public static int ReferenceVarianceContol = -1;

    private String varianceControl; // input covariance control parameters
    // (celling and flooring)

    private double flooring; // covariance flooring
    private double ceilling; // covariance celling

    public ParameterVarianceControl(ArrayList<LongOpt> list, Parameter param) {
        ceilling = 10.0;
        flooring = 0.0;
        setVarianceControl("0.0,10.0");
        ReferenceVarianceContol = param.getNextOptionIndex();
        addOptions(list);
    }

    public boolean readParam(int option, String optarg) {
        if (option == ReferenceVarianceContol) {
            setVarianceControl(optarg);
            return true;
        }
        return false;
    }

    public void addOptions(ArrayList<LongOpt> list) {
        list.add(new LongOpt("varCtrl", 1, null, ReferenceVarianceContol));
    }

    public double getFlooring() {
        return flooring;
    }

    public void setFlooring(double flooring) {
        this.flooring = flooring;
    }

    public double getCeilling() {
        return ceilling;
    }

    public void setCeilling(double ceilling) {
        this.ceilling = ceilling;
    }

    public String getVarianceControl() {
        return varianceControl;
    }

    public void setVarianceControl(String varControl) {
        this.varianceControl = varControl;
        double floor;
        double ceil;
        floor = ceil = 0.0;

        StringTokenizer stok375 = new StringTokenizer(varControl, ",");
        int cpt375 = 0;
        int nb = 0;
        while (stok375.hasMoreTokens()) {
            if (cpt375 == 0) {
                floor = Double.parseDouble(stok375.nextToken());
                nb++;
            } else if (cpt375 == 1) {
                ceil = Double.parseDouble(stok375.nextToken());
                nb++;
            }
            cpt375++;
        }
        if (nb > 0) {
            setFlooring(floor);
        }
        if (nb > 1) {
            setCeilling(ceil);
        }
    }

    public void printVarianceControl() {
        System.out.println("info[ParameterVarianceControl] \t --varCtrl \t covariance control (floor[,ceil]) = " + getVarianceControl());
        System.out.println("info[ParameterVarianceControl] \t \t flooring = " + getFlooring());
        System.out.println("info[ParameterVarianceControl] \t \t ceilling = " + getCeilling());
    }
}
