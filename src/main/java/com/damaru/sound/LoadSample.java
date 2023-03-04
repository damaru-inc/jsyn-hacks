package com.damaru.sound;

import com.jsyn.data.FloatSample;
import com.jsyn.util.SampleLoader;
import com.softsynth.math.FourierMath;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * This loads a sound file and can print out some information about it,
 * or play with fft or other functions.
 */
public class LoadSample {

    private static final int PAGES_PER_SECOND = 30;
    private static final int FFT_BANDS = 16;

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

        while (framesLeft > 0 && pageNo < 100) {
            // If youu call more than one of these, only the last should increment pageNo.
            //framesLeft = doFft(sample, pageNo, framesPerPage, framesLeft);
            //framesLeft = doPage(sample, pageNo++, framesPerPage, framesLeft);
            framesLeft = dumpPage(sample, pageNo++, framesPerPage, framesLeft);
        }

    }

    private int dumpPage(FloatSample sample, int pageNo, int framesPerPage, int framesLeft) {
        int startFrame = pageNo * framesPerPage;
        int framesToDo = Math.min(framesPerPage, framesLeft);
        int smaller = Math.min(framesPerPage, 64);
        float[] buffer = new float[smaller * 2];
        float[] chan1 = new float[smaller];
        float[] chan2 = new float[smaller];
        sample.read(startFrame, buffer, 0, smaller);

        int channelIndex = 0;

        for (int i = 0; i < buffer.length; i += 2) {
            chan1[channelIndex] = buffer[i];
            chan2[channelIndex] = buffer[i + 1];
            channelIndex++;
        }

        System.out.printf("page: %3d %s\n", pageNo, arrayToString(chan1));
        System.out.printf("          %s\n", arrayToString(chan2));
        System.out.println();
        return framesLeft - framesToDo;
    }

    private int doPage(FloatSample sample, int pageNo, int framesPerPage, int framesLeft) {
        int startFrame = pageNo * framesPerPage;
        int framesToDo = Math.min(framesPerPage, framesLeft);
        float[] buffer = new float[framesToDo * 2];
        float[] chan1 = new float[framesToDo];
        float[] chan2 = new float[framesToDo];
        sample.read(startFrame, buffer, 0, framesToDo);

        int channelIndex = 0;

        for (int i = 0; i < buffer.length; i += 2) {
            chan1[channelIndex] = buffer[i];
            chan2[channelIndex] = buffer[i + 1];
            channelIndex++;
        }

        trim(chan1);
        return framesLeft - framesToDo;
    }

    private float[] trim(float src[]) {
        float delta = 0.01f;
        int first = 0;
        int last = 0;

        for (int i = 0; i < src.length; i++) {
            if (Math.abs(src[i]) < delta) {
                first = i;
                break;
            }
        }

        for (int i = src.length - 1; i >= 0; i--) {
            if (Math.abs(src[i]) < delta) {
                last = i;
                break;
            }
        }

        System.out.printf("orig: %6d first: %5d last: %4d len: %d\n", src.length, first, last, last - first + 1);
        return src;
    }

    private int doFft(FloatSample sample, int pageNo, int framesPerPage, int framesLeft) {
        int startFrame = pageNo * framesPerPage;
        int framesToDo = Math.min(framesPerPage, framesLeft);
//        System.out.printf("page: %3d framesToDo: %5d framesLeft: %5d framesPerPage: %5d startFrame %5d\n", pageNo,
//                framesToDo, framesLeft, framesPerPage, startFrame);
        float[] buffer = new float[framesToDo * 2];
        sample.read(startFrame, buffer, 0, framesToDo);

        double[] chan1 = new double[framesToDo];
        double[] chan2 = new double[framesToDo];
        double[] imaginary1 = new double[framesToDo];
        double[] imaginary2 = new double[framesToDo];
        double[] magnitude1 = new double[FFT_BANDS];
        double[] magnitude2 = new double[FFT_BANDS];
        double[] target1 = new double[FFT_BANDS];
        double[] target2 = new double[FFT_BANDS];
        Arrays.fill(imaginary1, 0);
        Arrays.fill(imaginary2, 0);

        int channelIndex = 0;

        for (int i = 0; i < buffer.length && channelIndex < framesToDo; i += 2) {
            chan1[channelIndex] = buffer[i];
            chan2[channelIndex] = buffer[i + 1];
            channelIndex++;
        }

        doFftForChannel(target1, chan1, imaginary1, magnitude1);
        doFftForChannel(target2, chan2, imaginary2, magnitude2);

        System.out.printf("page: %3d %s\n", pageNo, arrayToString(target1));
        System.out.printf("          %s\n", arrayToString(target2));
        System.out.println();
        return framesLeft - framesToDo;
    }

    private String arrayToString(float[] vals) {
        StringBuffer sb = new StringBuffer();
        for (float d : vals) {
            sb.append(String.format("%6.3f ", d));
        }
        return sb.toString();
    }

    private String arrayToString(double[] vals) {
        StringBuffer sb = new StringBuffer();
        for (double d : vals) {
            sb.append(String.format("%5.2f ", d));
        }
        return sb.toString();
    }

    private void doFftForChannel(double[] target, double[] real, double[] imaginary, double[] magnitude) {
        FourierMath.fft(FFT_BANDS, real, imaginary);
        FourierMath.calculateMagnitudes(real, imaginary, magnitude);
        for (int i = 0; i < target.length; i++) {
            target[i] = (float) (2 * magnitude[i]);
        }
    }

    public static void main(String[] args) throws IOException {
        new LoadSample().test();
    }

}
