package threading;

import sound.SpeechSynthesis;

public class SayTask implements Runnable {

	private static final int WORDS_PER_MINUTE = 185;
	private SpeechSynthesis speechSynthesis;
	private String content;
	private String voice;
	private Process process;
	
	public SayTask(String content, String voice) {
		this.content = content;
		this.voice = voice;
		speechSynthesis = new SpeechSynthesis();
		speechSynthesis.setWordsPerMinute(WORDS_PER_MINUTE);
		speechSynthesis.blocking(false);
	}
	
	@Override
	public void run() {
		try {
			process = speechSynthesis.say(voice, content);
			process.waitFor();
		} catch (InterruptedException e) {
			if(process.isAlive()) process.destroy();
			Thread.currentThread().interrupt();
		}
	}
}
