package de.aurora.mggvertretungsplan;

public interface AsyncTaskCompleteListener<T> {
	public void onTaskComplete(T result);
}
