/**
 * 
 * <p>
 * ParameterAdjustSegmentation
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
 *          ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 *          not more use
 */

package fr.lium.spkDiarization.parameter;

/**
 * The Class ParameterAdjustSegmentation.
 */
public class ParameterAdjustSegmentation extends ParameterBase implements Cloneable {

	/** The seach decay. */
	private Integer seachDecay;

	/**
	 * The Class ActionSeachDecay.
	 */
	private class ActionSeachDecay extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String arg) {
			setSeachDecay(Integer.parseInt(arg));
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return seachDecay.toString();
		}
	}

	/** The half window size for energie. */
	private Integer halfWindowSizeForEnergie;

	/**
	 * The Class ActionHalfWindowSizeForEnergie.
	 */
	private class ActionHalfWindowSizeForEnergie extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String arg) {
			setHalfWindowSizeForEnergie(Integer.parseInt(arg));
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return halfWindowSizeForEnergie.toString();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected ParameterAdjustSegmentation clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return (ParameterAdjustSegmentation) super.clone();
	}

	/**
	 * Instantiates a new parameter adjust segmentation.
	 * 
	 * @param parameter the parameter
	 */
	public ParameterAdjustSegmentation(Parameter parameter) {
		super(parameter);
		setSeachDecay(25);
		addOption(new LongOptWithAction("sSeachDecay", new ActionSeachDecay(), ""));

		setHalfWindowSizeForEnergie(5);
		addOption(new LongOptWithAction("sHalfWindowSizeForEnergie", new ActionHalfWindowSizeForEnergie(), ""));
	}

	/**
	 * Gets the seach decay.
	 * 
	 * @return the seach decay
	 */
	public int getSeachDecay() {
		return seachDecay;
	}

	/**
	 * Sets the seach decay.
	 * 
	 * @param adjSeachDecay the new seach decay
	 */
	public void setSeachDecay(int adjSeachDecay) {
		this.seachDecay = adjSeachDecay;
	}

	/**
	 * Gets the half window size for energie.
	 * 
	 * @return the half window size for energie
	 */
	public int getHalfWindowSizeForEnergie() {
		return halfWindowSizeForEnergie;
	}

	/**
	 * Sets the half window size for energie.
	 * 
	 * @param adjHSizeWin the new half window size for energie
	 */
	public void setHalfWindowSizeForEnergie(int adjHSizeWin) {
		this.halfWindowSizeForEnergie = adjHSizeWin;
	}

}