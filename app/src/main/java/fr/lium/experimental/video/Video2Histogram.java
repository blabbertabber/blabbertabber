package fr.lium.experimental.video;

import static com.googlecode.javacv.cpp.opencv_core.cvAvg;
import static com.googlecode.javacv.cpp.opencv_core.cvClearMemStorage;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSize;
import static com.googlecode.javacv.cpp.opencv_core.cvRect;
import static com.googlecode.javacv.cpp.opencv_core.cvResetImageROI;
import static com.googlecode.javacv.cpp.opencv_core.cvSetImageROI;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.CvSize;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_objdetect;

import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.lib.SpkDiarizationLogger;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.libFeature.AudioFeatureDescription;
import fr.lium.spkDiarization.libFeature.AudioFeatureSet;
import fr.lium.spkDiarization.parameter.Parameter;

/**
 * The Class Video2Histogram.
 */
public class Video2Histogram {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(Video2Histogram.class.getName());

	/** The Constant MAX_PIXEL_VALUE. */
	private static final int MAX_PIXEL_VALUE = 255;

	/**
	 * The main method.
	 * 
	 * @param args the arguments
	 * @throws Exception the exception
	 */
	public static void main(String[] args) throws Exception {
		SpkDiarizationLogger.setup();
		Parameter parameter = MainTools.getParameters(args);
		info(parameter, "videoPlanSegmentation");
		if (parameter.show.isEmpty() == false) {
			Loader.load(opencv_objdetect.class);

			VideoFeatureSet videoFeatureSet = new VideoFeatureSet(parameter);
			videoFeatureSet.open();

			AudioFeatureDescription featureDescription = new AudioFeatureDescription();
			featureDescription.setStaticCoeffPresence(true);
			featureDescription.setStaticCoeffNeeded(true);
			featureDescription.setEnergyPresence(false);
			featureDescription.setEnergyNeeded(false);
			featureDescription.setFeatureSize(12);
			featureDescription.setFeaturesFormat(AudioFeatureSet.GZTXT);

			AudioFeatureSet featureSet = new AudioFeatureSet(videoFeatureSet.getLenght(), featureDescription);
			featureSet.CreateCurrentShow(parameter.show);
			IplImage image = null;
			while ((image = videoFeatureSet.grab()) != null) {
				CvMemStorage storage = CvMemStorage.create();
				float[] featureVector = new float[12];
				CvSize imageSize = cvGetSize(image);

				/*
				 * split the image into 4 subimages and compute the average color for every subimages)
				 */
				CvScalar[] m = new CvScalar[4];
				m[0] = cvAverageColorOfRectangleROI(image, 0, 0, imageSize.width() / 2, imageSize.height() / 2);
				m[1] = cvAverageColorOfRectangleROI(image, 0, (imageSize.height() / 2) - 1, imageSize.width() / 2, imageSize.height() / 2);
				m[2] = cvAverageColorOfRectangleROI(image, (imageSize.width() / 2) - 1, 0, imageSize.width() / 2, imageSize.height() / 2);
				m[3] = cvAverageColorOfRectangleROI(image, (imageSize.width() / 2) - 1, (imageSize.height() / 2) - 1, imageSize.width() / 2, imageSize.height() / 2);

				int i = 0;
				for (int k = 0; k < 4; k++) {
					for (int j = 0; j < 3; j++) {
						featureVector[i] = (float) (m[k].getVal(j) / MAX_PIXEL_VALUE);
						i++;
					}
				}
				featureSet.addFeature(featureVector);
				cvClearMemStorage(storage);
			}
			videoFeatureSet.close();
			MainTools.writeFeatureSet(parameter, featureSet);

			ClusterSet clusterSet = new ClusterSet();
			Cluster cluster = clusterSet.createANewCluster("video");
			Segment segment = new Segment(parameter.show, 0, featureSet.getCurrentFeatureListSize(), cluster, parameter.getParameterSegmentationInputFile().getRate());
			cluster.addSegment(segment);
			MainTools.writeClusterSet(parameter, clusterSet);
		}
	}

	/**
	 * Cv average color of rectangle roi.
	 * 
	 * @param iplimg the iplimg
	 * @param x the x
	 * @param y the y
	 * @param width the width
	 * @param height the height
	 * @return the cv scalar
	 */
	public static CvScalar cvAverageColorOfRectangleROI(IplImage iplimg, int x, int y, int width, int height) {
		cvSetImageROI(iplimg, cvRect(x, y, width, height));
		CvScalar mean = cvAvg(iplimg, null);
		cvResetImageROI(iplimg);
		return mean;
	}

	/**
	 * Info.
	 * 
	 * @param parameter the parameter
	 * @param program the program
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws InvocationTargetException the invocation target exception
	 */
	public static void info(Parameter parameter, String program) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (parameter.help) {
			logger.config(parameter.getSeparator2());
			logger.config("info[program] \t name = " + program);
			parameter.getSeparator();
			parameter.logShow();
			parameter.getSeparator();
			parameter.getParameterOutputFeature().logAll();
			parameter.getParameterSegmentationOutputFile().logAll();
			logger.config(parameter.getSeparator());
			parameter.getParameterVideoInputFeature().logAll();
			logger.config(parameter.getSeparator());
		}
	}

}
