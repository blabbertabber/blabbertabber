/**
 * 
 * <p>
 * Pair
 * </p>
 * 
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
 * @version v2.0
 * 
 *          Copyright (c) 2007-2009 Universite du Maine. All Rights Reserved. Use is subject to license terms.
 * 
 *          THIS SOFTWARE IS PROVIDED BY THE "UNIVERSITE DU MAINE" AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *          DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *          USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *          ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. A path that determine an entity in a LinkSet. This class is employed in conjunction of Entity, EntitySet, Link and LinkSet.
 * 
 */
package fr.lium.spkDiarization.lib;

/**
 * The Class Pair.
 * 
 * @param <A> the generic type
 * @param <B> the generic type
 */
public class Pair<A, B> {

	/** The first. */
	protected A first;

	/** The second. */
	protected B second;

	/**
	 * Instantiates a new pair.
	 * 
	 * @param first the first
	 * @param second the second
	 */
	public Pair(A first, B second) {
		this.first = first;
		this.second = second;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Pair[" + first + "," + second + "]";
	}

	/**
	 * Equals.
	 * 
	 * @param x the x
	 * @param y the y
	 * @return true, if successful
	 */
	private static boolean equals(Object x, Object y) {
		return ((x == null) && (y == null)) || ((x != null) && x.equals(y));
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public boolean equals(Object other) {
		return (other instanceof Pair) && equals(first, ((Pair) other).first) && equals(second, ((Pair) other).second);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (first == null) {
			return (second == null) ? 0 : second.hashCode() + 1;
		} else if (second == null) {
			return first.hashCode() + 2;
		} else {
			return (first.hashCode() * 17) + second.hashCode();
		}
	}

	/**
	 * Of.
	 * 
	 * @param <A> the generic type
	 * @param <B> the generic type
	 * @param a the a
	 * @param b the b
	 * @return the pair
	 */
	public static <A, B> Pair<A, B> of(A a, B b) {
		return new Pair<A, B>(a, b);
	}

	/**
	 * Gets the first.
	 * 
	 * @return the first
	 */
	public A getFirst() {
		return first;
	}

	/**
	 * Gets the second.
	 * 
	 * @return the second
	 */
	public B getSecond() {
		return second;
	}

	/**
	 * Sets the first.
	 * 
	 * @param first the first to set
	 */
	public void setFirst(A first) {
		this.first = first;
	}

	/**
	 * Sets the second.
	 * 
	 * @param second the second to set
	 */
	public void setSecond(B second) {
		this.second = second;
	}

}
