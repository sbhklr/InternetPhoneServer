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

public class SoundSpeak9 extends PApplet {

    ControlP5 cp5;

    private static final String dialing = "d";
    private static final String connect = "c";
    private static final String hangup = "h";
    private static final String pickup = "p";
    private static final String ring = "r";
    private static final String navigate = "n";
    private static final String incognito = "i";

    private boolean needIntro = true;
    private String testCommand;
    private boolean finishedIntroMessage = false;
    private boolean time = false;

    private int lastHangupTime;
    private int hangupDuration;
    private int longHangupTime = 4000;
    private int firstPickupTime = 1000;
    private SpeechSynthesis speech;
    private SerialConnection serialConnection;

	private SoundPlayer soundPlayer;

    public static final String VOICE = "Yuri";

    public void settings() {
        size(600, 400);
    }

    public void setup() {
        background(0, 0, 0);
        soundPlayer = new SoundPlayer(this);
        setupSpeech();
        enableInputTextbox();
        serialConnection = new SerialConnection(this);
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
            println("Connecting");
            connect(command.substring(2));
            needIntro = false;

        } else if (commandSymbol.equals(hangup)) {
            stopSound();
            needIntro = false;
            lastHangupTime = millis();
            println("hung up");

        } else if (commandSymbol.equals(pickup)) {
            hangupDuration = millis() - lastHangupTime;
            if (hangupDuration > longHangupTime || millis() < firstPickupTime){
                playIntroMessage();
            } else if (hangupDuration < longHangupTime && finishedIntroMessage) {
                setupDialAudio();
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
        String webpageText = getWebPageBody(rawIPAddress);
        if (webpageText != null) {
            readWebpage(webpageText);
        } else {
            setSitAudio();
            println("Couldn't connect");
        }
    }

    private String getWebPageBody(String ipAddress) {
        Document webpage = null;
        try {
            webpage = Jsoup.connect(getURLFromIP(ipAddress)).get();
        } catch (Exception e) {
            return null;
        }
        String webpageHtml = webpage.html();
        Document doc = Jsoup.parseBodyFragment(webpageHtml);
        String webpageText = doc.body().text();
        return webpageText;
    }

    private void readWebpage(String content) {
        String voice = "Alex";
        SpeechSynthesis speech = new SpeechSynthesis();
        speech.setWordsPerMinute(175);
        speech.blocking(false);
        speech.say(voice, content.substring(0, 450));
        soundPlayer.stop();
        println(content.substring(0, 450));
    }

    public String getURLFromIP(String ipAddress) {

        String partOne = ipAddress.substring(0, 3);
        String partTwo = ipAddress.substring(3, 6);
        String partThree = ipAddress.substring(6, 9);
        String partFour = ipAddress.substring(9, 12);

        partOne = Integer.valueOf(partOne).toString();
        partTwo = Integer.valueOf(partTwo).toString();
        partThree = Integer.valueOf(partThree).toString();
        partFour = Integer.valueOf(partFour).toString();

        return "http://" + partOne + "." + partTwo + "." + partThree + "." + partFour;
    }

    private void setSitAudio() {        
        soundPlayer.playSoundFile("resources/SIT.wav", true);
    }

    private void setupDialAudio() {
        soundPlayer.playSoundFile("resources/dialtone.mp3", true);
    }

    private void stopSound() {
        soundPlayer.stop();
        stopSpeech();
    }

    private void stopSpeech() {
        try {
            Runtime.getRuntime().exec("killall say");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void playIntroMessage() {
         delay(2000);
            speech.say(VOICE, "Welcome to the internet. Dial for websites.");
            println("Intro Message");
        finishedIntroMessage = true;
    }

    private void setupSpeech() {
        speech = new SpeechSynthesis();
        speech.setWordsPerMinute(170);
        speech.blocking(false);
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
        PApplet.main(SoundSpeak9.class.getName());
    }
}
