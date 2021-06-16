package sample;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import sample.utils.Utils;



public class Controller {

    private ScheduledExecutorService timer;
    private boolean                  cameraActive = false;
    private VideoCapture             videoCapture = new VideoCapture();
    private static final int         CAMERA_ID     = 0;
    private Mat logo;

    @FXML
    private Button button;

    @FXML
    private ImageView currentFrame;

    @FXML
    private CheckBox grayscale;

    @FXML
    private CheckBox logoCheckBox;

    @FXML
    private ImageView histogram;


    @FXML
    protected void startCamera() {

        this.currentFrame.setFitWidth(600);
        this.currentFrame.setPreserveRatio(true);

        if(!this.cameraActive){


            this.videoCapture.open(CAMERA_ID);

            if(this.videoCapture.isOpened()){

                this.cameraActive = true;

                Runnable frameGrabber = new Runnable() {
                    @Override
                    public void run() {

                        Mat frame = grabFrame();
                        Image imageToShow = Utils.mat2Image(frame);
                        updateImageView(currentFrame, imageToShow);
                    }
                };

                this.timer = Executors.newSingleThreadScheduledExecutor();
                this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
                this.button.setText("STOP CAMERA");

            } else {

                System.err.println("Impossible to open the camera connection...");
            }


        } else {

            this.cameraActive = false;
            this.button.setText("START CAMERA");
            this.stopAcquisition();

        }


    }


    @FXML
    protected void loadLogo(){

        if(logoCheckBox.isSelected()){
            this.logo = Imgcodecs.imread("resources/Poli.png");
        }
    }
    private Mat grabFrame(){

        Mat frame = new Mat();

        if(this.videoCapture.isOpened()){

            try {

                this.videoCapture.read(frame);

                if(!frame.empty()){

                    if(logoCheckBox.isSelected() && this.logo != null){

                        Rect roi = new Rect(frame.cols() - logo.cols(),
                                            frame.rows() - logo.rows(),
                                               logo.cols(),
                                               logo.rows());
                        Mat imageROI = frame.submat(roi);
                        Core.addWeighted(imageROI, 1.0, logo, 0.8, 0.0, imageROI);
                        //logo.copyTo(imageROI, logo);

                    }

                    if(grayscale.isSelected()){

                        Imgproc.cvtColor(frame,frame,Imgproc.COLOR_BGR2GRAY);
                    }

                    this.showHistogram(frame, grayscale.isSelected());

                }
            } catch (Exception ex){
                System.err.println("exception during image elaboration:" + ex);
            }
        }
        return frame;
    }

    private void showHistogram(Mat frame, boolean gray){

        List<Mat> images = new ArrayList<>();
        Core.split(frame, images);
        MatOfInt histSize = new MatOfInt(256);
        MatOfInt channels = new MatOfInt(0);
        MatOfFloat histRange = new MatOfFloat(0, 256);

        Mat hist_blue = new Mat();
        Mat hist_green = new Mat();
        Mat hist_red = new Mat();

        Imgproc.calcHist(images.subList(0,1), channels, new Mat(), hist_blue, histSize, histRange, false);

        if(!gray){

            Imgproc.calcHist(images.subList(1,2), channels, new Mat(), hist_green, histSize, histRange, false);
            Imgproc.calcHist(images.subList(2,3), channels, new Mat(), hist_red, histSize, histRange, false);

        }

        int hist_width = 150;
        int hist_height = 150;
        int bin_w = (int) Math.round(hist_width / histSize.get(0,0)[0]);

        Mat histImage = new Mat(hist_height, hist_width, CvType.CV_8UC3, new Scalar(0,0,0));
        Core.normalize(hist_blue, hist_blue, 0, histImage.rows(), Core.NORM_MINMAX, -1, new Mat());

        if(!gray){

            Core.normalize(hist_green, hist_green, 0, histImage.rows(), Core.NORM_MINMAX, -1, new Mat());
            Core.normalize(hist_red, hist_red, 0, histImage.rows(), Core.NORM_MINMAX, -1, new Mat());
        }

        for(int i = 1; i < histSize.get(0,0)[0]; i++){
            Imgproc.line(histImage,
                    new Point(bin_w * (i-1), hist_height - Math.round(hist_blue.get(i - 1, 0)[0])),
                    new Point(bin_w * (i), hist_height - Math.round(hist_blue.get(i,0)[0])),
                    new Scalar(255,0,0), 2,8,0);

            if(!gray){

                Imgproc.line(histImage,
                        new Point(bin_w * (i -1 ), hist_height - Math.round(hist_green.get(i - 1, 0)[0])),
                        new Point(bin_w * (i), hist_height - Math.round(hist_green.get(i,0)[0])),
                        new Scalar(0,255,0), 2, 8,0);

                Imgproc.line(histImage,
                        new Point(bin_w * (i - 1), hist_height -Math.round(hist_red.get(i - 1, 0)[0])),
                        new Point(bin_w * (i), hist_height - Math.round(hist_red.get(i,0)[0])),
                        new Scalar(0,0,255), 2, 8, 0);
            }
        }

        Image histImg = Utils.mat2Image(histImage);
        updateImageView(histogram, histImg);
    }

    private void updateImageView(ImageView view, Image image){

        Utils.onFXThread(view.imageProperty(),image);
    }

    private void stopAcquisition(){

        if(this.timer!=null && !this.timer.isShutdown()){

            try{

                this.timer.shutdown();
                this.timer.awaitTermination(33,TimeUnit.MILLISECONDS);

            } catch (InterruptedException ex){

                System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + ex);
            }
        }

        if(this.videoCapture.isOpened()){

            this.videoCapture.release();
        }
    }

    protected void setClosed(){

        this.stopAcquisition();
    }

    public void initialize(){

        this.videoCapture = new VideoCapture();
        this.cameraActive = false;
    }
}
