package com.robmcelhinney.PhoneBlock;

import android.content.Context;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

/**
 *    Based on implementation from:
 *    Title: CNN for Human Activity Recognition
 *    Author: Aaqib Saeed
 *    Date: <15/09/2017>
 *    Availability: https://github.com/aqibsaeed/Human-Activity-Recognition-using-CNN/tree/master/ActivityRecognition
 **/

class TensorFlowClassifier {
    static {
        System.loadLibrary("tensorflow_inference");
    }

    private TensorFlowInferenceInterface tfInferenceInterface;
    private static final String MODEL_FILE = "file:///android_asset/SittingIntoCar.pb";
    private static final String INPUT_NAME = "input";
    private static final String[] OUTPUT_NAMES = {"y_output"};
    private static final String OUTPUT_NAME = "y_output";
    private static final long[] INPUT_SIZE = {1, 200, 3};
    private static final int OUTPUT_SIZE = 6;

    TensorFlowClassifier(final Context context) {
        tfInferenceInterface = new TensorFlowInferenceInterface(context.getAssets(), MODEL_FILE);
    }

    float[] predictProbabilities(float[] data) {
        float[] result = new float[OUTPUT_SIZE];
        tfInferenceInterface.feed(INPUT_NAME, data, INPUT_SIZE);
        tfInferenceInterface.run(OUTPUT_NAMES);
        tfInferenceInterface.fetch(OUTPUT_NAME, result);
        return result;
    }
}
