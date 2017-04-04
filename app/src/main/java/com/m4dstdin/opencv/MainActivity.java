package com.m4dstdin.opencv;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.m4dstdin.opencv.util.CascadeHelper;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.IOException;

import static org.opencv.android.CameraBridgeViewBase.CAMERA_ID_FRONT;
import static org.opencv.android.OpenCVLoader.OPENCV_VERSION_3_2_0;

public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    static {
        OpenCVLoader.initDebug();
    }

    private JavaCameraView cameraView;
    private CascadeClassifier faceCascade;
    private CascadeClassifier eyeCascade;
    private Size minimumSize;
    private Size maximumSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraView = (JavaCameraView) findViewById(R.id.cameraView);
        cameraView.setCvCameraViewListener(this);
        cameraView.setCameraIndex(CAMERA_ID_FRONT);
    }

    @Override
    protected void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OPENCV_VERSION_3_2_0, this, loaderCallback);
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        // The faces will be a 20% of the height of the screen
        int absoluteFaceSize = (int) (height * 0.2);
        minimumSize = new Size(absoluteFaceSize, absoluteFaceSize);
        maximumSize = new Size();
    }

    @Override
    public void onCameraViewStopped() {
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat rgba = inputFrame.rgba();
        Mat grayScale = inputFrame.gray();
        MatOfRect faces = new MatOfRect();

        faceCascade.detectMultiScale(
                grayScale,
                faces, 1.1, 2, 2,
                minimumSize,
                maximumSize
        );
        
        // If there are any faces found, draw a rectangle around it
        Rect[] facesArray = faces.toArray();
        for (Rect face : facesArray) {
            Imgproc.rectangle(rgba, face.tl(), face.br(), new Scalar(0, 255, 0, 255), 3);
            // Create a sub Mat from the whole image to detect eyes
            Mat faceImage = rgba.submat(face);
            Mat faceImageGrayscale = grayScale.submat(face);
            MatOfRect eyes = new MatOfRect();
            eyeCascade.detectMultiScale(faceImageGrayscale, eyes);
            Rect[] eyesArray = eyes.toArray();
            for (Rect eye : eyesArray) {
                Imgproc.rectangle(faceImage, eye.tl(), eye.br(), new Scalar(0, 255, 0, 255), 2);
            }
        }

        return rgba;
    }

    private final BaseLoaderCallback loaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            super.onManagerConnected(status);
            switch (status) {
                case SUCCESS: {
                    try {
                        faceCascade = CascadeHelper.getCascade(MainActivity.this, "haarcascade_frontal_face.xml");
                        eyeCascade = CascadeHelper.getCascade(MainActivity.this, "haarcascade_eye.xml");
                    } catch (IOException e) {
                        Log.e(getClass().getSimpleName(), "Unable to load haarcascade, killing application", e);
                        System.exit(0);
                    }
                    cameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

}
