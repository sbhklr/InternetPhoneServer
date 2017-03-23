enum Mode {
	Incognito,
	History,
	Article,
	Developer,
	None
}

public class StateManager {
	public boolean reiceverPickedUp = false;
	public boolean callingPhone = false;
	public int lastHangupTime = 0;
	private Mode unconfirmedMode = null;
	private Mode currentMode = Mode.None;
	private SpeechPlayer speechPlayer;
	
	private long lastTimeConfirmationRead = 0;
	private static final int CONFIRMATION_MESSAGE_INTERVAL = 8000;
	
	public StateManager(SpeechPlayer speechPlayer) {
		this.speechPlayer = speechPlayer;
	}
	
	public void confirmMode(){
		currentMode = unconfirmedMode;
		unconfirmedMode = null;
		speechPlayer.say(modeAsString(currentMode) + " mode confirmed.");
	}
	
	public Mode getCurrentMode() {
		return currentMode;
	}
	
	public boolean hasUnconfirmedMode(){
		return unconfirmedMode != null;
	}
	
	public void setMode(String modeCommand){
		String modeSymbol = modeCommand.substring(2);
		System.out.println("Setting mode: " + modeSymbol);
		
		switch (modeSymbol) {
		case "i":
			unconfirmedMode = Mode.Incognito;
			break;
		case "h":
			unconfirmedMode = Mode.History;
			break;
		case "a":
			unconfirmedMode = Mode.Article;
			break;
		case "d":
			unconfirmedMode = Mode.Developer;
			break;
		case "n":
			unconfirmedMode = Mode.None;
			break;
		default:
			break;
		}
	}
	
	public void readModeConfirmationPrompt() {
		if(System.currentTimeMillis() - lastTimeConfirmationRead < CONFIRMATION_MESSAGE_INTERVAL) return;
		
		lastTimeConfirmationRead  = System.currentTimeMillis();
		speechPlayer.say("Switched to " + modeAsString(unconfirmedMode) + " mode. Dial 1 to confirm.");
	}

	private String modeAsString(Mode mode) {
		String modeAsString;
		
		switch (mode) {
		case Incognito:
			modeAsString = "incognito";
			break;
		case History:
			modeAsString = "history";
			break;
		case Article:
			modeAsString = "article";
			break;
		case Developer:
			modeAsString = "developer";
			break;
		case None:
			modeAsString = "default";
			break;
		default:
			modeAsString = "unknown";
			break;
		}
		return modeAsString;
	}
}
