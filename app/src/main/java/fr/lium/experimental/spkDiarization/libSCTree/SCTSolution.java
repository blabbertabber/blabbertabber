/**
 * <p>
 * SCTSolution
 * </p>
 *
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
 * @author <a href="mailto:vincent.jousse@lium.univ-lemans.fr">Vincent Jousse</a>
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
 */

package fr.lium.experimental.spkDiarization.libSCTree;

public class SCTSolution implements Cloneable {
    boolean[] used;
    int current;
    SCTProbabilities probabilities;
    boolean[] gapBefore;
    boolean[] gapAfter;
    int size;

    public SCTSolution(int size) {
        this.size = size;
        used = new boolean[size];
        gapBefore = new boolean[size];
        gapAfter = new boolean[size];
        for (int i = 0; i < size; i++) {
            used[i] = false;
            gapBefore[i] = false;
            gapAfter[i] = false;
        }
        current = -1;
        probabilities = null;
    }

    /**
     * @return the size
     */
    public int getSize() {
        return size;
    }

    public boolean isUsed(int key) {
        return (used[key]);
    }

    public SCTProbabilities getProbabilities() {
        return probabilities;
    }

    public void setProbabilities(SCTProbabilities probabilities) {
        this.probabilities = probabilities;
    }

    public boolean isClosed() {
        return (probabilities != null);
    }

    protected int getCurrent() {
        return current;
    }

    protected void setCurrent(int index, boolean usage, boolean gapBefore, boolean gapAfter) {
        current = index;
        used[current] = usage;
        this.gapAfter[current] = gapAfter;
        this.gapBefore[current] = gapBefore;
    }

    @Override
    protected Object clone() {
        SCTSolution result = null;
        try {
            result = (SCTSolution) (super.clone());
        } catch (CloneNotSupportedException e) {
        }
        result.used = new boolean[size];
        result.gapAfter = new boolean[size];
        result.gapBefore = new boolean[size];
        for (int i = 0; i < used.length; i++) {
            result.used[i] = used[i];
            result.gapAfter[i] = gapAfter[i];
            result.gapBefore[i] = gapBefore[i];
        }

        return result;
    }

    public void debug() {
        System.err.println("[debug] SCTSolution current:" + current);
        System.err.print("[debug] SCTSolution solution: ");
        for (int i = 0; i < used.length; i++) {
            System.err.print(" " + i + "=" + used[i]);
        }
        System.err.println();
        System.err.println(probabilities.toString());
        System.err.println("[debug] SCTSolution -------------------");

    }

    /**
     * @return the beforeGap
     */
    public boolean isBeforeGap(int i) {
        return gapBefore[i];
    }

    /**
     * @param beforeGap the beforeGap to set
     */
    public void setBeforeGap(int i, boolean beforeGap) {
        this.gapBefore[i] = beforeGap;
    }

    /**
     * @return the afterGap
     */
    public boolean isAfterGap(int i) {
        return gapAfter[i];
    }

    /**
     * @param afterGap the afterGap to set
     */
    public void setAfterGap(int i, boolean afterGap) {
        this.gapAfter[i] = afterGap;
    }

}
