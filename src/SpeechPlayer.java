import java.io.IOException;

public class SpeechPlayer {
	
	private SpeechSynthesis speechSynthesis;

	public SpeechPlayer() {
		speechSynthesis = new SpeechSynthesis();
		speechSynthesis.setWordsPerMinute(175);
		speechSynthesis.blocking(false);
	}
	
	public void say(String content) {
		say(content,"Alex");
	}
	
	public void say(String content, String voice) {
		System.out.println("Reading content: " + content);
        speechSynthesis.say(voice, content);
    }
	
	public void stop(){
		try {
			Runtime.getRuntime().exec("killall say");
		} catch (IOException e) {
			 e.printStackTrace();
        }
	}
}
