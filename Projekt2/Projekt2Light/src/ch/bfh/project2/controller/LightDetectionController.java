package ch.bfh.project2.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import ch.bfh.project2.utils.Utils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * The LightDetectionController class implements the application logic. It
 * handles the button for starting/stopping the camera, the acquired video
 * stream, the relative controls and the image segmentation process.
 * 
 * @author Aebischer Patrik, Bösiger Elia
 * @date 26.12.2017
 * @version 1.0
 *
 */
public class LightDetectionController {
	// FXML camera button
	@FXML
	private Button cameraButton;
	// the FXML area for showing the current frame
	@FXML
	private ImageView originalFrame;
	// the FXML area for showing the output of the morphological operations
	@FXML
	private ImageView morphImage;
	// FXML slider for setting HSV ranges
	@FXML
	private Slider brightnessFilter;
	@FXML
	private Slider redMin;
	@FXML
	private Slider redMax;
	@FXML
	private Slider greenMin;
	@FXML
	private Slider greenMax;
	@FXML
	private Slider blueMin;
	@FXML
	private Slider blueMax;
	@FXML
	private RadioButton radioBright;
	@FXML
	private RadioButton radioColor;

	// a timer for acquiring the video stream
	private ScheduledExecutorService timer;
	// the OpenCV object that performs the video capture
	private VideoCapture capture = new VideoCapture();
	// a flag to change the button behavior
	private boolean cameraActive;

	/**
	 * The action triggered by pushing the "start camera" button on the GUI
	 */
	@FXML
	private void startCamera() {

		// set a fixed width for all the image to show and preserve image ratio
		this.imageViewProperties(this.originalFrame, 400);
		this.imageViewProperties(this.morphImage, 400);

		if (!this.cameraActive) {
			// start the video capture
			this.capture.open(0);

			// is the video stream available?
			if (this.capture.isOpened()) {
				this.cameraActive = true;

				// grab a frame every 33 ms (30 frames/sec)
				Runnable frameGrabber = new Runnable() {

					@Override
					public void run() {
						// effectively grab and process a single frame
						Mat frame = getFrame();
						// convert and show the frame
						Image imageToShow = Utils.mat2Image(frame);
						updateImageView(originalFrame, imageToShow);
					}
				};

				this.timer = Executors.newSingleThreadScheduledExecutor();
				this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

				// update the button content
				this.cameraButton.setText("Stop Kamera");
			} else {
				// log the error
				System.err.println("Failed to open the camera connection.");
			}
		} else {
			// the camera is not active at this point
			this.cameraActive = false;
			// update again the button content
			this.cameraButton.setText("Start Kamera");

			// stop the timer
			this.stopAcquisition();
		}
	}

	/**
	 * Get a frame from the opened video stream
	 * 
	 * @return the frame to show
	 */
	private Mat getFrame() {
		Mat frame = new Mat();

		// check if the capture is open
		if (this.capture.isOpened()) {
			try {
				// read the current frame
				this.capture.read(frame);

				// if the frame is not empty, process it
				if (!frame.empty()) {
					// init
					Mat blurredImage = new Mat();
					Mat grayImage = new Mat();
					Mat mask = new Mat();
					Mat morphOutput = new Mat();

					// remove some noise
					Imgproc.blur(frame, blurredImage, new Size(11, 11));

					if (this.radioBright.isSelected()) {

						Imgproc.cvtColor(frame, grayImage, Imgproc.COLOR_BGR2GRAY);

						Imgproc.threshold(grayImage, mask, this.brightnessFilter.getValue(), 255,
								Imgproc.THRESH_BINARY);
					} else {

						Core.inRange(blurredImage,
								new Scalar(this.blueMin.getValue(), this.greenMin.getValue(), this.redMin.getValue()),
								new Scalar(this.blueMax.getValue(), this.greenMax.getValue(), this.redMax.getValue()),
								mask);
					}

					Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(24, 24));
					Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(12, 12));

					Imgproc.erode(mask, morphOutput, erodeElement);
					Imgproc.erode(morphOutput, morphOutput, erodeElement);

					Imgproc.dilate(morphOutput, morphOutput, dilateElement);

					morphOutput = mask;

					this.updateImageView(this.morphImage, Utils.mat2Image(morphOutput));

					// find the light contours and show them
					frame = this.findAndDrawLights(morphOutput, frame);

				}

			} catch (Exception e) {
				// log the (full) error
				System.err.print("Exception during the image elaboration.");
				e.printStackTrace();
			}
		}

		return frame;
	}

	/**
	 * Given a binary image containing one or more closed surfaces. THe method
	 * finds the closed surfaces and highlights the objects contours
	 * 
	 * @param maskedImage
	 *            the binary image to be used as a mask
	 * @param frame
	 *            the original image to be used for drawing the objects contours
	 * @return the image with the objects contours framed
	 */
	private Mat findAndDrawLights(Mat maskedImage, Mat frame) {

		List<MatOfPoint> contours = new ArrayList<>();
		Mat hierarchy = new Mat();

		Imgproc.findContours(maskedImage, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

		if (hierarchy.size().height > 0 && hierarchy.size().width > 0) {
			// show each contour
			for (int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0]) {
				Imgproc.drawContours(frame, contours, idx, new Scalar(250, 0, 0), 10);
			}
		}

		return frame;
	}

	/**
	 * Set a fixed width to the image
	 * 
	 * @param image
	 *            the image to use
	 * @param dimension
	 *            the width of the image to set
	 */
	private void imageViewProperties(ImageView image, int dimension) {
		// set a fixed width for the given ImageView
		image.setFitWidth(dimension);
		// preserve the image ratio
		image.setPreserveRatio(true);
	}

	/**
	 * Stop the acquisition from the camera and release all the resources
	 */
	private void stopAcquisition() {
		if (this.timer != null && !this.timer.isShutdown()) {
			try {
				// stop the timer
				this.timer.shutdown();
				this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				// log any exception
				System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
			}
		}

		if (this.capture.isOpened()) {
			// release the camera
			this.capture.release();
		}
	}

	/**
	 * Update an ImageView with the current Image in the JavaFX main thread
	 * 
	 * @param view
	 *            the ImageView to update
	 * @param image
	 *            the Image to show
	 */
	private void updateImageView(ImageView view, Image image) {
		Utils.onFXThread(view.imageProperty(), image);
	}

	/**
	 * On application close, stop the acquisition from the camera
	 */
	public void setClosed() {
		this.stopAcquisition();
	}
}