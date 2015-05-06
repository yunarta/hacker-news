package com.yunarta.hackernews.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;

import java.util.concurrent.Executors;

/**
 * Created by yunarta on 7/5/15.
 */
public class StoryLoaderManager {

    private static StoryLoaderManager mInstance;

    public static StoryLoaderManager instance() {
        if (mInstance == null) {
            mInstance = new StoryLoaderManager();
        }

        return mInstance;
    }

    protected AsyncHttpClient mClient;

    protected Gson mGson;

    protected StoryLoaderManager() {
        mClient = new AsyncHttpClient();
        mClient.setThreadPool(Executors.newScheduledThreadPool(10));

        mGson = new GsonBuilder().create();
    }

    public AsyncHttpClient getClient() {
        return mClient;
    }

    public Gson getGson() {
        return mGson;
    }
}
