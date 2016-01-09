/**
 * <p>
 * ModelIO
 * </p>
 *
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
 * @author <a href="mailto:gael.salaun@univ-lemans.fr">Gael Salaun</a>
 * @author <a href="mailto:teva.merlin@lium.univ-lemans.fr">Teva Merlin</a>
 * @version v2.0
 * <p/>
 * Copyright (c) 2007-2009 Universite du Maine. All Rights Reserved. Use is subject to license terms.
 * <p/>
 * THIS SOFTWARE IS PROVIDED BY THE "UNIVERSITE DU MAINE" AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 * <p/>
 * Reader and writer for models
 */

package fr.lium.spkDiarization.libModel;

import java.io.IOException;
import java.util.ArrayList;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.DoubleVector;
import fr.lium.spkDiarization.lib.IOFile;

/**
 * The Class Model Input Output.
 */
public class ModelIO {

    /** The key gmm container. */
    protected static String keyGMMContainer = "GMMVECT_";

    /** The key gauss container. */
    protected static String keyGaussContainer = "GAUSSVEC";

    /** The key gmm. */
    protected static String keyGMM = "GMM_____";

    /** The key gauss. */
    protected static String keyGauss = "GAUSS___";

    /**
     * Reader of a GMM in AMIRAL/LIA format (old format, non public format).
     *
     * @param file the file
     *
     * @return the GMM
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws DiarizationException the diarization exception
     * @deprecated
     */
    public static GMM readAmiral(IOFile file) throws IOException, DiarizationException {
        String lia = "LIA Modele Multidistrib.";
        String LIAIdent = file.readString(24);
        file.readChar();
        if (LIAIdent != lia) {
            throw new DiarizationException("GMM: readAmiral() error of identification ");
        }

        file.readShort(); // swap

        if (file.readChar() != 2) { // version
            throw new DiarizationException("GMM: readAmiral() error of version ");
        }
        file.readInt(); // length
        int lenInfo = file.readInt();
        if (lenInfo > 0) {
            file.readString(lenInfo - 1); // info
            file.readChar();
        }
        int nbComp = file.readInt();
        short dim = file.readShort();
        int nbfeaturesTrain = file.readInt();
        DoubleVector mw = new DoubleVector(nbComp);
        for (int i = 0; i < nbComp; i++) {
            mw.set(i, file.readDouble());
        }
        int kind = Gaussian.DIAG;
        GMM gmm = new GMM(0, dim, kind);
        for (int c = 0; c < nbComp; c++) {
            char typeDist = file.readChar();
            if (typeDist != 1) {
                throw new DiarizationException("GMM: readAmiral() error of distribution type ");
            }
            file.readDouble(); // cst
            char diag = file.readChar();
            if (diag != 1) {
                kind = Gaussian.FULL;
                gmm.setKind(kind);
            }
            Gaussian g = gmm.addNewComponent();
            g.setWeight(mw.get(c));
            g.initModel();
            g.setAccumulatorCount(nbfeaturesTrain);
            for (int i = 0; i < dim; i++) { // cov
                for (int j = i; j < ((kind == Gaussian.FULL) ? dim : i + 1); j++) {
                    g.setCovariance(i, j, file.readDouble());
                }
            }
            for (int i = 0; i < dim; i++) { // invert cov
                for (int j = i; j < ((kind == Gaussian.FULL) ? dim : i + 1); j++) {
                    file.readDouble();
                }
            }
            for (int j = 0; j < dim; j++) { // mean
                g.setMean(j, file.readDouble());
            }
            g.computeInvertCovariance();
            g.setGLR();
            g.computeLikelihoodConstant();
            file.readDouble(); // det
            file.readDouble(); // coeff appartenance des trames a cette
            // gaussienne
        }
        for (int i = 0; i < nbComp; i++) {
            gmm.getComponent(i).setWeight(mw.get(i));
        }
        return gmm;
    }

    /**
     * Reader of a Gaussian.
     *
     * @param f the file
     *
     * @return the gaussian
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws DiarizationException the diarization exception
     */
    public static Gaussian readerGauss(IOFile f) throws IOException, DiarizationException {
        String k = f.readString(8);
        Gaussian g = null;
        if (k.equals(ModelIO.keyGauss)) {
            f.readInt(); // read id not use know
            int l = f.readInt();
            String name = f.readString(l);
            String gender = f.readString(1);
            int kind = f.readInt();
            int dim = f.readInt();
            int count = f.readInt();
            double weight = f.readDouble();
            if (kind == Gaussian.FULL) {
                g = new FullGaussian(dim);
            } else {
                g = new DiagGaussian(dim);
            }
            g.initModel();
            g.setName(name);
            g.setGender(gender);
            g.setAccumulatorCount(count);
            g.setWeight(weight);
            for (int j = 0; j < dim; j++) {
                double v = f.readDouble();
                g.setMean(j, v);
                for (int t = j; t < ((kind == Gaussian.FULL) ? dim : j + 1); t++) {
                    v = f.readDouble();
                    g.setCovariance(j, t, v);
                }
            }

            g.computeInvertCovariance();
            g.setGLR();
            g.computeLikelihoodConstant();
        } else {
            throw new DiarizationException("ModelIO: readGauss() bad id");
        }
        return g;
    }

