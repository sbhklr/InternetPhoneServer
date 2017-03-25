import data.SerialConnection;
import data.WebContentReader;
import logic.HistoryManager;
import logic.Mode;
import logic.StateManager;
import processing.core.PApplet;
import sound.SoundPlayer;
import sound.SpeechPlayer;

public class Main extends PApplet {

	private static final String DiallingCommand = "d";
    private static final String ConnectCommand = "c";
    private static final String HangupCommand = "h";
    private static final String PickupCommand = "p";
    private static final String RingCommand = "r";
    private static final String SetModeCommand = "m";

    private static final String DEFAULT_VOICE = "Alex";
    private static final String MODE_CONFIRMATION_DIGIT = "1";
    private static final int HISTORY_READING_DELAY = 2000;
    private static final int PICKUP_TONE_AFTER_CONFIRMATION_DELAY = 2750;
    private static final int RECEIVER_PICKUP_TO_EAR_DELAY = 3000;

    private SerialConnection serialConnection;
	private SoundPlayer soundPlayer;
	private SpeechPlayer speechPlayer;
	private UIManager uiManager;
	private boolean introMessagePlayed = false;
	private WebContentReader webContentReader;
	private StateManager stateManager;
	private HistoryManager historyManager;

    public void settings() {
        UIManager.applySettings(this);
    }

    public void setup() {
    	speechPlayer = new SpeechPlayer();
    	historyManager = new HistoryManager(speechPlayer);
    	stateManager = new StateManager(speechPlayer);
    	uiManager = new UIManager(this);
        uiManager.setup();
        soundPlayer = new SoundPlayer(this);
        serialConnection = new SerialConnection(this);
        webContentReader = new WebContentReader(soundPlayer, speechPlayer, stateManager);
    }

    public void draw() {
    	String command = serialConnection.readData();
    	if(command != null){
    		System.out.println("Command received: " + command);
    		executeCommand(command);
    	}
    	
    	if(stateManager.hasUnconfirmedMode() && stateManager.reiceverPickedUp){
    		stateManager.readModeConfirmationPrompt();
    		return;
    	}
    	
    	if(webContentReader.contentAvailableForPlayback()){
    		if(!stateManager.reiceverPickedUp && !stateManager.callingPhone){
    			callPhone();
    			stateManager.callingPhone = true;
    		} else if(stateManager.reiceverPickedUp){
    			webContentReader.readAvailableContent(0);
    		}
    		return;
    	}
    	
    	if(stateManager.getCurrentMode() == Mode.History && !historyManager.paused){
			historyManager.readHistory(HISTORY_READING_DELAY);
			return;
		}
    }

    private void executeCommand(String command) {
        String commandSymbol = command.substring(0, 1);

        if (commandSymbol.equals(ConnectCommand)) {
            connect(command.substring(2));
        } else if (commandSymbol.equals(HangupCommand)) {
            handleHangupCommand();
        } else if (commandSymbol.equals(PickupCommand)) {
        	handlePickupCommand();
        } else if (commandSymbol.equals(DiallingCommand)) {
            handleDiallingCommand(command);
        } else if (commandSymbol.equals(SetModeCommand)) {
        	handleSetModeCommand(command);
        } else if (commandSymbol.equals(RingCommand)) {
            callPhone();
        }
    }

	private void handleDiallingCommand(String command) {
		stopSound();
		String dialledDigit = command.substring(2, 3);
		String resetDiallingCommand = "rs:\n";            
		
		if(stateManager.hasUnconfirmedMode() && dialledDigit.equals(MODE_CONFIRMATION_DIGIT)){
			serialConnection.writeData(resetDiallingCommand);
			stateManager.confirmMode();
			stateManager.readConfirmationMessage();
			if(stateManager.getCurrentMode() != Mode.History)
				playPickupTone(PICKUP_TONE_AFTER_CONFIRMATION_DELAY);
		} else if(stateManager.getCurrentMode() == Mode.History){
			serialConnection.writeData(resetDiallingCommand);
			int recentNumberIndex = Integer.parseInt(dialledDigit) - 1;
			String recentNumber = historyManager.getRecentlyDialledNumber(recentNumberIndex);
			if (recentNumber != null) {
				connect(recentNumber);
			} else {
				//If invalid digit is dialled start reading history again.
				historyManager.lastTimeHistoryRead = 0;
			}
		}
	}

	private void handleSetModeCommand(String command) {
		stopSound();
		//Enable history manager after each change of mode
		historyManager.paused = false;
		stateManager.setUnconfirmedMode(command);
		
		boolean isInDefaultMode = stateManager.getCurrentMode() == Mode.None && !stateManager.hasUnconfirmedMode();
		if(isInDefaultMode) {
			playPickupTone(0);
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
			webContentReader.readAvailableContent(RECEIVER_PICKUP_TO_EAR_DELAY);
		} else if(stateManager.hasUnconfirmedMode()){
			//After pickup read confirmation message immediately
			stateManager.lastTimeConfirmationRead = 0;
		} else if(stateManager.getCurrentMode() == Mode.History) {
			//After pickup read history immediately
			historyManager.lastTimeHistoryRead = 0;
			//Enable reading history list after hanging up and picking up
			historyManager.paused = false;
		} else {
			int hangupDuration = millis() - stateManager.lastHangupTime;
			
			if (hangupDuration > RECEIVER_PICKUP_TO_EAR_DELAY || !introMessagePlayed ){
				println("Playing Intro Message...");
				introMessagePlayed = true;
				playIntroMessage();
			} else if (hangupDuration < RECEIVER_PICKUP_TO_EAR_DELAY) {
				playPickupTone(0);
			}
		}
	}

	private void playPickupTone(int delay) {
		if(!stateManager.reiceverPickedUp) return;
		println("Playing pick up tone in " + delay + " ms");
		soundPlayer.playSoundFile("resources/dialtone.wav", true, delay);
	}

	private void callPhone() {
		String outputCommand = "b:1\n";            
		serialConnection.writeData(outputCommand);
		println("Calling phone...");
	}

    private void connect(String rawIPAddress) {
    	if(stateManager.getCurrentMode() != Mode.Incognito)
    		historyManager.addNumber(rawIPAddress);
    	webContentReader.read(rawIPAddress);
    	//Pause history manager when connecting to a website
    	historyManager.paused = true;
    }

    private void stopSound() {
        soundPlayer.stop();
        speechPlayer.stop();
    }

    private void playIntroMessage() {
    	if(!stateManager.reiceverPickedUp) return;
        speechPlayer.say("Welcome to the internet. Dial for websites.", DEFAULT_VOICE, 2000, null);
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
