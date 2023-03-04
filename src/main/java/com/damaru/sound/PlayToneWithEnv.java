package com.damaru.sound;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.unitgen.EnvelopeDAHDSR;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.SineOscillator;
import com.jsyn.util.WaveRecorder;

import java.io.File;
import java.io.IOException;

/**
 * This plays and/or records a sequence of sine tones.
 */
public class PlayToneWithEnv {

    //private static final int[] freqs = {75, 150, 300, 600, 1200, 2400, 4800, 9600};

    private static final int[] freqs = {
            100, 150, 200, 300,
            400, 600, 800, 1200,
            1600, 2400, 3200, 4800,
            6400, 9600, 12800, 19200};


    private static final double DURATION = 1.0;
    private static final double ATTACK = 0.01;
    private static final double DUTY = 0.5;
    private static final double RELEASE = ATTACK;

    private SineOscillator oscillator1 = new SineOscillator();
    private SineOscillator oscillator2 = new SineOscillator();
    Synthesizer synth = JSyn.createSynthesizer();

    LineOut lineOut;
    private WaveRecorder recorder;
    private EnvelopeDAHDSR dahdsr;

    boolean play = false;
    boolean record = true;

    private void test() throws IOException {


        if (record) {
            synth.setRealTime(false);
            File waveFile = new File("test.wav");
            recorder = new WaveRecorder(synth, waveFile);
        }

        synth.start();
        synth.add(oscillator1);
        synth.add(oscillator2);
        synth.add(dahdsr = new EnvelopeDAHDSR());

        dahdsr.output.connect(oscillator1.amplitude);
        dahdsr.output.connect(oscillator2.amplitude);
        dahdsr.attack.set(ATTACK);
        dahdsr.release.set(RELEASE);

        if (play) {
            lineOut = new LineOut();
            synth.add(lineOut);
            oscillator1.output.connect(0, lineOut.input, 0);
            oscillator2.output.connect(0, lineOut.input, 1);
        }

        if (record) {
            oscillator1.output.connect(0, recorder.getInput(), 0);
            oscillator2.output.connect(0, recorder.getInput(), 1);
        }

        if (play) lineOut.start();
        if (record) recorder.start();

        try {
            double time = synth.getCurrentTime();
            for (int i = 0; i < freqs.length; i++) {
                play(time, freqs[i]);
                time += DURATION;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (record) {
            recorder.stop();
            recorder.close();
        }

        synth.stop();
    }

    private void play(double time, int freq) throws InterruptedException {
        set(freq);
        dahdsr.input.set(0.5);
        synth.sleepUntil(time + DURATION * DUTY);
        dahdsr.input.set(0.0);
        synth.sleepUntil(time + DURATION);
    }

    private void set(int freq) {
        oscillator1.frequency.set(freq);
        oscillator2.frequency.set(freq);
    }

    public static void main(String[] args) throws IOException {
        new PlayToneWithEnv().test();
    }
}
