/**
 * <p>
 * Pair
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
 * A path that determine an entity in a LinkSet.
 * This class is employed in conjunction of Entity, EntitySet, Link and LinkSet.
 */
package fr.lium.spkDiarization.lib;

/**
 * The Class Pair.
 */
public class Pair<A, B> {

    public final A fst;
    public final B snd;

    public Pair(A fst, B snd) {
        this.fst = fst;
        this.snd = snd;
    }

    private static boolean equals(Object x, Object y) {
        return (x == null && y == null) || (x != null && x.equals(y));
    }

    public static <A, B> Pair<A, B> of(A a, B b) {
        return new Pair<A, B>(a, b);
    }

    public String toString() {
        return "Pair[" + fst + "," + snd + "]";
    }

    // changed casting from (Pair) to (Pair<?,?>) to eliminate warning about generic types in Pair
    public boolean equals(Object other) {
        return (other instanceof Pair<?, ?>) && equals(fst, ((Pair<?, ?>) other).fst)
                && equals(snd, ((Pair<?, ?>) other).snd);
    }

    public int hashCode() {
        if (fst == null)
            return (snd == null) ? 0 : snd.hashCode() + 1;
        else if (snd == null)
            return fst.hashCode() + 2;
        else
            return fst.hashCode() * 17 + snd.hashCode();
    }

}
