package threading;

public interface Callback<T extends Runnable> {
	public void onComplete(T task);
}
