package com.example.majid.ar;

import android.annotation.SuppressLint;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import filters.Filter;
import filters.NoneFilter;
import filters.ar.ImageDetectionFilter;

public class MainActivity extends AppCompatActivity implements CvCameraViewListener2, OnTouchListener {
    private static final String TAG = "OCVSample::Activity";

    private CameraView mOpenCvCameraView;
    private List<Size> mResolutionList;
    private MenuItem[] mEffectMenuItems;
    private SubMenu mColorEffectsMenu;
    private MenuItem[] mResolutionMenuItems;
    private SubMenu mResolutionMenu;
    private MenuItem nextTrackerMenu;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(MainActivity.this);

                    final Filter starryNight;
                    try {
                        starryNight = new ImageDetectionFilter(
                                MainActivity.this,
                                R.drawable.starry_night);
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to load drawable: " +
                                "starry_night");
                        e.printStackTrace();
                        break;
                    }

                    final Filter akbarHunting;
                    try {
                        akbarHunting = new ImageDetectionFilter(
                                MainActivity.this,
                                R.drawable.akbar_hunting_with_cheetahs);
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to load drawable: " +
                                "akbar_hunting_with_cheetahs");
                        e.printStackTrace();
                        break;
                    }

                    mImageDetectionFilters = new Filter[] {
                            new NoneFilter(),
                            starryNight,
                            akbarHunting
                    };

//                    mCurveFilters = new Filter[] {
//                            new NoneFilter(),
//                            new PortraCurveFilter(),
//                            new ProviaCurveFilter(),
//                            new VelviaCurveFilter(),
//                            new CrossProcessCurveFilter()
//                    };
//
//                    mMixerFilters = new Filter[] {
//                            new NoneFilter(),
//                            new RecolorRCFilter(),
//                            new RecolorRGVFilter(),
//                            new RecolorCMVFilter()
//                    };
//
//                    mConvolutionFilters = new Filter[] {
//                            new NoneFilter(),
//                            new StrokeEdgesFilter(),
//                    };

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    // Keys for storing the indices of the active filters.
    private static final String STATE_IMAGE_DETECTION_FILTER_INDEX = "imageDetectionFilterIndex";
    private static final String STATE_CURVE_FILTER_INDEX = "curveFilterIndex";
    private static final String STATE_MIXER_FILTER_INDEX = "mixerFilterIndex";
    private static final String STATE_CONVOLUTION_FILTER_INDEX = "convolutionFilterIndex";

    // The filters.
    private Filter[] mImageDetectionFilters;
//    private Filter[] mCurveFilters;
//    private Filter[] mMixerFilters;
//    private Filter[] mConvolutionFilters;

    // The indices of the active filters.
    private int mImageDetectionFilterIndex;
//    private int mCurveFilterIndex;
//    private int mMixerFilterIndex;
//    private int mConvolutionFilterIndex;

//    private Mat mRgba;
//    private Mat mGray;
//    private Mat mView;
//    private Mat mObject;

    TextView log;

    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if(savedInstanceState != null){
            mImageDetectionFilterIndex = savedInstanceState.getInt(
                    STATE_IMAGE_DETECTION_FILTER_INDEX, 0);
        }else{
            mImageDetectionFilterIndex = 0;
        }


        setContentView(R.layout.tutorial3_surface_view);

        log = (TextView) findViewById(R.id.log);

        mOpenCvCameraView = (CameraView) findViewById(R.id.tutorial3_activity_java_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    public void setMessage(String s){
        log.setText("\n" + s + "" + log.getText().toString());
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {

        // Save the current filter indices.
        savedInstanceState.putInt(STATE_IMAGE_DETECTION_FILTER_INDEX,
                mImageDetectionFilterIndex);

        super.onSaveInstanceState(savedInstanceState);
    }

    public void onCameraViewStarted(int width, int height) {

//        mRgba = new Mat();
//        mGray = new Mat();
//        mView = new Mat();
//        mObject = new Mat();

    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        final Mat rgba = inputFrame.rgba();

        // Apply the active filters.
        if (mImageDetectionFilters != null) {
            mImageDetectionFilters[mImageDetectionFilterIndex].apply(
                    rgba, rgba);
        }

        return rgba;

        //return inputFrame.rgba();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        nextTrackerMenu = menu.add(0, 10001 , Menu.NONE  ,"Next Tracker");

        List<String> effects = mOpenCvCameraView.getEffectList();

        //solarize

        if (effects == null) {
            Log.e(TAG, "Color effects are not supported by device!");
            return true;
        }


        mColorEffectsMenu = menu.addSubMenu("Color Effect");
        mEffectMenuItems = new MenuItem[effects.size()];

        int idx = 0;
        ListIterator<String> effectItr = effects.listIterator();
        while(effectItr.hasNext()) {
            String element = effectItr.next();
            mEffectMenuItems[idx] = mColorEffectsMenu.add(1, idx, Menu.NONE, element);
            idx++;
        }

        mResolutionMenu = menu.addSubMenu("Resolution");
        mResolutionList = mOpenCvCameraView.getResolutionList();
        mResolutionMenuItems = new MenuItem[mResolutionList.size()];

        ListIterator<Size> resolutionItr = mResolutionList.listIterator();
        idx = 0;
        while(resolutionItr.hasNext()) {
            Size element = resolutionItr.next();
            mResolutionMenuItems[idx] = mResolutionMenu.add(2, idx, Menu.NONE,
                    Integer.valueOf(element.width).toString() + "x" + Integer.valueOf(element.height).toString());
            idx++;
        }

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);

        if(item.getItemId() == 10001){
            mImageDetectionFilterIndex++;
            if (mImageDetectionFilterIndex == mImageDetectionFilters.length) {
                mImageDetectionFilterIndex = 0;
            }
            return true;
        }

        if (item.getGroupId() == 1)
        {
            mOpenCvCameraView.setEffect((String) item.getTitle());
            Toast.makeText(this, mOpenCvCameraView.getEffect(), Toast.LENGTH_SHORT).show();
        }
        else if (item.getGroupId() == 2)
        {
            int id = item.getItemId();
            Size resolution = mResolutionList.get(id);
            mOpenCvCameraView.setResolution(resolution);
            resolution = mOpenCvCameraView.getResolution();
            String caption = Integer.valueOf(resolution.width).toString() + "x" + Integer.valueOf(resolution.height).toString();
            Toast.makeText(this, caption, Toast.LENGTH_SHORT).show();
        }

        return true;
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.i(TAG,"onTouch event");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentDateandTime = sdf.format(new Date());
        String fileName = Environment.getExternalStorageDirectory().getPath() +
                "/sample_picture_" + currentDateandTime + ".jpg";
        mOpenCvCameraView.takePicture(fileName);
        Toast.makeText(this, fileName + " saved", Toast.LENGTH_SHORT).show();
        return false;
    }
}