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
}
