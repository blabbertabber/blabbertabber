/**
 * 
 * <p>
 * ParameterInputFeature
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
 * The Class ParameterInputFeature2.
 */
public class ParameterInputFeature2 extends ParameterAudioFeature implements Cloneable {

	/**
	 * Instantiates a new parameter input feature2.
	 * 
	 * @param parameter the parameter
	 */
	public ParameterInputFeature2(Parameter parameter) {
		super(parameter);
		setType("Input2");
		addOption(new LongOptWithAction("f" + getType() + "Mask", new ActionFeatureMask(), ""));
		addOption(new LongOptWithAction("f" + getType() + "Desc", new ActionFeaturesDescString(), ""));
		addOption(new LongOptWithAction("f" + getType() + "MemoryOccupationRate", new ActionMemoryOccupationRate(), ""));
		addOption(new LongOptWithAction("f" + getType() + "SpeechThr", new ActionSpeechMethod(), ""));
		addOption(new LongOptWithAction("f" + getType() + "SpeechMethod", new ActionSpeechMethod(), ""));
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.parameter.ParameterAudioFeature#clone()
	 */
	@Override
	protected ParameterInputFeature2 clone() throws CloneNotSupportedException {
		return (ParameterInputFeature2) super.clone();
	}

}
