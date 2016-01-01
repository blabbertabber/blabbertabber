/**
 * 
 * <p>
 * ParameterEM
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
 * The Class ParameterEM.
 */
public class ParameterEM extends ParameterBase implements Cloneable {

	/** The minimum iteration. */
	private int minimumIteration; // Minimum of iteration of EM algorithm.

	/** The maximum iteration. */
	private int maximumIteration; // Maximum of iteration of EM algorithm.

	/** The minimum gain. */
	private double minimumGain; // Minimum gain between two iterations of EM
	// algorithm.

	/** The em control. */
	private String emControl; // String containing the EM parameters.

	/**
	 * The Class ActionEMControl.
	 */
	private class ActionEMControl extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setEMControl(optarg);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#log(java.util.logging.Logger, fr.lium.spkDiarization.parameter.LongOptWithAction)
		 */
		@Override
		public void log(Logger logger, LongOptWithAction longOpt) {
			String message = "--" + longOpt.getName() + " \t EM control (minIt,maxIt,minGain) = ";
			message += getEMControl() + " (" + getMinimumIteration() + "," + getMaximumIteration() + ",";
			message += getMinimumGain() + ")" + " [" + logger.getName() + "]";
			logger.config(message);

			message = "\t \t minIt = ";
			message += getMinimumIteration();
			logger.config(message);
			message = "\t \t maxIt = ";
			message += getMaximumIteration();
			logger.config(message);
			message = "\t \t minGain = ";
			message += getMinimumGain();
			logger.config(message);
		}
	}

	/**
	 * Instantiates a new parameter em.
	 * 
	 * @param parameter the parameter
	 */
	protected ParameterEM(Parameter parameter) {
		super(parameter);
		setEMControl("3,10,0.01");
		addOption(new LongOptWithAction("emCtrl", new ActionEMControl(), ""));
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected ParameterEM clone() throws CloneNotSupportedException {
		return (ParameterEM) super.clone();
	}

	/**
	 * Gets the minimum iteration.
	 * 
	 * @return the minimum iteration
	 */
	public int getMinimumIteration() {
		return minimumIteration;
	}

	/**
	 * Sets the minimum iteration.
	 * 
	 * @param minimumIteration the new minimum iteration
	 */
	public void setMinimumIteration(int minimumIteration) {
		this.minimumIteration = minimumIteration;
	}

	/**
	 * Gets the maximum iteration.
	 * 
	 * @return the maximum iteration
	 */
	public int getMaximumIteration() {
		return maximumIteration;
	}

	/**
	 * Sets the maximum iteration.
	 * 
	 * @param maximumIteration the new maximum iteration
	 */
	public void setMaximumIteration(int maximumIteration) {
		this.maximumIteration = maximumIteration;
	}

	/**
	 * Gets the minimum gain.
	 * 
	 * @return the minimum gain
	 */
	public double getMinimumGain() {
		return minimumGain;
	}

	/**
	 * Sets the minimum gain.
	 * 
	 * @param minimumGain the new minimum gain
	 */
	public void setMinimumGain(float minimumGain) {
		this.minimumGain = minimumGain;
	}

	/**
	 * Sets the minimum gain.
	 * 
	 * @param minimumGain the new minimum gain
	 */
	public void setMinimumGain(double minimumGain) {
		this.minimumGain = minimumGain;
	}

	/**
	 * Sets the eM control.
	 * 
	 * @param emControl the new eM control
	 */
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

	/**
	 * Gets the eM control.
	 * 
	 * @return the eM control
	 */
	public String getEMControl() {
		return emControl;
	}

}
