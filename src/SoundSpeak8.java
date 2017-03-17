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


public class SoundSpeak8 extends PApplet {

    ControlP5 cp5;

    private static final int ConnectCommandByteCount = 15;
    private static final int BaudRate = 9600;
    public static final String SerialPortName = "/dev/tty.usbmodem1411";

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


    public void settings() {
        size(600, 400);
    }

    public void setup() {
        background(0, 0, 0);
//        serialPort = new Serial(this, SerialPortName, BaudRate);
        setSitAudio();
        enableInputTextbox();

    }


    public void draw() {

//        playIntroMessage();

//        while (serialPort.available() > 0) {
//            char currentChar = serialPort.readChar();
//            serialDataBuffer.append(currentChar);
//            if (currentChar == '\n') {
//                executeCommand(serialDataBuffer.toString());
//                serialDataBuffer = new StringBuffer();
//            }
//        }
    }

    private void executeCommand(String command) {
        String commandSymbol = command.substring(0, 1);

        if (commandSymbol.equals(connect)) {
            println("Connecting");
            connect(command.substring(2));
            needIntro = false;

        } else if (commandSymbol.equals(hangup)) {
            serialDataBuffer = new StringBuffer();
            tonePlayer.close();
            stopSpeech();
            needIntro = false;
            println("Hanging up");

        } else if (commandSymbol.equals(pickup)) {
            setupDialAudio();
            tonePlayer.rewind();
            tonePlayer.loop();
            println("Dial tone");
            // playIntroMessage();
        } else if (commandSymbol.equals(dialing)) {
            tonePlayer.close();
        }
    }

    private void connect(String rawIPAddress) {
        println(rawIPAddress);
        String webpageText = getWebPageBody(rawIPAddress);
        if (webpageText != null) {
            readWebpage(webpageText);
        } else {
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
        speech.say(voice, content.substring(0, 150));
        tonePlayer.close();
        println(content.substring(0, 150));
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
        minim = new Minim(this);
        File audioFile = new File(filePath);
        String audioFilePath = audioFile.getAbsolutePath();
        tonePlayer = minim.loadFile(audioFilePath);

    }

    private void setupDialAudio() {
        String filePath = "/Users/james/Documents/intelliJ/TangibleInternet/src/data/dialtone.mp3";
        minim = new Minim(this);
        File audioFile = new File(filePath);
        String audioFilePath = audioFile.getAbsolutePath();
        tonePlayer = minim.loadFile(audioFilePath);

    }

    private void stopSpeech() {
        try {
            Runtime.getRuntime().exec("killall say");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void playIntroMessage() {
        String voice = "Alex";
        SpeechSynthesis speech = new SpeechSynthesis();
        speech.setWordsPerMinute(175);
        speech.blocking(false);
        speech.say(voice, "Hello");
        println("Intro Message");
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
                .getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER)
        ;
    }

    public void send() {
        testCommand = cp5.get(Textfield.class, "command").getText();
        executeCommand(testCommand);
        println(testCommand);

    }

    public static void main(String[] args) {
        PApplet.main(SoundSpeak8.class.getName());
    }
}
