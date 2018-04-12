package com.robmcelhinney.PhoneBlock;

import android.content.Context;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;


class TensorFlowClassifier {
    static {
        System.loadLibrary("tensorflow_inference");
    }

    private TensorFlowInferenceInterface inferenceInterface;
//    private static final String MODEL_FILE = "file:///android_asset/frozen_har.pb";
//    private static final String MODEL_FILE = "file:///android_asset/frozen_har combineordered.pb";
//    private static final String MODEL_FILE = "file:///android_asset/frozen_har jogging.pb";
//    private static final String MODEL_FILE = "file:///android_asset/frozen_har all7.pb";
    private static final String MODEL_FILE = "file:///android_asset/frozen_har mineNoSitting.pb";

    private static final String INPUT_NODE = "input";
    private static final String[] OUTPUT_NODES = {"y_"};
    private static final String OUTPUT_NODE = "y_";
    private static final long[] INPUT_SIZE = {1, 200, 3};
//    private static final int OUTPUT_SIZE = 7;
    private static final int OUTPUT_SIZE = 6;

    TensorFlowClassifier(final Context context) {
        inferenceInterface = new TensorFlowInferenceInterface(context.getAssets(), MODEL_FILE);
    }

    float[] predictProbabilities(float[] data) {
        float[] result = new float[OUTPUT_SIZE];
        inferenceInterface.feed(INPUT_NODE, data, INPUT_SIZE);
        inferenceInterface.run(OUTPUT_NODES);
        inferenceInterface.fetch(OUTPUT_NODE, result);

        //Downstairs	Jogging  sittingCar    Standing    Upstairs    Walking
        return result;
    }
}
