import processing.core.PApplet;

public class Main extends PApplet {

	private static final String dialing = "d";
    private static final String connect = "c";
    private static final String hangup = "h";
    private static final String pickup = "p";
    private static final String ring = "r";
    private static final String incognito = "i";

    private static final int RECEIVER_PICKUP_DELAY = 4000;
    private SerialConnection serialConnection;

	private SoundPlayer soundPlayer;
	private SpeechPlayer speechPlayer;
	private UIManager uiManager;
	private boolean introMessagePlayed = false;
	private WebContentReader webContentReader;
	private StateManager stateManager;

    public void settings() {
        UIManager.applySettings(this);
    }

    public void setup() {
    	stateManager = new StateManager();
    	uiManager = new UIManager(this);
        uiManager.setup();
        soundPlayer = new SoundPlayer(this);
        speechPlayer = new SpeechPlayer();
        serialConnection = new SerialConnection(this);
        webContentReader = new WebContentReader(soundPlayer, speechPlayer);
    }

    public void draw() {
    	String command = serialConnection.readData();
    	if(command != null){
    		System.out.println("Command received: " + command);
    		executeCommand(command);
    	}
    	
    	if(webContentReader.contentAvailableForPlayback()){
    		if(!stateManager.reiceverPickedUp && !stateManager.callingPhone){
    			callPhone();
    			stateManager.callingPhone = true;
    		} else if(stateManager.reiceverPickedUp){
    			webContentReader.readAvailableContent(0);
    		}
    	}
    }

    private void executeCommand(String command) {
        String commandSymbol = command.substring(0, 1);

        if (commandSymbol.equals(connect)) {
            connect(command.substring(2));
        } else if (commandSymbol.equals(hangup)) {
            handleHangupCommand();
        } else if (commandSymbol.equals(pickup)) {
        	handlePickupCommand();
        } else if (commandSymbol.equals(dialing)) {
            stopSound();
        } else if (commandSymbol.equals(incognito)) {

            //add incognito mode

        } else if (commandSymbol.equals(ring)) {
            callPhone();
        }
    }

	private void handleHangupCommand() {
		stopSound();
		stateManager.lastHangupTime = millis();
		stateManager.reiceverPickedUp = false;
	}

	private void handlePickupCommand() {
		stateManager.reiceverPickedUp = true;
		stateManager.callingPhone = false;
		
		if(webContentReader.contentAvailableForPlayback()){
			webContentReader.readAvailableContent(RECEIVER_PICKUP_DELAY);
		} else {
			int hangupDuration = millis() - stateManager.lastHangupTime;
			
			if (hangupDuration > RECEIVER_PICKUP_DELAY || !introMessagePlayed ){
				println("Playing Intro Message...");
				introMessagePlayed = true;
				playIntroMessage();
			} else if (hangupDuration < RECEIVER_PICKUP_DELAY) {
				println("Playing pick up tone...");
				soundPlayer.playSoundFile("resources/dialtone.wav", true);
			}
		}
	}

	private void callPhone() {
		String outputCommand = "r:1\n";            
		serialConnection.writeData(outputCommand);
		println("call phone");
	}

    private void connect(String rawIPAddress) {
    	webContentReader.read(rawIPAddress);
    }

    private void stopSound() {
        soundPlayer.stop();
        speechPlayer.stop();
    }

    private void playIntroMessage() {
        speechPlayer.say("Welcome to the internet. Dial for websites.", "Alex", 2000);
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
