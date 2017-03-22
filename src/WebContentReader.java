import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class WebContentReader {
	private final static int MIN_CONNECTION_DELAY = 3000;
	private static final int MAX_CONNECTION_DELAY = 6000;
	private HTTPReader httpReader;
	private SoundPlayer soundPlayer;
	private SpeechPlayer speechPlayer;
	
	private Timer delayTimer;
	private TimerTask task;
	
	public WebContentReader(SoundPlayer soundPlayer, SpeechPlayer speechPlayer) {
		this.soundPlayer = soundPlayer;
		this.speechPlayer = speechPlayer;
		httpReader = new HTTPReader();
		delayTimer = new Timer();
	}

	public void read(String ipAddress){
		Random randomizer = new Random();
		int delay = randomizer.nextBoolean() ? MIN_CONNECTION_DELAY + randomizer.nextInt(MAX_CONNECTION_DELAY - MIN_CONNECTION_DELAY) : 0;
    	
    	System.out.println("Loading " + ipAddress + " in " + delay + "ms.");
    	
    	if(delay > 0) {
    		speechPlayer.say("Your page is loading. Please hang up, we'll call you back.");
    	}
    	
    	if(task != null) task.cancel();
		task = new TimerTask() {
			@Override
			public void run() {
				loadAndReadWebContent(ipAddress);
			}
		};
		delayTimer.schedule( task, delay);
	}

	private void loadAndReadWebContent(String ipAddress) {
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
