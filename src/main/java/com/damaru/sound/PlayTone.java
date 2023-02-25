package com.damaru.sound;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.SineOscillator;
import com.jsyn.util.WaveRecorder;

import java.io.File;
import java.io.IOException;
import java.time.Instant;

public class PlayTone {

    private static final double DURATION = 4.0;
    private static final int FRAMERATE = 40_000;
    private SineOscillator oscillator = new SineOscillator();
    Synthesizer synth = JSyn.createSynthesizer();

    LineOut lineOut;
    private WaveRecorder recorder;

    boolean play = true;
    boolean record = false;

    private void test() throws IOException {

        if (record) {
            synth.setRealTime(false);
            File waveFile = new File("test.wav");
            recorder = new WaveRecorder(synth, waveFile);
        }

        // Create a context for the synthesizer.

        // Start synthesizer using default stereo output at 44100 Hz.
        synth.start(FRAMERATE);

        // Add a tone generator.
        synth.add(oscillator);

        if (play) {
            // Add a stereo audio output unit.
            lineOut = new LineOut();
            synth.add(lineOut);

            // Connect the oscillator to both channels of the output.
            oscillator.output.connect(0, lineOut.input, 0);
            oscillator.output.connect(0, lineOut.input, 1);
        }

        if (record) {
            oscillator.output.connect(0, recorder.getInput(), 0);
            oscillator.output.connect(0, recorder.getInput(), 1);
        }


        // Set the frequency and amplitude for the sine wave.
        set(0.0, 100.0);

        // We only need to start the LineOut. It will pull data from the
        // oscillator.

        if (play) lineOut.start();
        if (record) recorder.start();

        // Sleep while the sound is generated in the background.
        try {
            double time = synth.getCurrentTime();
            time += play(time, 100.0);
            time += play(time, 200.0);
            time += play(time, 300.0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Stop everything.

        if (record) {
            recorder.stop();
            recorder.close();
        }

        synth.stop();
    }

    private double play(double time, double freq) throws InterruptedException {
        set(0.8, freq);
        synth.sleepUntil(time + DURATION / 2.0);
        set(0.0, freq);
        synth.sleepUntil(time + DURATION);
        return DURATION;
    }

    private void set(double vol, double freq) {
        oscillator.amplitude.set(vol);
        oscillator.frequency.set(freq);
        System.out.printf("vol %1f freq %4f %s\n", vol, freq, Instant.now().toString());
    }

    public static void main(String[] args) throws IOException {
        new PlayTone().test();
    }
}
