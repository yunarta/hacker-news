package com.yunarta.hackernews.api;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;
import com.yunarta.hackernews.api.entity.Story;
import com.yunarta.hackernews.api.entity.TopStories;

import org.apache.http.Header;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import bolts.Task;

/**
 * Created by yunarta on 7/5/15.
 */
public class RestAPIManager {

    public static Task<TopStories> topStories(Context context) {
        final Task<TopStories>.TaskCompletionSource source = Task.create();

        if (true) {
            AsyncHttpClient client = new AsyncHttpClient();
            client.setMaxRetriesAndTimeout(5, 30000);
            client.get(context, "https://hacker-news.firebaseio.com/v0/topstories.json", new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    source.setError(new RestAPIException(throwable));
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    source.setResult(TopStories.make(responseString));
                }
            });
        }

        return source.getTask();
    }

    public static Task<Story> loadStory(Context context, Story story) {
        final Task<Story>.TaskCompletionSource source = Task.create();

        File dir = new File(context.getFilesDir(), "cache");
        final File file = new File(dir, story.id + ".json");
        if (!file.exists() || file.lastModified() + 5000 > System.currentTimeMillis()) {
            StoryLoaderManager.instance().getClient()
                    .get("https://hacker-news.firebaseio.com/v0/item/" + story.id + ".json", new TextHttpResponseHandler() {
                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            source.setError(new RestAPIException(throwable));
                        }

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, String responseString) {
                            try {
                                FileWriter writer = new FileWriter(file);
                                writer.write(responseString);
                                writer.close();

                                source.setResult(Story.make(responseString));
                            } catch (IOException e) {
                                e.printStackTrace();
                                source.setError(new RestAPIException(e));
                            }
                        }
                    });

        } else {
            try {
                source.setResult(Story.make(new FileReader(file)));
            } catch (Exception e) {
                e.printStackTrace();
                source.setError(new RestAPIException(e));
            }
        }

        return source.getTask();
    }
}
