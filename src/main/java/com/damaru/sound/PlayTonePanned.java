package com.damaru.sound;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.unitgen.EnvelopeDAHDSR;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.Pan;
import com.jsyn.unitgen.SineOscillator;
import com.jsyn.util.WaveRecorder;

import java.io.File;
import java.io.IOException;

/**
 * This creates a sound file containing a sequence of sine tones, panning each one left to right.
 *
 * It was created to test the FFT library in processing.org.
 */
public class PlayTonePanned {

    private static final int[] freqs = {300, 600, 900, 1200, 2000, 3000, 5000, 8000, 12_000, 16_000};

    private static final double DURATION = 2.0; // On/off cycle length in seconds
    private static final double ATTACK = 0.01;
    private static final double RELEASE = ATTACK;
    private static final double DUTY = 0.5; // Duty cycle of on/off periods
    private static final int PAN_STEPS = (int) (30 * DURATION); // This will result in 30 pan steps per second.
    private static final double PAN_INTERVAL_BETWEEN_STEPS = (DURATION * DUTY) / PAN_STEPS;
    private static final double PAN_INCREMENT = 2.0 / PAN_STEPS;

    Synthesizer synth = JSyn.createSynthesizer();
    private SineOscillator osc = new SineOscillator();
    private LineOut lineOut;
    private Pan panner = new Pan();
    private WaveRecorder recorder;
    private EnvelopeDAHDSR dahdsr = new EnvelopeDAHDSR();

    boolean play = false;
    boolean record = true;

    private void test() throws IOException {

        if (record) {
            synth.setRealTime(false);
            File waveFile = new File("pan.wav");
            recorder = new WaveRecorder(synth, waveFile);
        }

        synth.start();
        synth.add(dahdsr);
        synth.add(osc);
        synth.add(panner);

        dahdsr.output.connect(osc.amplitude);
        dahdsr.attack.set(ATTACK);
        dahdsr.release.set(RELEASE);
        osc.output.connect(panner.input);

        if (play) {
            lineOut = new LineOut();
            synth.add(lineOut);
            panner.output.connect(0, lineOut.input, 0);
            panner.output.connect(1, lineOut.input, 1);
        }

        if (record) {
            panner.output.connect(0, recorder.getInput(), 0);
            panner.output.connect(1, recorder.getInput(), 1);
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
        osc.frequency.set(freq);
        panner.pan.set(-1.0);
        dahdsr.input.set(0.5);
        doPan(time);
        dahdsr.input.set(0.0);
        synth.sleepUntil(time + DURATION);
    }

    private void doPan(double time) throws InterruptedException {
        double pan = -1.0;
        for (int i = 0; i < PAN_STEPS; i++) {
            synth.sleepUntil(time + PAN_INTERVAL_BETWEEN_STEPS);
            time += PAN_INTERVAL_BETWEEN_STEPS;
            pan += PAN_INCREMENT;
            panner.pan.set(pan);
        }
    }

    public static void main(String[] args) throws IOException {
        new PlayTonePanned().test();
    }
}
