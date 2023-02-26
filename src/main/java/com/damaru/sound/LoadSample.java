package com.damaru.sound;

import com.jsyn.data.FloatSample;
import com.jsyn.util.SampleLoader;

import java.io.File;
import java.io.IOException;

public class LoadSample {

    private static final int PAGES_PER_SECOND = 4;

    private void test() throws IOException {
        File file = new File("test.wav");
        SampleLoader.setJavaSoundPreferred(false);
        FloatSample sample = SampleLoader.loadFloatSample(file);
        double seconds = sample.getNumFrames() / sample.getFrameRate();
        System.out.println("Sample has: channels  = " + sample.getChannelsPerFrame());
        System.out.println("            frames    = " + sample.getNumFrames());
        System.out.println("            rate      = " + sample.getFrameRate());
        System.out.println("            seconds    = " + seconds);

        int framesPerPage = ((int) sample.getFrameRate()) / PAGES_PER_SECOND;

        int pageNo = 0;
        int framesLeft = sample.getNumFrames();

        while (framesLeft > 0) {
            framesLeft = doPage(sample, pageNo++, framesPerPage, framesLeft);
        }

    }

    private int doPage(FloatSample sample, int pageNo, int framesPerPage, int framesLeft) {
        int startFrame = pageNo * framesPerPage;
        int framesToDo = Math.min(framesPerPage, framesLeft);
        float[] buffer = new float[framesToDo * 2];
        sample.read(startFrame, buffer, 0, framesToDo);
        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;

        for (int i = 0; i < buffer.length; i++) {
            float val = buffer[i];
            if (val < min) min = val;
            if (val > max) max = val;
        }

        System.out.printf("page: %3d framesToDo: %5d min: %f max: %f\n", pageNo, framesToDo, min, max);
        return framesLeft - framesToDo;
    }

    public static void main(String[] args) throws IOException {
        new LoadSample().test();
    }

}
