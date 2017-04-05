package net.czaarek99.spotifyreorder.util;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Czarek on 2017-03-26.
 */

public abstract class CallbackGroup<T> {

    private final Set<Callback<T>> callbacks = new HashSet<>();
    private final Set<Callback<T>> pendingCallbacks = new HashSet<>();
    private final Set<Callback<T>> failedCallbacks = new HashSet<>();
    private final Set<Callback<T>> successfullCallbacks = new HashSet<>();
    private CallbackState state = CallbackState.WAITING;
    private int retries = 0;

    public void executeCallbacks(){
        if(state == CallbackState.WAITING){
            pendingCallbacks.addAll(callbacks);

            for (Callback<T> callback : pendingCallbacks) {
                state = CallbackState.IN_PROGRESS;
                callbackExecution(callback);

            }
        } else {
            throw new RuntimeException("CallbackGroup is already in progress!");
        }
    }

    public void retry(RetryType type){
        retries++;

        if(state == CallbackState.FINISHED){
            if(type == RetryType.ALL){
                successfullCallbacks.clear();
                failedCallbacks.clear();
                state = CallbackState.WAITING;
                executeCallbacks();
            } else if(type == RetryType.FAILED){
                pendingCallbacks.addAll(failedCallbacks);
                failedCallbacks.clear();

                for (Callback<T> callback : pendingCallbacks) {
                    state = CallbackState.IN_PROGRESS;
                    callbackExecution(callback);
                }
            }
        } else {
            throw new RuntimeException("CallbackGroup hasn't finished yet!");
        }
    }

    public void addCallback(final Callback<T> callback){
        if(state == CallbackState.WAITING){
            Callback<T> callbackWrapper = new Callback<T>() {
                @Override
                public void success(T t, Response response) {
                    successfullCallbacks.add(this);
                    callback.success(t, response);
                    onCallbackFinish(this);
                }

                @Override
                public void failure(RetrofitError error) {
                    failedCallbacks.add(this);
                    callback.failure(error);
                    onCallbackFinish(this);
                }
            };

            callbacks.add(callbackWrapper);
        } else {
            throw new RuntimeException("CallbackGroup is already in progress!");
        }

    }

    public int getRetries() {
        return retries;
    }

    public boolean hasFailedCallbacks(){
        return !failedCallbacks.isEmpty();
    }

    public int getSuccessfulCallbacks(){
        return successfullCallbacks.size();
    }

    public int getSize(){
        return callbacks.size();
    }

    public CallbackState getState() {
        return state;
    }

    private void onCallbackFinish(Callback<T> callback){
        pendingCallbacks.remove(callback);

        if(pendingCallbacks.isEmpty()){
            state = CallbackState.FINISHED;
            onAllFinished();
        }
    }

    public abstract void onAllFinished();

    public abstract void callbackExecution(Callback<T> callback);

    public enum RetryType {
        ALL, FAILED
    }

    public enum CallbackState {
        WAITING, IN_PROGRESS, FINISHED
    }
}



