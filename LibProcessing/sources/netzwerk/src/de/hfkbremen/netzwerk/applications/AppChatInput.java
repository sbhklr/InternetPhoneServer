package de.hfkbremen.netzwerk.applications;

import de.hfkbremen.netzwerk.NetzwerkClient;
import processing.core.PApplet;

public class AppChatInput extends PApplet {

    private controlP5.ControlP5 cp5;

    private NetzwerkClient mClient;

    private boolean onTop = true;

    public void settings() {
        size(displayWidth / 2 - 10, 50);
    }

    public void setup() {
        frameRate(15);
        mClient = new NetzwerkClient(this, "localhost", "OSChat");

        cp5 = new controlP5.ControlP5(this);

        cp5.addTextfield("msg").setPosition(10, 10).setAutoClear(false).setSize(displayWidth / 2 - 55, 20);

        // create a toggle
        cp5.addToggle("onTop").setPosition(displayWidth / 2 - 35, 10).setSize(20, 20);

    }

    public void draw() {
        background(0);
        surface.setAlwaysOnTop(onTop);
    }

    public void clear() {
        cp5.get(controlP5.Textfield.class, "msg").clear();
    }

    public void controlEvent(controlP5.ControlEvent theEvent) {
        if (theEvent.isAssignableFrom(controlP5.Textfield.class)) {
            if (theEvent.getName().equals("msg")) {
                sendMsg(theEvent.getStringValue());
            }
            clear();
        }
    }

    public void sendMsg(String theMsg) {
        mClient.send("msg", theMsg);
    }

    public static void main(String[] args) {
        PApplet.main(AppChatInput.class.getName());
    }
}
