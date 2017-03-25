package sound;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import ddf.minim.AudioPlayer;
import ddf.minim.Minim;
import processing.core.PApplet;

public class SoundPlayer {
	private Minim minim;
    private AudioPlayer tonePlayer;
    private Timer delayTimer;
    
    public SoundPlayer(PApplet applet) {
    	minim = new Minim(applet);
    	delayTimer = new Timer();
	}
    
    public void playSoundFile(String filePath, boolean loop, int delay) {
    	
    	if(delay == 0){
			startSound(filePath, loop);
			return;
		}
		
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				startSound(filePath, loop);
			}
		};
		delayTimer.schedule( task, delay);
    }

	private void startSound(String filePath, boolean loop) {
		if(tonePlayer != null && tonePlayer.isPlaying()) stop();
    	File audioFile = new File(filePath);
        String audioFilePath = audioFile.getAbsolutePath();
        tonePlayer = minim.loadFile(audioFilePath);
        if(loop) tonePlayer.loop();
	}
    
    public void playSoundFile(String filePath, int delay) {    	
        playSoundFile(filePath, false, delay);
    }
    
    public void stop() {
        if(tonePlayer != null && tonePlayer.isPlaying()){
        	tonePlayer.pause();
        	tonePlayer.close();
        }
    }
}
