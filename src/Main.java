import controlP5.ControlP5;
import controlP5.Textfield;
import ddf.minim.AudioPlayer;
import ddf.minim.Minim;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import processing.core.PApplet;
import processing.serial.Serial;

import java.io.File;
import java.io.IOException;

public class Main extends PApplet {

    ControlP5 cp5;

    private static final String dialing = "d";
    private static final String connect = "c";
    private static final String hangup = "h";
    private static final String pickup = "p";
    private static final String ring = "r";
    private static final String incognito = "i";

    private boolean needIntro = true;
    private String testCommand;
    private boolean finishedIntroMessage = false;
    private boolean time = false;

    private int lastHangupTime;
    private int hangupDuration;
    private int longHangupTime = 4000;
    private int firstPickupTime = 1000;
    private SerialConnection serialConnection;

	private SoundPlayer soundPlayer;
	private HTTPReader httpReader;
	private SpeechPlayer speechPlayer;

    public static final String VOICE = "Yuri";

    public void settings() {
        size(600, 400);
    }

    public void setup() {
        background(0, 0, 0);
        soundPlayer = new SoundPlayer(this);
        enableInputTextbox();
        speechPlayer = new SpeechPlayer();
        serialConnection = new SerialConnection(this);
        httpReader = new HTTPReader();
    }

    public void draw() {
    	String command = serialConnection.readData();
    	if(command != null){
    		System.out.println("Command received: " + command);
    		executeCommand(command);
    	}
    }

    private void executeCommand(String command) {
        String commandSymbol = command.substring(0, 1);

        if (commandSymbol.equals(connect)) {
            connect(command.substring(2));
            needIntro = false;
            
        } else if (commandSymbol.equals(hangup)) {
            stopSound();
            needIntro = false;
            lastHangupTime = millis();

        } else if (commandSymbol.equals(pickup)) {
            hangupDuration = millis() - lastHangupTime;
            if (hangupDuration > longHangupTime || millis() < firstPickupTime){
                playIntroMessage();
            } else if (hangupDuration < longHangupTime && finishedIntroMessage) {
                soundPlayer.playSoundFile("resources/dialtone.wav", true);
                println("pick up tone");
            }

        } else if (commandSymbol.equals(dialing)) {
            stopSound();
            println("dial");
        } else if (commandSymbol.equals(incognito)) {

            //add incognito mode

        } else if (commandSymbol.equals(ring)) {
            String outputCommand = "r:1\n";            
			serialConnection.writeData(outputCommand);
            println("call phone");
        }
    }

    private void connect(String rawIPAddress) {
        String webpageText = httpReader.getWebPageBody(rawIPAddress);
        if (webpageText != null) {
        	soundPlayer.stop();
        	String shortenedContent = webpageText.substring(0, 450);
            speechPlayer.say(shortenedContent, "Alex");
        } else {
            soundPlayer.playSoundFile("resources/SIT.wav", true);
            println("Couldn't connect");
        }
    }

    private void stopSound() {
        soundPlayer.stop();
        speechPlayer.stop();
    }

    private void playIntroMessage() {
    	println("Intro Message");
        delay(2000);
        speechPlayer.say("Welcome to the internet. Dial for websites.", VOICE);
        finishedIntroMessage = true;
    }

    private void enableInputTextbox() {
        cp5 = new ControlP5(this);

        cp5.addTextfield("command")
                .setPosition(20, 170)
                .setSize(200, 40)
                .setFont(createFont("arial", 12))
                .setAutoClear(false)
        ;

        cp5.addBang("send")
                .setPosition(240, 170)
                .setSize(80, 40)
                .getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER);
    }

    public void send() {
        testCommand = cp5.get(Textfield.class, "command").getText();
        executeCommand(testCommand);
        println(testCommand);

    }

    public static void main(String[] args) {
        PApplet.main(Main.class.getName());
    }
}
