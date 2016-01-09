/**
 * <p>
 * ParameterFeature
 * </p>
 *
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
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
 * not more use
 */

package fr.lium.spkDiarization.parameter;

import java.util.StringTokenizer;

import fr.lium.spkDiarization.libFeature.FeatureDescription;
import fr.lium.spkDiarization.libFeature.FeatureSet;

public abstract class ParameterFeature implements ParameterInterface {

    public static String[] FeaturesTypeString = {"spro4", "htk", "sphinx", "gztxt", "audio2sphinx", "featureSetTransformation", "audio8kHz2sphinx"};

    private String featureMask;
    private FeatureDescription featureDescription;
    private String featuresDescString; // Feature description in a string
    private double memoryOccupationRate;
    private String type;

    public ParameterFeature() {
        featureDescription = new FeatureDescription();
        featuresDescString = FeaturesTypeString[FeatureSet.SPHINX] + ",1:1:0:0:0:0,13,0:0:0:0";
        setFeaturesDescription(featuresDescString);
        setMemoryOccupationRate(0.8);
    }

    /**
     * @return the memoryOccupationRate
     */
    public double getMemoryOccupationRate() {
        return memoryOccupationRate;
    }

    /**
     * @param memoryOccupationRate the memoryOccupationRate to set
     */
    public void setMemoryOccupationRate(double memoryOccupationRate) {
        this.memoryOccupationRate = memoryOccupationRate;
    }

    protected String getType() {
        return type;
    }

    protected void setType(String type) {
        this.type = type;
    }

    public String getFeatureMask() {
        return featureMask;
    }

    public void setFeatureMask(String featuresInputMask) {
        this.featureMask = featuresInputMask;
    }

    public FeatureDescription getFeaturesDescription() {
        return featureDescription;
    }

    public void setFeaturesDescription(String optarg) {
        featuresDescString = optarg;
        char[] inputType = new char[255];
        int i;
        int s;
        int e;
        int ds;
        int dds;
        int de;
        int dde;
        int fDim;
        int nb;
        int fc;
        int fr;
        int fwsize;
        int normMethod = 0;
        i = s = e = ds = dds = de = dde = fDim = nb = fc = fr = fwsize = 0;

        StringTokenizer strTok = new StringTokenizer(optarg, ",");
        int cpt = 0;
        nb = 0;
        while (strTok.hasMoreTokens()) {
            String token = strTok.nextToken();
            if (cpt == 0) {
                inputType = token.toCharArray();
                nb++;
            } else if (cpt == 1) {
                StringTokenizer stok2 = new StringTokenizer(token, ":");
                int cpt2 = 0;
                while (stok2.hasMoreTokens()) {
                    if (cpt2 == 0) {
                        s = Integer.parseInt(stok2.nextToken());
                        nb++;
                    } else if (cpt2 == 1) {
                        e = Integer.parseInt(stok2.nextToken());
                        nb++;
                    } else if (cpt2 == 2) {
                        ds = Integer.parseInt(stok2.nextToken());
                        nb++;
                    } else if (cpt2 == 3) {
                        de = Integer.parseInt(stok2.nextToken());
                        nb++;
                    } else if (cpt2 == 4) {
                        dds = Integer.parseInt(stok2.nextToken());
                        nb++;
                    } else if (cpt2 == 5) {
                        dde = Integer.parseInt(stok2.nextToken());
                        nb++;
                    }
                    cpt2++;
                }
            } else if (cpt == 2) {
                fDim = Integer.parseInt(token);
                nb++;
            } else if (cpt == 3) {
                StringTokenizer strTok2 = new StringTokenizer(token, ":");
                int cpt2 = 0;
                while (strTok2.hasMoreTokens()) {
                    if (cpt2 == 0) {
                        fc = Integer.parseInt(strTok2.nextToken());
                        nb++;
                    } else if (cpt2 == 1) {
                        fr = Integer.parseInt(strTok2.nextToken());
                        nb++;
                    } else if (cpt2 == 2) {
                        fwsize = Integer.parseInt(strTok2.nextToken());
                        nb++;
                    } else if (cpt2 == 3) {
                        normMethod = Integer.parseInt(strTok2.nextToken());
                        nb++;
                    }
                    cpt2++;
                }
            }
            cpt++;
        }
        if (String.valueOf(inputType).equals(FeaturesTypeString[FeatureSet.SPRO4])) {
            featureDescription.setFeaturesFormat(FeatureSet.SPRO4);
        } else if (String.valueOf(inputType).equals(FeaturesTypeString[FeatureSet.HTK])) {
            featureDescription.setFeaturesFormat(FeatureSet.HTK);
        } else if (String.valueOf(inputType).equals(FeaturesTypeString[FeatureSet.SPHINX])) {
            featureDescription.setFeaturesFormat(FeatureSet.SPHINX);
        } else if (String.valueOf(inputType).equals(FeaturesTypeString[FeatureSet.GZTXT])) {
            featureDescription.setFeaturesFormat(FeatureSet.GZTXT);
        } else if (String.valueOf(inputType).equals(FeaturesTypeString[FeatureSet.AUDIO16Khz2SPHINXMFCC])) {
            featureDescription.setFeaturesFormat(FeatureSet.AUDIO16Khz2SPHINXMFCC);
        } else if (String.valueOf(inputType).equals(FeaturesTypeString[FeatureSet.AUDIO8kHz2SPHINXMFCC])) {
            featureDescription.setFeaturesFormat(FeatureSet.AUDIO8kHz2SPHINXMFCC);
        } else if (String.valueOf(inputType).equals(FeaturesTypeString[FeatureSet.FEATURESETTRANSFORMATION])) {
            featureDescription.setFeaturesFormat(FeatureSet.FEATURESETTRANSFORMATION);
        }

        i = 1;
        if (nb > i++) {
            featureDescription.setStaticCoeffPresence((s == 1) || (s == 3));
            featureDescription.setStaticCoeffNeeded((s == 1) || (s == 2));
        }
        if (nb > i++) {
            featureDescription.setEnergyPresence((e == 1) || (e == 3));
            featureDescription.setEnergyNeeded((e == 1) || (e == 2));
        }
        if (nb > i++) {
            featureDescription.setDeltaCoeffPresence((ds == 1) || (ds == 3));
            featureDescription.setDeltaCoeffNeeded((ds == 1) || (ds == 2));
        }
        if (nb > i++) {
            featureDescription.setDeltaEnergyPresence((de == 1) || (de == 3));
            featureDescription.setDeltaEnergyNeeded((de == 1) || (de == 2));
        }
        if (nb > i++) {
            featureDescription.setDoubleDeltaCoeffPresence((dds == 1) || (dds == 3));
            featureDescription.setDoubleDeltaCoeffNeeded((dds == 1) || (dds == 2));
        }
        if (nb > i++) {
            featureDescription.setDoubleDeltaEnergyPresence((dde == 1) || (dde == 3));
            featureDescription.setDoubleDeltaEnergyNeeded((dde == 1) || (dde == 2));
        }
        if (nb > i++) {
            featureDescription.setVectorSize(fDim);
        }
        if (nb > i++) {
            featureDescription.setCentered(fc == 1);
        }
        if (nb > i++) {
            featureDescription.setReduced(fr == 1);
        }
        if (nb > i++) {
            featureDescription.setNormalizationWindowSize(fwsize);
        }
        if (nb > i++) {
            featureDescription.setNormalizationMethod(normMethod);
        }
    }

