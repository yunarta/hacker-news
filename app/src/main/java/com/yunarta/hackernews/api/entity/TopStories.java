package com.yunarta.hackernews.api.entity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

/**
 * Created by yunarta on 7/5/15.
 */
public class TopStories {

    ArrayList<Story> stories;

    public TopStories(int length) {
        stories = new ArrayList<>(length);
    }

    public static TopStories make(String json) {
        Gson gson = new GsonBuilder().create();

        int[] stories = gson.fromJson(json, int[].class);
        TopStories topStories = new TopStories(stories.length);
        for (int story : stories) {
            topStories.add(new Story(story));
        }

        return topStories;
    }

    public void add(Story story) {
        stories.add(story);
    }

    public int count() {
        return stories.size();
    }

    public Story get(int i) {
        return stories.get(i);
    }
}
