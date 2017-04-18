package com.example.majid.ar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class MainActivity extends AppCompatActivity {

    static {
        OpenCVLoader.initDebug();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Mat m = new Mat(100, 100, CvType.CV_8UC4);
    }
}
