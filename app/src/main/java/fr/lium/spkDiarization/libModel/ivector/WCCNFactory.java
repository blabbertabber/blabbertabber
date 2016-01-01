package fr.lium.spkDiarization.libModel.ivector;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.libMatrix.MatrixSquare;
import fr.lium.spkDiarization.libModel.gaussian.FullGaussian;

/**
 * A factory for creating WCCN objects.
 */
public class WCCNFactory {

	/*
	 * Brief computed the WCCN matrix (Within Class Covariance Normalization) on a list of iVectors use in WCCN+cosine dustance.
	 */
	/**
	 * Train wccn matrix.
	 * 
	 * @param list the list
	 * @return the matrix square
	 * @throws DiarizationException the diarization exception
	 */
	public static MatrixSquare trainWCCNMatrix(IVectorArrayList list) throws DiarizationException {
		FullGaussian fg = EigenFactorRadialNormalizationFactory.meanAndCovariance(list);
		fg.computeInvertCovariance();
		return fg.getInvertCovariance().choleskyAt();
	}

	/**
	 * Applied wccn.
	 * 
	 * @param list the list
	 * @param At the at
	 */
	public static void appliedWCCN(IVectorArrayList list, MatrixSquare At) {
		for (IVector iv : list) {
			iv.NormalizeWithCholeskyDecompositionMatrix(At);
		}
	}

}
