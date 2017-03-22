import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;


public class WebContentReader {
	private static final int CONTENT_LOADED_MESSAGE_DURATION = 2500;
	private static final String CONTENT_VOICE = "Allison";
	private static final String NARRATOR_VOICE = "Alex";
	private final static int MIN_CONNECTION_DELAY = 8000;
	private static final int MAX_CONNECTION_DELAY = 10000;
	private HTTPReader httpReader;
	private SoundPlayer soundPlayer;
	private SpeechPlayer speechPlayer;
	
	private Timer delayTimer;
	private TimerTask task;
	private String webContent;
	private AtomicBoolean contentAvailable = new AtomicBoolean(false);
	
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
    	loadWebContent(ipAddress);
    	
    	if(delay > 0) {
    		speechPlayer.say("Your page is loading. Please hang up, we'll call you back.");
    		if(task != null) task.cancel();
    		task = new TimerTask() {
    			@Override
    			public void run() {
    				contentAvailable.set(true);
    			}
    		};
    		delayTimer.schedule( task, delay);
    	} else {
    		contentAvailable.set(true);
    	}
    	
	}
	
	public boolean contentAvailableForPlayback() {
		return contentAvailable.get();
	}
	
	public void readAvailableContent(int delay){
		if(!contentAvailable.get()) return;
		contentAvailable.set(false);
		soundPlayer.stop();

		if(webContent == null) {
			soundPlayer.playSoundFile("resources/SIT.wav", true);
		} else {
			speechPlayer.say("Your website has been loaded.", NARRATOR_VOICE, delay);
        	String shortenedContent = webContent.length() > 450 ? webContent.substring(0, 450) : webContent;
        	webContent = null;
            speechPlayer.say(shortenedContent, CONTENT_VOICE, delay + CONTENT_LOADED_MESSAGE_DURATION);
		}
	}

	private void loadWebContent(String ipAddress) {
		String webpageText = httpReader.getWebPageBody(ipAddress);
        
		if (webpageText == null) {
        	System.out.println("Couldn't connect");
        } else {
        	webContent = webpageText;
        }
	}
}