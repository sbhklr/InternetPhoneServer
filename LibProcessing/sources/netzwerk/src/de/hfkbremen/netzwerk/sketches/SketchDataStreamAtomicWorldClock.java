package de.hfkbremen.netzwerk.sketches;

import de.hfkbremen.netzwerk.NetzwerkClient;
import processing.core.PApplet;

public class SketchDataStreamAtomicWorldClock extends PApplet {

    private NetzwerkClient mClient;

    public void setup() {
        size(15, 15);
        frameRate(1);

        mClient = new NetzwerkClient(this, "localhost", "time");
    }

    public void draw() {
        mClient.send("local", hour(), minute(), second());
    }

    public static void main(String[] args) {
        PApplet.main(SketchDataStreamAtomicWorldClock.class.getName());
    }
}
