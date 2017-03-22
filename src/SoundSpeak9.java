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

    private static final int ConnectCommandByteCount = 15;
    private static final int BaudRate = 9600;

    private static final String dialing = "d";
    private static final String connect = "c";
    private static final String hangup = "h";
    private static final String pickup = "p";
    private static final String ring = "r";
    private static final String navigate = "n";
    private static final String incognito = "i";

    private Serial serialPort;
    private StringBuffer serialDataBuffer = new StringBuffer();

    private Minim minim;
    private AudioPlayer tonePlayer;

    private boolean needIntro = true;
    private String testCommand;
    private boolean finishedIntroMessage = false;
    private boolean time = false;

    private int lastHangupTime;
    private int hangupDuration;
    private int longHangupTime = 4000;
    private int firstPickupTime = 1000;
    private SpeechSynthesis speech;
    public static final String VOICE = "Yuri";

    public void settings() {
        size(600, 400);
    }

    public void setup() {
        background(0, 0, 0);
        minim = new Minim(this);
        serialPort = new Serial(this, getSerialPort(), BaudRate);
        setupSpeech();
        enableInputTextbox();
    }
    
    private String getSerialPort(){
    	for (String port : Serial.list()) {
			if(port.contains("usbmodem")) return port;
		}
    	return null;
    }

    public void draw() {
        while (serialPort.available() > 0) {
            char currentChar = serialPort.readChar();
            serialDataBuffer.append(currentChar);
            if (currentChar == '\n') {
                executeCommand(serialDataBuffer.toString());
                serialDataBuffer = new StringBuffer();
                println(serialDataBuffer);
            }
        }
    }

    private void executeCommand(String command) {
        String commandSymbol = command.substring(0, 1);

        if (commandSymbol.equals(connect)) {
            println("Connecting");
            connect(command.substring(2));
            needIntro = false;

        } else if (commandSymbol.equals(hangup)) {
            serialDataBuffer = new StringBuffer();
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
                tonePlayer.rewind();
                tonePlayer.loop();
                println("pick up tone");
            }

        } else if (commandSymbol.equals(dialing)) {
            stopSound();
            println("dial");
        } else if (commandSymbol.equals(incognito)) {

            //add incognito mode

        } else if (commandSymbol.equals(ring)) {
            serialPort.write("r:1\n");
            println("call phone");
        }
    }

    private void connect(String rawIPAddress) {
        println(rawIPAddress);
        String webpageText = getWebPageBody(rawIPAddress);
        if (webpageText != null) {
            readWebpage(webpageText);
        } else {
            setSitAudio();
            tonePlayer.rewind();
            tonePlayer.loop();
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
        stopTone();
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
        String filePath = "/Users/james/Documents/intelliJ/TangibleInternet/src/data/SIT.wav";
        playSoundFile(filePath);

    }

    private void playSoundFile(String filePath) {
        File audioFile = new File(filePath);
        String audioFilePath = audioFile.getAbsolutePath();
        tonePlayer = minim.loadFile(audioFilePath);
    }

    private void setupDialAudio() {
        String filePath = "/Users/james/Documents/intelliJ/TangibleInternet/src/data/dialtone.mp3";
        playSoundFile(filePath);

    }

    private void stopSound() {
        stopTone();
        stopSpeech();
    }

    private void stopSpeech() {
        try {
            Runtime.getRuntime().exec("killall say");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopTone() {
        if(tonePlayer != null) tonePlayer.close();
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
