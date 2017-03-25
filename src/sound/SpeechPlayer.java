package sound;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import threading.Callback;
import threading.CallbackTask;
import threading.SayTask;

public class SpeechPlayer {
	
	private static final String DEFAULT_VOICE = "Alex";
	private ArrayList<Future<SayTask>> sayFutures;
	private Timer delayTimer;
	private ExecutorService pool;

	public SpeechPlayer() {
		pool = Executors.newCachedThreadPool(); 
		delayTimer = new Timer();
		sayFutures = new ArrayList<>();
	}
	
	public void say(String content, Callback<SayTask> onComplete) {
		say(content,DEFAULT_VOICE, onComplete);
	}
	
	public void say(String content, String voice, int delay, Callback<SayTask> onComplete) {
		if(delay == 0){
			say(content, voice, onComplete);
			return;
		}
		
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				say(content, voice, onComplete);
			}
		};
		delayTimer.schedule( task, delay);
    }
	
	@SuppressWarnings("unchecked")
	public void say(String content, String voice, Callback<SayTask> onComplete){
		System.out.println("Reading content: " + content);
		Future<?> task = pool.submit(new CallbackTask<SayTask>(new SayTask(content, voice), onComplete));
		sayFutures.add((Future<SayTask>) task);
	}
	
	public void stop(){
		delayTimer.cancel();
		delayTimer = new Timer();
		
		ArrayList<Future<SayTask>> cancelledFutures = new ArrayList<>();
		
		for (Future<SayTask> task : sayFutures) {
			if(!task.isDone() && !task.isCancelled()){
				task.cancel(true);
				cancelledFutures.add(task);
			}
		}
		sayFutures.removeAll(cancelledFutures);
	}
}
