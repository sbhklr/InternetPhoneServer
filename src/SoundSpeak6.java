import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import processing.core.PApplet;
import processing.serial.Serial;

import java.io.IOException;

public class SoundSpeak6 extends PApplet {

    public static final String SerialPortName = "/dev/tty.usbmodem1411";
    private boolean getURL = false;
    private String inputAddress;
    private String rawIPAddress;

    private Serial myPort;  // Create object from Serial class


    public void settings() {
        size(200, 200);
    }

    public void setup() {

        background(0,0,0);
        println(Serial.list());
        myPort = new Serial(this, SerialPortName, 9600);
    }


    public void draw() {

        if (myPort.available() > 0) {  // If data is available,
            //rawIPAddress = myPort.readStringUntil('\n');
            rawIPAddress = myPort.readString();
            getURL = true;
            println(rawIPAddress);
        }

        if (getURL) {

            Document webpage = null;
            try {
                webpage = Jsoup.connect(getCleanIP()).get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String webpageHtml = webpage.html();
            Document doc = Jsoup.parseBodyFragment(webpageHtml);
            String webpageText = doc.body().text();
            println("connected");
            println(webpageText.substring(0, 250));

            readWebpage(webpageText);

            getURL = false;
        }



    }

    private void readWebpage(String CIIDText) {
        String voice = "Alex";
        SpeechSynthesis speech = new SpeechSynthesis();
        speech.setWordsPerMinute(175);
        speech.blocking(false);
        speech.say(voice, CIIDText.substring(0, 100));
    }

//    public void submit() {
//        inputAddress = cp5.get(Textfield.class, "url").getText();
//        println(inputAddress);
//        getURL = true;
//
//    }


    public String getCleanIP() {

        String partOne = rawIPAddress.substring(0, 3);
        String partTwo = rawIPAddress.substring(3, 6);
        String partThree = rawIPAddress.substring(6, 9);
        String partFour = rawIPAddress.substring(9, 12);

        partOne = Integer.valueOf(partOne).toString();
        partTwo = Integer.valueOf(partTwo).toString();
        partThree = Integer.valueOf(partThree).toString();
        partFour = Integer.valueOf(partFour).toString();

        return "http://" + partOne + "." + partTwo + "." + partThree + "." + partFour;
    }


    public static void main(String[] args) {
        PApplet.main(SoundSpeak6.class.getName());
    }
}


