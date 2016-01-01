/**
 * 
 * <p>
 * ParameterMAP
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
 * The Class ParameterMAP.
 */
public class ParameterMAP extends ParameterBase implements Cloneable {
	// Type of MAP training method of GMM.
	/**
	 * The Enum MAPMethod.
	 */
	public enum MAPMethod {

		/** The map std. */
		MAP_STD,
		/** The map lin. */
		MAP_LIN,
		/** The vpmap. */
		VPMAP
	};

	/** The Constant MAPMethodString. */
	public final static String[] MAPMethodString = { "std", "linear", "vpmap" };

	/** The method. */
	private MAPMethod method; // type of MAP method.

	/** The prior. */
	private double prior; // Prior parameter of MAP.

	/** The weight adaptation. */
	private boolean weightAdaptation; // MAP: adaptation of the weights.

	/** The mean adaptatation. */
	private boolean meanAdaptatation; // MAP: adaptation of the means.

	/** The covariance adaptation. */
	private boolean covarianceAdaptation; // MAP: adaptation of the covariances.

	/** The map control. */
	private String mapControl; // input MAP control parameters

	/**
	 * The Class ActionMAPControl.
	 */
	private class ActionMAPControl extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setMAPControl(optarg);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#log(java.util.logging.Logger, fr.lium.spkDiarization.parameter.LongOptWithAction)
		 */
		@Override
		public void log(Logger logger, LongOptWithAction longOpt) {
			logger.config("--" + longOpt.getName() + " \t MAP control (method,prior,w:m:c) = " + getMAPControl() + " ["
					+ logger.getName() + "]");
			logger.config("\t Method " + formatStrigArray(MAPMethodString) + " = " + getMethod().ordinal());
			logger.config("\t prior = " + getPrior());
			logger.config("\t weight adaptation [0,1] = " + isWeightAdaptation());
			logger.config("\t mean adaptation [0,1] = " + isMeanAdaptatation());
			logger.config("\t covariance adaptation [0,1] = " + isCovarianceAdaptation());
		}
	}

	/**
	 * Instantiates a new parameter map.
	 * 
	 * @param parameter the parameter
	 */
	public ParameterMAP(Parameter parameter) {
		super(parameter);
		setMAPControl(MAPMethodString[MAPMethod.MAP_STD.ordinal()] + ",15,0:1:0");
		addOption(new LongOptWithAction("mapCtrl", new ActionMAPControl(), ""));
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected ParameterMAP clone() throws CloneNotSupportedException {
		return (ParameterMAP) super.clone();
	}

	/**
	 * Gets the method.
	 * 
	 * @return the method
	 */
	public MAPMethod getMethod() {
		return method;
	}

	/**
	 * Sets the method.
	 * 
	 * @param method the new method
	 */
	protected void setMethod(MAPMethod method) {
		logger.finest("info[ParameterMAP] \t method:" + method);
		this.method = method;
	}

	/**
	 * Gets the prior.
	 * 
	 * @return the prior
	 */
	public double getPrior() {
		return prior;
	}

	/**
	 * Sets the prior.
	 * 
	 * @param prior the new prior
	 */
	protected void setPrior(double prior) {
		this.prior = prior;
	}

	/**
	 * Checks if is weight adaptation.
	 * 
	 * @return true, if is weight adaptation
	 */
	public boolean isWeightAdaptation() {
		return weightAdaptation;
	}

	/**
	 * Sets the weight adaptation.
	 * 
	 * @param weightAdaptation the new weight adaptation
	 */
	protected void setWeightAdaptation(boolean weightAdaptation) {
		this.weightAdaptation = weightAdaptation;
	}

	/**
	 * Checks if is mean adaptatation.
	 * 
	 * @return true, if is mean adaptatation
	 */
	public boolean isMeanAdaptatation() {
		return meanAdaptatation;
	}

	/**
	 * Sets the mean adaptatation.
	 * 
	 * @param meanAdaptatation the new mean adaptatation
	 */
	protected void setMeanAdaptatation(boolean meanAdaptatation) {
		this.meanAdaptatation = meanAdaptatation;
	}

	/**
	 * Checks if is covariance adaptation.
	 * 
	 * @return true, if is covariance adaptation
	 */
	public boolean isCovarianceAdaptation() {
		return covarianceAdaptation;
	}

	/**
	 * Sets the covariance adaptation.
	 * 
	 * @param covarianceAdaptation the new covariance adaptation
	 */
	protected void setCovarianceAdaptation(boolean covarianceAdaptation) {
		this.covarianceAdaptation = covarianceAdaptation;
	}

	/**
	 * Sets the mAP control.
	 * 
	 * @param mapControl the new mAP control
	 */
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
			for (MAPMethod num : MAPMethod.values()) {
				if (ch.equals(MAPMethodString[num.ordinal()])) {
					setMethod(num);
				}
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

	/**
	 * Gets the mAP control.
	 * 
	 * @return the mAP control
	 */
	public String getMAPControl() {
		return mapControl;
	}

}
