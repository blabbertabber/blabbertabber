/**
 * <p>
 * ParameterEM
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

public class ParameterEM implements ParameterInterface {

    public int ReferenceEMControl = -1;

    private int minimumIteration; // Minimum of iteration of EM algorithm.
    private int maximumIteration; // Maximum of iteration of EM algorithm.
    private double minimumGain; // Minimum gain between two iterations of EM
    // algorithm.

    private String emControl; // String containing the EM parameters.

    protected ParameterEM() {
    }

    public ParameterEM(ArrayList<LongOpt> list, Parameter param) {
        super();
        // minimumIteration = 3;
        // maximumIteration = 10;
        // setMinimumGain(0.01);
        setEMControl("3,10,0.01");
        ReferenceEMControl = param.getNextOptionIndex();
        addOptions(list);
    }

    public boolean readParam(int option, String optarg) {
        if (option == ReferenceEMControl) {
            setEMControl(optarg);
            return true;
        }
        return false;
    }

    public void addOptions(ArrayList<LongOpt> list) {
        list.add(new LongOpt("emCtrl", 1, null, ReferenceEMControl));
    }

    public int getMinimumIteration() {
        return minimumIteration;
    }

    public void setMinimumIteration(int minimumIteration) {
        this.minimumIteration = minimumIteration;
    }

    public int getMaximumIteration() {
        return maximumIteration;
    }

    public void setMaximumIteration(int maximumIteration) {
        this.maximumIteration = maximumIteration;
    }

    public double getMinimumGain() {
        return minimumGain;
    }

    public void setMinimumGain(double minimumGain) {
        this.minimumGain = minimumGain;
    }

    public void setMinimumGain(float minimumGain) {
        this.minimumGain = minimumGain;
    }

    public String getEMControl() {
        return emControl;
    }

    public void setEMControl(String emControl) {
        this.emControl = emControl;
        int nb310 = 0;
        int minIt = 0;
        int maxIt = 0;
        Double minGain = 0.0;
        StringTokenizer stok310 = new StringTokenizer(emControl, ",");
        int cpt310 = 0;
        while (stok310.hasMoreTokens()) {
            if (cpt310 == 0) {
                minIt = Integer.parseInt(stok310.nextToken());
                nb310++;
            } else if (cpt310 == 1) {
                maxIt = Integer.parseInt(stok310.nextToken());
                nb310++;
            }
            if (cpt310 == 2) {
                minGain = Double.parseDouble(stok310.nextToken());
                nb310++;
            }
            cpt310++;
        }
        if (nb310 > 0) {
            setMinimumIteration(minIt);
        }
        if (nb310 > 1) {
            setMaximumIteration(maxIt);
        }
        if (nb310 > 2) {
            setMinimumGain(minGain);
        }
    }

    public void print() {
        System.out.print("info[ParameterEM] \t --emCtrl \t EM control (minIt,maxIt,minGain) = ");
        System.out.print(getEMControl() + " (" + getMinimumIteration() + "," + getMaximumIteration() + ",");
        System.out.println(getMinimumGain() + ")");
        System.out.print("info[ParameterEM] \t \t minIt = ");
        System.out.println(getMinimumIteration());
        System.out.print("info[ParameterEM] \t \t maxIt = ");
        System.out.println(getMaximumIteration());
        System.out.print("info[ParameterEM] \t \t minGain = ");
        System.out.println(getMinimumGain());
    }

}
