/**
 * <p>
 * ParameterTopGaussian
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

public class ParameterTopGaussian implements ParameterInterface {
    public static int ReferenceTopGaussian = -1;

    private String scoreNTopGMMMask;
    private int scoreNTop;

    public ParameterTopGaussian(ArrayList<LongOpt> list, Parameter param) {
        setScoreNTop(-1);
        setScoreNTopGMMMask("");
        ReferenceTopGaussian = param.getNextOptionIndex();
        addOptions(list);
    }

    public boolean readParam(int option, String optarg) {
        if (option == ReferenceTopGaussian) {
            setTopGaussian(optarg);
            return true;
        }
        return false;
    }

    public void addOptions(ArrayList<LongOpt> list) {
        list.add(new LongOpt("sTop", 1, null, ReferenceTopGaussian));
    }

    public String getScoreNTopGMMMask() {
        return scoreNTopGMMMask;
    }

    protected void setScoreNTopGMMMask(String scoreNTopGMMMask) {
        this.scoreNTopGMMMask = scoreNTopGMMMask;
    }

    public int getScoreNTop() {
        return scoreNTop;
    }

    public void setScoreNTop(int scoreNTop) {
        this.scoreNTop = scoreNTop;
    }

    public void setTopGaussian(String optarg) {
        StringTokenizer stok600 = new StringTokenizer(optarg, ",");
        setScoreNTop(Integer.parseInt(stok600.nextToken()));
        char[] ch2 = new char[255];
        ch2 = stok600.nextToken().toCharArray();
        setScoreNTopGMMMask(new String(ch2));

    }

    public void printTopGaussian() {
        System.out.print("info[ParameterTopGaussian] \t --sTop \t use top Gaussians (ntop,modelMask) = ");
        System.out.println(getScoreNTop() + "," + getScoreNTopGMMMask());
        System.out.println("info[ParameterTopGaussian] \t\t nb = " + getScoreNTop());
        System.out.println("info[ParameterTopGaussian] \t\t model = " + getScoreNTopGMMMask());
    }
}