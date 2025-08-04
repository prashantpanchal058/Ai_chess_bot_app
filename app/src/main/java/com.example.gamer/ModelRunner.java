package com.example.gamer;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

public class ModelRunner {
    private Interpreter interpreter;

    public ModelRunner(Context context) {
        try {
            interpreter = new Interpreter(loadModelFile(context, "model5.tflite"));
        } catch (IOException e) {
            Log.e("ModelRunner", "Model failed to load", e);
        }
    }

    private MappedByteBuffer loadModelFile(Context context, String modelName) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(modelName);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY,
                fileDescriptor.getStartOffset(),
                fileDescriptor.getDeclaredLength());
    }

    public float evaluatePosition(float[][][][] input) {
        float[][] output = new float[1][1]; // Assumes model returns a single float score
        interpreter.run(input, output);
        return output[0][0];
    }

    public String predict(List<float[][][][]> inputTensors, List<String> moves, boolean isWhiteTurn) {
        float[][] output = new float[1][1]; // Assuming model outputs a single float
        float bestScore = isWhiteTurn ? -Float.MAX_VALUE : Float.MAX_VALUE;
        String bestMove = null;

        for (int i = 0; i < inputTensors.size(); i++) {
            float[][][][] input = inputTensors.get(i);
            interpreter.run(input, output);
            float score = output[0][0];

            if (isWhiteTurn && score > bestScore) {
                bestScore = score;
                bestMove = moves.get(i);
            } else if (!isWhiteTurn && score < bestScore) {
                bestScore = score;
                bestMove = moves.get(i);
            }
        }
        return bestMove;
    }



}
