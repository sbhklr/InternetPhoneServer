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
	public Mode currentMode = Mode.Article;
	private SpeechPlayer speechPlayer;
	
	public StateManager(SpeechPlayer speechPlayer) {
		this.speechPlayer = speechPlayer;
	}
	
	public void setMode(String modeCommand){
		String modeSymbol = modeCommand.substring(2);
		System.out.println("Setting mode: " + modeSymbol);
		
		switch (modeSymbol) {
		case "i":
			currentMode = Mode.Incognito;
			break;
		case "h":
			currentMode = Mode.History;
			break;
		case "a":
			currentMode = Mode.Article;
			break;
		case "d":
			currentMode = Mode.Developer;
			break;
		case "n":
			currentMode = Mode.None;
			break;
		default:
			break;
		}
	}
	
	public void readCurrentMode(){
		String mode;
		
		switch (currentMode) {
		case Incognito:
			mode = "incognito";
			break;
		case History:
			mode = "history";
			break;
		case Article:
			mode = "article";
			break;
		case Developer:
			mode = "developer";
			break;
		case None:
			mode = "default";
			break;
		default:
			mode = "unknown";
			break;
		}
		
		speechPlayer.say("Switched to " + mode + " mode.");
	}
}
