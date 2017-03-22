import java.util.Random;


public class WebContentReader {
	private static final int MAX_CONNECTION_DELAY = 5000;
	private HTTPReader httpReader;
	private SoundPlayer soundPlayer;
	private SpeechPlayer speechPlayer;
	
	public WebContentReader(SoundPlayer soundPlayer, SpeechPlayer speechPlayer) {
		this.soundPlayer = soundPlayer;
		this.speechPlayer = speechPlayer;
		httpReader = new HTTPReader();
	}

	public void read(String ipAddress){
		Random randomizer = new Random();
    	int delay = randomizer.nextBoolean() ? randomizer.nextInt(MAX_CONNECTION_DELAY) : 0;
    	
        String webpageText = httpReader.getWebPageBody(ipAddress);
        
        if (webpageText != null) {
        	soundPlayer.stop();
        	String shortenedContent = webpageText.substring(0, 450);
            speechPlayer.say(shortenedContent, "Alex");
        } else {
            soundPlayer.playSoundFile("resources/SIT.wav", true);
            System.out.println("Couldn't connect");
        }
	}
}
