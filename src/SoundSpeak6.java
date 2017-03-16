import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import processing.core.PApplet;
import processing.serial.Serial;

import java.io.IOException;

public class SoundSpeak6 extends PApplet {

    private static final int ConnectCommandByteCount = 15;
	private static final int BaudRate = 9600;
	public static final String SerialPortName = "/dev/tty.usbmodem1411";
	
    private Serial serialPort;
    private StringBuffer serialDataBuffer = new StringBuffer();

    public void settings() {
        size(200, 200);
    }

    public void setup() {
        background(0,0,0);
        System.out.println(Serial.list());
        serialPort = new Serial(this, SerialPortName, BaudRate);
    }


    public void draw() {

    	while(serialPort.available() > 0){
    		char currentChar = serialPort.readChar();
    		serialDataBuffer.append(currentChar);
    		if(currentChar == '\n'){
    			executeCommand(serialDataBuffer.toString());
    		}
    	}
    }

	private void executeCommand(String command) {
		String commandSymbol = command.substring(0,1);
		
		if(commandSymbol.equals("c")){
			connect(command.substring(2));
		} else if(commandSymbol.equals("ajsklfdjas")){
			
		}
	}

	private void connect(String rawIPAddress) {
		println(rawIPAddress);
		String webpageText = getWebPageBody(rawIPAddress);
		if(webpageText != null){
			readWebpage(webpageText);            	
		} else {
			//TODO send 404 not found sound
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
        speech.say(voice, content.substring(0, 100));
    }

//    public void submit() {
//        inputAddress = cp5.get(Textfield.class, "url").getText();
//        println(inputAddress);
//        getURL = true;
//
//    }


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

    public static void main(String[] args) {
        PApplet.main(SoundSpeak6.class.getName());
    }
}
