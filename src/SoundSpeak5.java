import controlP5.ControlP5;
import controlP5.Textfield;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import processing.core.PApplet;
import processing.serial.Serial;
import java.io.IOException;

public class SoundSpeak5 extends PApplet {

    ControlP5 cp5;
    private boolean getURL = false;
    private String inputAddress;
    private String rawIPAddress;
    private String readableIPAddress;

    private Serial myPort;  // Create object from Serial class
    private String val;     // Data received from the serial port



    public void settings() {
        size(100, 100);
    }

    public void setup() {

        background(0);

        //enableInputTextbox();

        String portName = Serial.list()[3]; //change the 0 to a 1 or 2 etc. to match your port
        myPort = new Serial(this, portName, 9600);

    }


    public void draw() {

        if ( myPort.available() > 0)
        {  // If data is available,
            rawIPAddress = myPort.readStringUntil(13);
            getURL = true;
        }
        println(rawIPAddress); //print it out in the console



        if (getURL) {

            getCleanIP();

            Document webpage = null;
            try {
                webpage = Jsoup.connect(readableIPAddress).get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String webpageHtml = webpage.html();
            Document doc = Jsoup.parseBodyFragment(webpageHtml);
            String webpageText = doc.body().text();
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

    public void submit() {
        inputAddress = cp5.get(Textfield.class, "url").getText();
        println(inputAddress);
        getURL = true;

    }


    public void getCleanIP() {

        rawIPAddress = inputAddress;
        String partOne = rawIPAddress.substring(0, 3);
        String partTwo = rawIPAddress.substring(3, 6);
        String partThree = rawIPAddress.substring(6, 9);
        String partFour = rawIPAddress.substring(9, 12);

        partOne = Integer.valueOf(partOne).toString();
        partTwo = Integer.valueOf(partTwo).toString();
        partThree = Integer.valueOf(partThree).toString();
        partFour = Integer.valueOf(partFour).toString();

        readableIPAddress = "http://" + partOne + "." + partTwo + "." + partThree + "." + partFour;

    }

    private void enableInputTextbox() {
        cp5 = new ControlP5(this);

        cp5.addTextfield("url")
                .setPosition(20, 170)
                .setSize(200, 40)
                .setFont(createFont("arial", 12))
                .setAutoClear(false)
        ;

        cp5.addBang("submit")
                .setPosition(240, 170)
                .setSize(80, 40)
                .getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER)
        ;
    }


    public static void main(String[] args) {
        PApplet.main(SoundSpeak5.class.getName());
    }
}


