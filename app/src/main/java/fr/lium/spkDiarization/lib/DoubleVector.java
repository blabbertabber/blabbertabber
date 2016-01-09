/**
 * <p>
 * DoubleVector
 * </p>
 *
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
 * @author <a href="mailto:gael.salaun@univ-lemans.fr">Gael Salaun</a>
 * @author <a href="mailto:teva.merlin@lium.univ-lemans.fr">Teva Merlin</a>
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
 * Vector matrix class. Used to store mean model.
 */

package fr.lium.spkDiarization.lib;


// TODO: Auto-generated Javadoc

/**
 * The Class DoubleVector.
 */
public class DoubleVector extends SquareMatrix implements Cloneable {

    /**
     * Instantiates a new double vector.
     */
    public DoubleVector() {
        super();
        data = new double[dimension2];
    }

    /**
     * Instantiates a new double vector.
     *
     * @param _dimension the dimension of the vector
     */
    public DoubleVector(int _dimension) {
        dimension = _dimension;
        dimension2 = _dimension;
        data = new double[super.dimension2];
        for (int i = 0; i < dimension2; i++) {
            data[i] = 0;
        }
    }

    /**
     * Check positive value.
     *
     * @param i the element #i
     *
     * @return true, if successful
     */
    public boolean checkPositifValue(int i) {
        double value = get(i);
        if (value <= 0.0) {
            set(i, Double.MIN_VALUE);
            return false;
        }
        return true;
    }

    /**
     * Gets the element #i.
     *
     * @param i the element #i
     *
     * @return the double
     */
    public double get(int i) {
        return get(i, 0);
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.SquareMatrix#get(int, int)
     */
    @Override
    public double get(int i, int j) {
        if (data == null) {
            return 0;
        } else {
            return data[i];
        }
    }

    /**
     * Increment element #i of #step.
     *
     * @param i the element #i
     * @param step the step
     */
    public void increment(int i, double step) {
        data[i] = data[i] + step;
    }

    /**
     * Invert.
     *
     * @param m the m
     *
     * @return true, if successful
     */
    public boolean invert(DoubleVector m) {
        m.logDeterminant = 0.0;
        logDeterminant = 0.0;
        for (int i = 0; i < dimension; i++) {
            m.data[i] = 1.0 / data[i];
            double lv = Math.log(data[i]);
            logDeterminant += lv;
            m.logDeterminant -= lv;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.SquareMatrix#resize(int)
     */
    @Override
    public void resize(int _dim) {
        double[] old = new double[dimension2];
        for (int i = 0; i < dimension2; i++) {
            old[i] = data[i];
        }
        int old_dim = dimension;
        dimension = _dim;
        dimension2 = dimension;
        logDeterminant = -1.0;
        data = new double[dimension2];
        for (int i = 0; i < dimension2; i++) {
            data[i] = 0;
        }
        int min = Math.min(old_dim, dimension);
        for (int i = 0; i < min; i++) {
            data[i] = old[i];
        }
    }

    /**
     * Sets #value in the element #i.
     *
     * @param i the i
     * @param value the value
     */
    public void set(int i, double value) {
        data[i] = value;
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.SquareMatrix#set(int, int, double)
     */
    @Override
    public void set(int i, int j, double k) {
        set(i, k);
    }

}
