/**
 * 
 * <p>
 * ParameterInitializationEM
 * </p>
 * 
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
 * @version v2.0
 * 
 *          Copyright (c) 2007-2009 UniversitE du Maine. All Rights Reserved. Use is subject to license terms.
 * 
 *          THIS SOFTWARE IS PROVIDED BY THE "UNIVERSITE DU MAINE" AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *          DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *          USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *          ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 *          not more use
 */

package fr.lium.spkDiarization.parameter;

import java.util.logging.Logger;

/**
 * The Class ParameterInitializationEM.
 */
public class ParameterInitializationEM extends ParameterEM implements Cloneable {

	/** The Constant TrainInitMethodString. */
	public final static String[] TrainInitMethodString = { "split_all", "split", "uniform", "copy" };

	// Type of initialization method of GMM.
	/**
	 * The Enum ModelInitializeMethod.
	 */
	public static enum ModelInitializeMethod {

		/** The traininit split all. */
		TRAININIT_SPLIT_ALL,
		/** The traininit split. */
		TRAININIT_SPLIT,
		/** The traininit uniform. */
		TRAININIT_UNIFORM,
		/** The traininit copy. */
		TRAININIT_COPY
	};

	/** The model init method. */
	private ModelInitializeMethod modelInitMethod; // Initialization method of GMM.

	/**
	 * The Class ActionModelInitializeMethod.
	 */
	private class ActionModelInitializeMethod extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setModelInitMethod(optarg);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#log(java.util.logging.Logger, fr.lium.spkDiarization.parameter.LongOptWithAction)
		 */
		@Override
		public void log(Logger logger, LongOptWithAction longOpt) {
			String message = "--" + longOpt.getName() + "\tem initialization method ";
			message += formatStrigArray(TrainInitMethodString) + " = ";
			message += TrainInitMethodString[getModelInitMethod().ordinal()] + "(" + getModelInitMethod().ordinal();
			message += ")" + " [" + logger.getName() + "]";
			logger.config(message);
		}
	}

	/**
	 * Instantiates a new parameter initialization em.
	 * 
	 * @param parameter the parameter
	 */
	public ParameterInitializationEM(Parameter parameter) {
		super(parameter);
		setMinimumIteration(1);
		setMaximumIteration(5);
		setMinimumGain(0.01);
		setEMControl("1,5,0.01");
		modelInitMethod = ModelInitializeMethod.TRAININIT_UNIFORM;
		addOption(new LongOptWithAction("emInitMethod", new ActionModelInitializeMethod(), ""));
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.parameter.ParameterEM#clone()
	 */
	@Override
	protected ParameterInitializationEM clone() throws CloneNotSupportedException {
		return (ParameterInitializationEM) super.clone();
	}

	/**
	 * Sets the model init method.
	 * 
	 * @param ch the new model init method
	 */
	public void setModelInitMethod(String ch) {
		for (ModelInitializeMethod num : ModelInitializeMethod.values()) {
			if (ch.equals(TrainInitMethodString[num.ordinal()])) {
				modelInitMethod = num;
			}
		}
	}

	/**
	 * Gets the model init method.
	 * 
	 * @return the model init method
	 */
	public ModelInitializeMethod getModelInitMethod() {
		return modelInitMethod;
	}

}
