import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;


public class WebContentReader {
	private static final int CONTENT_LOADED_MESSAGE_DURATION = 2500;
	private static final String DEFAULT_CONTENT_VOICE = "Allison";
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
	private StateManager stateManager;
	
	public WebContentReader(SoundPlayer soundPlayer, SpeechPlayer speechPlayer, StateManager stateManager) {
		this.soundPlayer = soundPlayer;
		this.speechPlayer = speechPlayer;
		this.stateManager = stateManager;
		
		httpReader = new HTTPReader();
		delayTimer = new Timer();
	}

	public void read(String ipAddress){
		Random randomizer = new Random();
		int delay = randomizer.nextBoolean() ? MIN_CONNECTION_DELAY + randomizer.nextInt(MAX_CONNECTION_DELAY - MIN_CONNECTION_DELAY) : 0;
		
    	System.out.println("Loading " + ipAddress + " in " + delay + "ms.");
    	
    	if(delay > 0) {
    		speechPlayer.say("Your page is loading. Please hang up, we'll call you back.");
    		loadWebContent(ipAddress);
    		
    		if(task != null) task.cancel();
    		task = new TimerTask() {
    			@Override
    			public void run() {
    				contentAvailable.set(true);
    			}
    		};
    		delayTimer.schedule( task, delay);
    	} else {
    		loadWebContent(ipAddress);
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
        	String content = getContentFromHTML(webContent);
            speechPlayer.say(content, getContentVoice(webContent), delay + CONTENT_LOADED_MESSAGE_DURATION);
            webContent = null;
		}
	}

	private String getContentFromHTML(String htmlContent) {
		String content;
		if(stateManager.currentMode == Mode.Developer){
			content = htmlContent;
		} else if(stateManager.currentMode == Mode.Article){
			content = httpReader.getArticleContent(htmlContent);
		} else {
			content = httpReader.getWebPageBody(htmlContent);
		}
		
		String shortenedContent = content.length() > 450 ? content.substring(0, 450) : content;
		return shortenedContent;
	}
	

	private String getContentVoice(String html) {
		String voice = null;
		String language = httpReader.getLanguage(html);
		
		if(language == null){
			voice = DEFAULT_CONTENT_VOICE;
		} else if(language.equals("ru")){
			voice = "Yuri";
		} else if(language.equals("de")){
			voice = "Anna";
		} else {
			voice = DEFAULT_CONTENT_VOICE;
		}
		
		return stateManager.currentMode == Mode.Incognito ? "Whisper" : voice;
	}

	private void loadWebContent(String ipAddress) {
		String htmlContent = httpReader.getWebPageHTML(ipAddress);
        
		if (htmlContent == null) {
        	System.out.println("Couldn't connect");
        } else {
        	webContent = htmlContent;
        }
	}
}
