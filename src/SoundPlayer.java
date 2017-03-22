import java.io.File;

import ddf.minim.AudioPlayer;
import ddf.minim.Minim;
import processing.core.PApplet;

public class SoundPlayer {
	private Minim minim;
    private AudioPlayer tonePlayer;
    
    public SoundPlayer(PApplet applet) {
    	minim = new Minim(applet);
	}
    
    public void playSoundFile(String filePath, boolean loop) {
    	if(tonePlayer != null && tonePlayer.isPlaying()) stop();
    	
    	File audioFile = new File(filePath);
        String audioFilePath = audioFile.getAbsolutePath();
        tonePlayer = minim.loadFile(audioFilePath);
        if(loop) tonePlayer.loop();
    }
    
    public void playSoundFile(String filePath) {    	
        playSoundFile(filePath, false);
    }
    
    public void stop() {
        if(tonePlayer != null && tonePlayer.isPlaying()){
        	tonePlayer.pause();
        	tonePlayer.close();
        }
    }
}