    private int paramValueFromComponentPresenceAndNeed(boolean isComponentPresent, boolean isComponentNeeded) {
        if ((!isComponentPresent) && (!isComponentNeeded)) {
            return 0;
        }
        if ((isComponentPresent) && (isComponentNeeded)) {
            return 1;
        }
        if ((!isComponentPresent) && (isComponentNeeded)) {
            return 2;
        }
        if ((isComponentPresent) && (!isComponentNeeded)) {
            return 3;
        }
        return -1; // Never reached, this return is there just to prevent the
        // compiler from telling us "missing return statement"
    }

    public void printDescription() {
        System.out.print("info[ParameterFeature-" + type + "] \t --f" + type + "Desc \t Features info (type[,s:e:ds:de:dds:dde,dim,c:r:wSize:method]) = ");
        System.out.print(FeaturesTypeString[featureDescription.getFeaturesFormat()] + ",");
        System.out.print(paramValueFromComponentPresenceAndNeed(featureDescription.getStaticCoeffPresence(), featureDescription.getStaticCoeffNeeded()) + ":");
        System.out.print(paramValueFromComponentPresenceAndNeed(featureDescription.getEnergyPresence(), featureDescription.getEnergyNeeded()) + ":");
        System.out.print(paramValueFromComponentPresenceAndNeed(featureDescription.getDeltaCoeffPresence(), featureDescription.getDeltaCoeffNeeded()) + ":");
        System.out.print(paramValueFromComponentPresenceAndNeed(featureDescription.getDeltaEnergyPresence(), featureDescription.getDeltaEnergyNeeded()) + ":");
        System.out.print(paramValueFromComponentPresenceAndNeed(featureDescription.getDoubleDeltaCoeffPresence(), featureDescription
                .getDoubleDeltaCoeffNeeded())
                + ":");
        System.out.print(paramValueFromComponentPresenceAndNeed(featureDescription.getDoubleDeltaEnergyPresence(), featureDescription
                .getDoubleDeltaEnergyNeeded())
                + ",");
        System.out.print(featureDescription.getVectorSize() + ",");
        System.out.print((featureDescription.getCentered() ? 1 : 0) + ":");
        System.out.print((featureDescription.getReduced() ? 1 : 0) + ":");
        System.out.print(featureDescription.getNormalizationWindowSize() + ":");
        System.out.println(featureDescription.getNormalizationMethod());
        System.out.print("info[ParameterFeature-" + type + "] \t \t type [");
        System.out.print(FeaturesTypeString[FeatureSet.SPHINX] + "," + FeaturesTypeString[FeatureSet.SPRO4] + ",");
        System.out.print(FeaturesTypeString[FeatureSet.HTK] + ",");
        // FeaturesTypeString[Features.GZTXT]+ "] = ");
        System.out.print(FeaturesTypeString[FeatureSet.GZTXT] + "," + FeaturesTypeString[FeatureSet.AUDIO16Khz2SPHINXMFCC] + "] = ");
        System.out.print(FeaturesTypeString[featureDescription.getFeaturesFormat()]);
        System.out.println(" (" + featureDescription.getFeaturesFormat() + ")");
        System.out.println("info[ParameterFeature-" + type + "] \t \t static [0=not present,1=present ,3=to be removed] = "
                + paramValueFromComponentPresenceAndNeed(featureDescription.getStaticCoeffPresence(), featureDescription.getStaticCoeffNeeded()));
        System.out.println("info[ParameterFeature-" + type + "] \t \t energy [0,1,3] = "
                + paramValueFromComponentPresenceAndNeed(featureDescription.getEnergyPresence(), featureDescription.getEnergyNeeded()));
        System.out.println("info[ParameterFeature-" + type + "] \t \t delta [0,1,2=computed on the fly,3] = "
                + paramValueFromComponentPresenceAndNeed(featureDescription.getDeltaCoeffPresence(), featureDescription.getDeltaCoeffNeeded()));
        System.out.println("info[ParameterFeature-" + type + "] \t \t delta energy [0,1,2=computed on the fly,3] = "
                + paramValueFromComponentPresenceAndNeed(featureDescription.getDeltaEnergyPresence(), featureDescription.getDeltaEnergyNeeded()));
        System.out.println("info[ParameterFeature-" + type + "] \t \t delta delta [0,1,2,3] = "
                + paramValueFromComponentPresenceAndNeed(featureDescription.getDoubleDeltaCoeffPresence(), featureDescription.getDoubleDeltaCoeffNeeded()));
        System.out.println("info[ParameterFeature-" + type + "] \t \t delta delta energy [0,1,2,3] = "
                + paramValueFromComponentPresenceAndNeed(featureDescription.getDoubleDeltaEnergyPresence(), featureDescription.getDoubleDeltaEnergyNeeded()));
        System.out.println("info[ParameterFeature-" + type + "] \t \t file dim = " + featureDescription.getVectorSize());
        System.out.println("info[ParameterFeature-" + type + "] \t \t normalization, center [0,1] = " + (featureDescription.getCentered() ? 1 : 0));
        System.out.println("info[ParameterFeature-" + type + "] \t \t normalization, reduce [0,1] = " + (featureDescription.getReduced() ? 1 : 0));
        System.out.println("info[ParameterFeature-" + type + "] \t \t normalization, window size = " + featureDescription.getNormalizationWindowSize());
        System.out.print("info[ParameterFeature-" + type + "] \t \t normalization, method [");
        System.out.print(FeatureDescription.NORM_BY_SEGMENT + " (" + FeatureDescription.NORMALIZE_METHOD_STR[FeatureDescription.NORM_BY_SEGMENT] + "), ");
        System.out.print(FeatureDescription.NORM_BY_CLUSTER + " (" + FeatureDescription.NORMALIZE_METHOD_STR[FeatureDescription.NORM_BY_CLUSTER] + "), ");
        System.out.print(FeatureDescription.NORM_BY_SLIDING + " (" + FeatureDescription.NORMALIZE_METHOD_STR[FeatureDescription.NORM_BY_SLIDING] + "), ");
        System.out.print(FeatureDescription.NORM_BY_WARPING + " (" + FeatureDescription.NORMALIZE_METHOD_STR[FeatureDescription.NORM_BY_WARPING] + ")] =");
        System.out.println(featureDescription.getNormalizationMethod());

    }

    public void printMemoryOccupationRate() {
        System.out.print("info[ParameterFeature-" + type + "] \t --f" + type + "MemoryOccupationRate \t memory occupation rate of the feature in the java virtual machine = ");
        System.out.println(getMemoryOccupationRate());
    }

    public void printMask() {
        System.out.print("info[ParameterFeature-" + type + "] \t --f" + type + "Mask \t Features input mask = ");
        System.out.println(getFeatureMask());
    }

    public String getFeaturesDescString() {
        return featuresDescString;
    }

    public void setFeaturesDescString(String featuresDescString) {
        this.featuresDescString = featuresDescString;
    }

    public void print() {
        printMask();
        printDescription();
        printMemoryOccupationRate();
    }
}