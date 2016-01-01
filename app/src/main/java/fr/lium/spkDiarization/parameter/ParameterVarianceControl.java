/**
 * 
 * <p>
 * ParameterVarianceControl
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

import java.util.StringTokenizer;
import java.util.logging.Logger;

/**
 * The Class ParameterVarianceControl.
 */
public class ParameterVarianceControl extends ParameterBase implements Cloneable {

	/** The variance control. */
	private String varianceControl; // input covariance control parameters

	/**
	 * The Class ActionVarianceControl.
	 */
	private class ActionVarianceControl extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String arg) {
			setVarianceControl(arg);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#log(java.util.logging.Logger, fr.lium.spkDiarization.parameter.LongOptWithAction)
		 */
		@Override
		public void log(Logger logger, LongOptWithAction longOpt) {
			logger.config("--" + longOpt.getName() + " \t covariance control (floor[,ceil]) = " + getVarianceControl()
					+ " [" + logger.getName() + "]");
			logger.config("\t flooring = " + getFlooring());
			logger.config("\t ceilling = " + getCeilling());
		}
	}

	/** The flooring. */
	private double flooring; // covariance flooring

	/** The ceilling. */
	private double ceilling; // covariance celling

	/**
	 * Instantiates a new parameter variance control.
	 * 
	 * @param parameter the parameter
	 */
	public ParameterVarianceControl(Parameter parameter) {
		super(parameter);
		setFlooring(0.0);
		setCeilling(10.0);
		addOption(new LongOptWithAction("varCtrl", new ActionVarianceControl(), ""));
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected ParameterVarianceControl clone() throws CloneNotSupportedException {
		return (ParameterVarianceControl) super.clone();
	}

	/**
	 * Gets the flooring.
	 * 
	 * @return the flooring
	 */
	public double getFlooring() {
		return flooring;
	}

	/**
	 * Sets the flooring.
	 * 
	 * @param flooring the new flooring
	 */
	public void setFlooring(double flooring) {
		this.flooring = flooring;
	}

	/**
	 * Gets the ceilling.
	 * 
	 * @return the ceilling
	 */
	public double getCeilling() {
		return ceilling;
	}

	/**
	 * Sets the ceilling.
	 * 
	 * @param ceilling the new ceilling
	 */
	public void setCeilling(double ceilling) {
		this.ceilling = ceilling;
	}

	/**
	 * Sets the variance control.
	 * 
	 * @param varControl the new variance control
	 */
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

	/**
	 * Gets the variance control.
	 * 
	 * @return the variance control
	 */
	public String getVarianceControl() {
		return varianceControl;
	}

}