    /**
     * Reader of a Gaussian vector.
     *
     * @param f the file
     * @param gv the gaussian vector
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws DiarizationException the diarization exception
     */
    public static void readerGaussContainer(IOFile f, ArrayList<Gaussian> gv) throws IOException, DiarizationException {
        String k = f.readString(8);
        if (k.equals(keyGaussContainer)) {
            int size = f.readInt();
            gv.clear();
            for (int i = 0; i < size; i++) {
                gv.add(ModelIO.readerGauss(f));
            }
        } else {
            throw new DiarizationException("ModelIO: readGaussVect() bad id");
        }
    }

    /**
     * Reader of a GMM.
     *
     * @param f the file
     *
     * @return the GMM
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws DiarizationException the diarization exception
     */
    public static GMM readerGMM(IOFile f) throws IOException, DiarizationException {
        String k = f.readString(8);
        if (k.equals(keyGMM)) {
            f.readInt(); // compatibility with old model
            int l = f.readInt();
            String name = f.readString(l);
            String gender = f.readString(1);
            int kind = f.readInt();
            int dim = f.readInt();
            int nbComp = f.readInt();
            GMM g = new GMM(nbComp, dim, kind);
            // g.setId(id);
            g.setName(name);
            g.setGender(gender);
            ModelIO.readerGaussContainer(f, g.getComponents());
            return g;
        } else {
            System.out.println("ModelIO: readGMM() bad id");
            return new GMM();
        }
    }

    /**
     * Reader of GMM vector.
     *
     * @param f the file
     * @param gv the gaussian vector
     *
     * @throws DiarizationException the diarization exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void readerGMMContainer(IOFile f, ArrayList<GMM> gv) throws IOException, DiarizationException {
        String k = f.readString(8);
        if (k.equals(keyGMMContainer)) {
            int size = f.readInt();
            gv.clear();
            for (int i = 0; i < size; i++) {
                gv.add(ModelIO.readerGMM(f));
            }
        } else {
            throw new DiarizationException("ModelIO: readGMMVect() bad id");
        }
    }

    /**
     * Writer of Gaussian.
     *
     * @param f the file
     * @param g the gaussian
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws DiarizationException the diarization exception
     */
    public static void writerGauss(IOFile f, Gaussian g) throws IOException, DiarizationException {
        f.writeString(ModelIO.keyGauss);

        int dim = g.getDim();
        int kind = g.getKind();
        f.writeInt(g.getName().hashCode()); // Compatibility with old models
        String name = g.getName();
        int l = name.length();

        f.writeInt(l);
        f.writeString(name);
        f.writeString(g.getGender());
        f.writeInt(kind);
        f.writeInt(dim);
        f.writeInt(g.getCount());
        f.writeDouble(g.getWeight());
        for (int j = 0; j < dim; j++) {
            double v = g.getMean(j);
            f.writeDouble(v);
            for (int k = j; k < ((kind == Gaussian.FULL) ? dim : j + 1); k++) {
                v = g.getCovariance(j, k);
                f.writeDouble(v);
            }
        }
    }

    /**
     * Writer of Gaussian vector.
     *
     * @param f the file
     * @param gv the gaussian vector
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws DiarizationException the diarization exception
     */
    public static void writerGaussContainer(IOFile f, ArrayList<Gaussian> gv) throws IOException, DiarizationException {
        f.writeString(ModelIO.keyGaussContainer);
        f.writeInt(gv.size());
        for (Gaussian gaussian : gv) {
            ModelIO.writerGauss(f, gaussian);
        }
    }

    /**
     * Writer of GMM.
     *
     * @param f the file
     * @param g the gaussian
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws DiarizationException the diarization exception
     */
    public static void writerGMM(IOFile f, GMM g) throws IOException, DiarizationException {
        f.writeString(keyGMM);
        f.writeInt(g.getName().hashCode());// Compatibility with old model
        String name = g.getName();
        int l = name.length();
        f.writeInt(l);
        f.writeString(name);
        f.writeString(g.getGender());
        f.writeInt(g.getKind());
        f.writeInt(g.getDim());
        f.writeInt(g.getNbOfComponents());
        ModelIO.writerGaussContainer(f, g.getComponents());
    }

    /**
     * Writer of GMM vector.
     *
     * @param f the file
     * @param gv the gaussian vector
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws DiarizationException the diarization exception
     */
    public static void writerGMMContainer(IOFile f, ArrayList<GMM> gv) throws IOException, DiarizationException {
        f.writeString(ModelIO.keyGMMContainer);
        f.writeInt(gv.size());
        for (GMM gmm : gv) {
            ModelIO.writerGMM(f, gmm);
        }
    }

}
