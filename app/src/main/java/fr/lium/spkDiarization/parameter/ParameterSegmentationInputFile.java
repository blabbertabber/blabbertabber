/**
 * 
 * <p>
 * ParameterSegmentationInputFile
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
 * The Class ParameterSegmentationInputFile.
 */
public class ParameterSegmentationInputFile extends ParameterSegmentationFile implements Cloneable {

	/**
	 * Instantiates a new parameter segmentation input file.
	 * 
	 * @param parameter the parameter
	 */
	public ParameterSegmentationInputFile(Parameter parameter) {
		super(parameter);
		type = "Input";
		addOption(new LongOptWithAction("s" + type + "Mask", new ActionMask(), ""));
		addOption(new LongOptWithAction("s" + type + "Format", new ActionFormatEncoding(), ""));
		addOption(new LongOptWithAction("s" + type + "Rate", new ActionRate(), ""));
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.parameter.ParameterSegmentationFile#clone()
	 */
	@Override
	protected ParameterSegmentationInputFile clone() throws CloneNotSupportedException {
		return (ParameterSegmentationInputFile) super.clone();
	}
}
