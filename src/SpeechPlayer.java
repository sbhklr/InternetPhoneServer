import java.util.Timer;
import java.util.TimerTask;

public class SpeechPlayer {
	
	private SpeechSynthesis speechSynthesis;
	private Process sayProcess;
	private Timer delayTimer;
	private TimerTask task;

	public SpeechPlayer() {
		speechSynthesis = new SpeechSynthesis();
		speechSynthesis.setWordsPerMinute(175);
		speechSynthesis.blocking(false);
		delayTimer = new Timer();
	}
	
	public void say(String content) {
		say(content,"Alex");
	}
	
	public void say(String content, String voice, int delay) {
		if(task != null) task.cancel();
		task = new TimerTask() {
			@Override
			public void run() {
				say(content, voice);
			}
		};
		delayTimer.schedule( task, delay);
    }
	
	public void say(String content, String voice){
		synchronized (speechSynthesis) {
			stop();
			System.out.println("Reading content: " + content);
			sayProcess = speechSynthesis.say(voice, content);
		}
	}
	
	public void stop(){
		delayTimer.cancel();
		delayTimer = new Timer();
		
		if(sayProcess == null || !sayProcess.isAlive()) return;
		sayProcess.destroy();
	}
}
