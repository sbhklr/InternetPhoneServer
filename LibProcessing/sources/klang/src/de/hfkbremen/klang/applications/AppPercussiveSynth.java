package de.hfkbremen.klang.applications;

import de.hfkbremen.klang.Beat;
import de.hfkbremen.klang.Instrument;
import de.hfkbremen.klang.Note;
import de.hfkbremen.klang.Scale;
import de.hfkbremen.klang.Synthesizer;
import processing.core.PApplet;

public class AppPercussiveSynth extends PApplet {

    private Synthesizer mSynth;

    private Beat mBeat;

    private static final int O = -1;
    private static final int I = 0;

    private static final int BASS = 0;
    private static final int SNARE = 1;
    private static final int HIHAT = 2;
    private static final int NUMBER_OF_INSTRUMENTS = 3;

    private final int[][] mSteps = new int[NUMBER_OF_INSTRUMENTS][];

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mSynth = Synthesizer.getSynth("jsyn");

        mSynth.instrument(BASS).osc_type(Instrument.SQUARE);
        mSynth.instrument(BASS).attack(0.01f);
        mSynth.instrument(BASS).decay(0.04f);
        mSynth.instrument(BASS).sustain(0.0f);
        mSynth.instrument(BASS).release(0.0f);
        mSteps[BASS] = new int[]{
            I, O, O, O,
            O, O, O, O,
            I, O, O, O,
            O, O, O, I,};

        mSynth.instrument(SNARE).osc_type(Instrument.NOISE);
        mSynth.instrument(SNARE).attack(0.01f);
        mSynth.instrument(SNARE).decay(0.2f);
        mSynth.instrument(SNARE).sustain(0.0f);
        mSynth.instrument(SNARE).release(0.0f);
        mSteps[SNARE] = new int[]{
            O, O, O, O,
            I, O, O, O,
            O, O, O, O,
            I, O, O, O,};

        mSynth.instrument(HIHAT).osc_type(Instrument.NOISE);
        mSynth.instrument(HIHAT).attack(0.01f);
        mSynth.instrument(HIHAT).decay(0.04f);
        mSynth.instrument(HIHAT).sustain(0.0f);
        mSynth.instrument(HIHAT).release(0.0f);
        mSteps[HIHAT] = new int[]{
            I, O, I, O,
            I, O, I, O,
            I, O, I, O,
            I, I, I, I,};

        Synthesizer.createInstrumentsGUI(this, mSynth, BASS, SNARE, HIHAT);

        mBeat = new Beat(this);
        mBeat.bpm(130 * 4);
    }

    public void draw() {
        background(127);
    }

    public void beat(int pBeat) {
        for (int i = 0; i < NUMBER_OF_INSTRUMENTS; i++) {
            mSynth.instrument(i);
            int mStep = mSteps[i][pBeat % mSteps[i].length];
            if (mStep == I) {
                int mNote = Scale.note(Scale.HALF_TONE, Note.NOTE_C3, mStep);
                mSynth.noteOn(mNote, 127);
            } else {
                mSynth.noteOff();
            }
        }
    }

    public static void main(String[] args) {
        PApplet.main(AppPercussiveSynth.class
                .getName());
    }
}
