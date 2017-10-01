package de.aurora.mggvertretungsplan;

public interface AsyncTaskCompleteListener<T> {
    void onTaskComplete(T result);
}
