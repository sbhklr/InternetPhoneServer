package sound;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import ddf.minim.AudioPlayer;
import ddf.minim.Minim;
import processing.core.PApplet;

public class SoundPlayer {
	private Minim minim;
    private AudioPlayer tonePlayer;
    private Timer delayTimer;
    private HashMap<String, File> tempFiles = new HashMap<>();
    
    public SoundPlayer(PApplet applet) {
    	minim = new Minim(applet);
    	delayTimer = new Timer();
	}
    
    public void playSoundFile(String filePath, boolean loop, int delay) {
    	
    	File file = getFileFromPath(filePath);		
		
		if(delay == 0){
			startSound(file, loop);
			return;
		}
		
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				startSound(file, loop);
			}
		};
		delayTimer.schedule( task, delay);
    }

	private File getFileFromPath(String resourcePath){
		File soundFile = tempFiles.get(resourcePath);
		if(soundFile != null) {
			System.out.println("Reading file: " + soundFile.getAbsolutePath());
			return soundFile;
		}
		
	    URL resourceURL = getClass().getResource(resourcePath);
	    
	    if (resourceURL.toString().startsWith("rsrc:")) {
	    	System.out.println("Creating temp file for: " + resourcePath);
	        try {
	            InputStream input = getClass().getResourceAsStream(resourcePath);	            
	            soundFile = File.createTempFile("tempfile", ".wav");
	            OutputStream out = new FileOutputStream(soundFile);
	            int read;
	            byte[] bytes = new byte[1024];

	            while ((read = input.read(bytes)) != -1) {
	                out.write(bytes, 0, read);
	            }
	            out.close();
	            soundFile.deleteOnExit();
	        } catch (IOException ex) {
	           ex.printStackTrace();
	        }
	    } else {
	        soundFile = new File(resourceURL.getFile());
	    }

	    if (soundFile != null && !soundFile.exists()) {
	        throw new RuntimeException("Error: File " + soundFile + " not found!");
	    }
	    
	    tempFiles.put(resourcePath, soundFile);
	    return soundFile;
	}

	private void startSound(File audioFile, boolean loop) {
		if(tonePlayer != null && tonePlayer.isPlaying()) stop();    	
        String audioFilePath = audioFile.getAbsolutePath();
        //minim.loadFileStream(arg0, arg1, arg2)
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
