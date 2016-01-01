/**
 * 
 * <p>
 * ParameterModel
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

import java.util.logging.Logger;

import fr.lium.spkDiarization.libModel.gaussian.Gaussian;

/**
 * The Class ParameterModel.
 */
public class ParameterModel extends ParameterBase implements Cloneable {

	/** The Constant KindTypeString. */
	public final static String[] KindTypeString = { "FULL", "DIAG" };

	/** The kind. */
	private int kind;

	/**
	 * The Class ActionKind.
	 */
	private class ActionKind extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setModelKind(optarg);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#log(java.util.logging.Logger, fr.lium.spkDiarization.parameter.LongOptWithAction)
		 */
		@Override
		public void log(Logger logger, LongOptWithAction longOpt) {
			logger.config("--" + longOpt.getName() + " \t model: kind of Gaussians " + formatStrigArray(KindTypeString)
					+ " = " + KindTypeString[getModelKind()] + "(" + getModelKind() + ")" + " [" + logger.getName()
					+ "]");
		}
	}

	/** The number of components. */
	private Integer numberOfComponents;

	/**
	 * The Class ActionNumberOfComponents.
	 */
	private class ActionNumberOfComponents extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setNumberOfComponents(Integer.parseInt(optarg));
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return numberOfComponents.toString();
		}
	}

	/**
	 * Instantiates a new parameter model.
	 * 
	 * @param parameter the parameter
	 */
	public ParameterModel(Parameter parameter) {
		super(parameter);
		kind = Gaussian.FULL;
		setNumberOfComponents(1);
		addOption(new LongOptWithAction("kind", new ActionKind(), ""));
		addOption(new LongOptWithAction("nbComp", new ActionNumberOfComponents(), "model: number of components"));
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected ParameterModel clone() throws CloneNotSupportedException {
		return (ParameterModel) super.clone();
	}

	/**
	 * Gets the model kind.
	 * 
	 * @return the model kind
	 */
	public int getModelKind() {
		return kind;
	}

	/**
	 * Sets the kind.
	 * 
	 * @param kind the new kind
	 */
	public void setKind(int kind) {
		this.kind = kind;
	}

	/**
	 * Sets the model kind.
	 * 
	 * @param ch the new model kind
	 */
	public void setModelKind(String ch) {
		if (ch.equals(KindTypeString[Gaussian.FULL])) {
			kind = Gaussian.FULL;
		} else if (ch.equals(KindTypeString[Gaussian.DIAG])) {
			kind = Gaussian.DIAG;
		}
	}

	/**
	 * Gets the number of components.
	 * 
	 * @return the number of components
	 */
	public int getNumberOfComponents() {
		return numberOfComponents;
	}

	/**
	 * Gets the model kind as string.
	 * 
	 * @return the model kind as string
	 */
	public String getModelKindAsString() {
		return KindTypeString[getModelKind()];
	}

	/**
	 * Sets the number of components.
	 * 
	 * @param nbComp the new number of components
	 */
	public void setNumberOfComponents(int nbComp) {
		this.numberOfComponents = nbComp;
	}
}