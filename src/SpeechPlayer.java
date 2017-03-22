public class SpeechPlayer {
	
	private SpeechSynthesis speechSynthesis;
	private Process sayProcess;

	public SpeechPlayer() {
		speechSynthesis = new SpeechSynthesis();
		speechSynthesis.setWordsPerMinute(175);
		speechSynthesis.blocking(false);
	}
	
	public void say(String content) {
		say(content,"Alex");
	}
	
	public void say(String content, String voice) {
		stop();
		System.out.println("Reading content: " + content);
        sayProcess = speechSynthesis.say(voice, content);
    }
	
	public void stop(){
		if(sayProcess == null || !sayProcess.isAlive()) return;
		sayProcess.destroy();
	}
}
