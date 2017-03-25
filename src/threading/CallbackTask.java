package threading;

public class CallbackTask<T extends Runnable> implements Runnable {

	private final T task;
	private final Callback<T> callback;

	public CallbackTask(T task, Callback<T> callback) {
		this.task = task;
		this.callback = callback;
	}

	public void run() {
		task.run();
		if(callback != null) callback.onComplete(task);
	}

}