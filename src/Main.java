import processing.core.PApplet;

public class Main extends PApplet {

    private static final String dialing = "d";
    private static final String connect = "c";
    private static final String hangup = "h";
    private static final String pickup = "p";
    private static final String ring = "r";
    private static final String incognito = "i";

    private int lastHangupTime = 0;
    private static final int INTRO_MESSAGE_TIMEOUT = 4000;
    private SerialConnection serialConnection;

	private SoundPlayer soundPlayer;
	private HTTPReader httpReader;
	private SpeechPlayer speechPlayer;
	private UIManager uiManager;
	private boolean introMessagePlayed = false;

    public void settings() {
        UIManager.applySettings(this);
    }

    public void setup() {
    	uiManager = new UIManager(this);
        uiManager.setup();
        soundPlayer = new SoundPlayer(this);
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
            
        } else if (commandSymbol.equals(hangup)) {
            stopSound();
            lastHangupTime = millis();

        } else if (commandSymbol.equals(pickup)) {
            int hangupDuration = millis() - lastHangupTime;
            
            if (hangupDuration > INTRO_MESSAGE_TIMEOUT || !introMessagePlayed ){
            	println("Playing Intro Message...");
            	introMessagePlayed = true;
                playIntroMessage();
            } else if (hangupDuration < INTRO_MESSAGE_TIMEOUT) {
            	println("Playing pick up tone...");
                soundPlayer.playSoundFile("resources/dialtone.wav", true);
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
    	//TODO add delay
        //delay(2000);
        speechPlayer.say("Welcome to the internet. Dial for websites.");
    }

    public void sendCommand() {
        String command = uiManager.getCommandFromTextField();
        println("Executing command from UI: " + command);
        executeCommand(command);
    }

    public static void main(String[] args) {
        PApplet.main(Main.class.getName());
    }
}
