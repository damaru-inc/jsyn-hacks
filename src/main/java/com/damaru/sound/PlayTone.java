package com.damaru.sound;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.ports.UnitGatePort;
import com.jsyn.ports.UnitInputPort;
import com.jsyn.ports.UnitVariablePort;
import com.jsyn.unitgen.EnvelopeDAHDSR;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.SineOscillator;
import com.jsyn.util.WaveRecorder;

import java.io.File;
import java.io.IOException;
import java.time.Instant;

public class PlayTone {

    private static final double DURATION = 4.0;
    private SineOscillator oscillator1 = new SineOscillator();
    private SineOscillator oscillator2 = new SineOscillator();
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

        synth.start();
        synth.add(oscillator1);
        synth.add(oscillator2);

        if (play) {
            // Add a stereo audio output unit.
            lineOut = new LineOut();
            synth.add(lineOut);

            // Connect the oscillator to both channels of the output.
            oscillator1.output.connect(0, lineOut.input, 0);
            oscillator2.output.connect(0, lineOut.input, 1);
        }

        if (record) {
            oscillator1.output.connect(0, recorder.getInput(), 0);
            oscillator2.output.connect(0, recorder.getInput(), 1);
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
        oscillator1.amplitude.set(vol);
        oscillator1.frequency.set(freq);
        oscillator2.amplitude.set(vol);
        oscillator2.frequency.set(freq * 1.5);
        System.out.printf("vol %1f freq %4f %s\n", vol, freq, Instant.now().toString());
    }

    public static void main(String[] args) throws IOException {
        new PlayTone().test();
    }
}
