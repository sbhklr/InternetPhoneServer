import ddf.minim.AudioPlayer;
import ddf.minim.Minim;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import processing.core.PApplet;
import processing.serial.Serial;

import java.io.File;


public class SoundSpeak8 extends PApplet {

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
    private AudioPlayer sitPlayer;
    private boolean needIntro = true;


    public void settings() {
        size(200, 200);
    }

    public void setup() {
        background(0, 0, 0);
        serialPort = new Serial(this, SerialPortName, BaudRate);
        setupSitAudio();
    }


    public void draw() {

        while (serialPort.available() > 0) {
            char currentChar = serialPort.readChar();
            serialDataBuffer.append(currentChar);
            if (currentChar == '\n') {
                executeCommand(serialDataBuffer.toString());
                serialDataBuffer = new StringBuffer();
            }
        }
    }

    private void executeCommand(String command) {
        String commandSymbol = command.substring(0, 1);

        if (commandSymbol.equals(connect)) {
            connect(command.substring(2));
            needIntro = false;

        } else if (commandSymbol.equals(hangup)) {
            serialDataBuffer = new StringBuffer();
            sitPlayer.close();
            needIntro = false;

        } else if (commandSymbol.equals(pickup)) {
            playIntroMessage();
        } else {

        }


    }

    private void connect(String rawIPAddress) {
        println(rawIPAddress);
        String webpageText = getWebPageBody(rawIPAddress);
        if (webpageText != null) {
            readWebpage(webpageText);
        } else {
            sitPlayer.rewind();
            sitPlayer.loop();
            System.out.println("Couldn't connect");
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
        println(content.substring(0, 100));
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

    private void setupSitAudio() {
        String filePath = "/Users/james/Documents/intelliJ/TangibleInternet/src/data/SIT.wav";
        minim = new Minim(this);
        File audioFile = new File(filePath);
        String audioFilePath = audioFile.getAbsolutePath();
        sitPlayer = minim.loadFile(audioFilePath);

    }


    private void playIntroMessage() {
        while (needIntro) {
            String introMessage = "Welcome to the Internet! Dial 111 for directory service. Dial 12 digits for a webpage.";
            String voice = "Alex";
            SpeechSynthesis speech = new SpeechSynthesis();
            speech.setWordsPerMinute(175);
            speech.blocking(false);
            speech.say(voice, introMessage);
        }
    }


//    public void submit() {
//        inputAddress = cp5.get(Textfield.class, "url").getText();
//        println(inputAddress);
//        getURL = true;
//
//    }

    public static void main(String[] args) {
        PApplet.main(SoundSpeak8.class.getName());
    }
}
