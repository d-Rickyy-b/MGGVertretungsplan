package de.aurora.mggvertretungsplan;

interface AsyncTaskCompleteListener<T> {
	void onTaskComplete(T result);
}
