import controlP5.ControlP5;
import controlP5.Textfield;
import processing.core.PApplet;

public class UIManager {
	
	ControlP5 cp5;
	private PApplet applet;

	public UIManager(PApplet applet) {
		this.applet = applet;
	}

	public void setup(){
		this.applet.background(0, 0, 0);
		enableInputTextbox();
	}
	
	public static void applySettings(PApplet applet){
		applet.size(600, 400);
	}
	
	public String getCommandFromTextField(){
		return cp5.get(Textfield.class, "command").getText();
	}
	
	private void enableInputTextbox() {
        cp5 = new ControlP5(this.applet);

        cp5.addTextfield("command")
                .setPosition(20, 170)
                .setSize(200, 40)
                .setFont(this.applet.createFont("arial", 12))
                .setAutoClear(false)
        ;

        cp5.addBang("sendCommand")
                .setPosition(240, 170)
                .setSize(80, 40)
                .getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER);
    }
}
