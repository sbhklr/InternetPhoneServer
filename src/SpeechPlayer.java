import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class SpeechPlayer {
	
	private SpeechSynthesis speechSynthesis;
	private ArrayList<Process> sayProcesses;
	private Timer delayTimer;

	public SpeechPlayer() {
		speechSynthesis = new SpeechSynthesis();
		speechSynthesis.setWordsPerMinute(195);
		speechSynthesis.blocking(false);
		delayTimer = new Timer();
		sayProcesses = new ArrayList<>();
	}
	
	public void say(String content) {
		say(content,"Alex");
	}
	
	public void say(String content, String voice, int delay) {
		if(delay == 0){
			say(content, voice);
			return;
		}
		
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				say(content, voice);
			}
		};
		delayTimer.schedule( task, delay);
    }
	
	public void say(String content, String voice){
		synchronized (speechSynthesis) {
			System.out.println("Reading content: " + content);
			Process sayProcess = speechSynthesis.say(voice, content);
			sayProcesses.add(sayProcess);
		}
	}
	
	public void stop(){
		delayTimer.cancel();
		delayTimer = new Timer();
		ArrayList<Process> deadProcesses = new ArrayList<>();
		
		for (Process process : sayProcesses) {
			if(process.isAlive()){
				process.destroy();
				deadProcesses.add(process);
			}
		}
		sayProcesses.removeAll(deadProcesses);
	}
}
