package com.yunarta.hackernews.api.entity;

import android.net.Uri;

import com.google.gson.Gson;
import com.yunarta.hackernews.api.StoryLoaderManager;

import java.io.Reader;
import java.util.ArrayList;

/**
 * Created by yunarta on 7/5/15.
 */
public class Story {

    public int id;

    public boolean deleted;

    public String type;

    public String by;

    public long time;

    public String text;

    public boolean dead;

    public long parent;

    public ArrayList<Integer> kids;

    public String url;

    public int score;

    public String title;

    public ArrayList<String> parts;

    public String descendants;

    public int state;

    public String domain;

    public void update(Story story) {
        this.id = story.id;
        this.deleted = story.deleted;
        this.type = story.type;
        this.by = story.by;
        this.time = story.time;
        this.text = story.text;
        this.dead = story.dead;
        this.parent = story.parent;
        this.kids = story.kids;
        this.url = story.url;
        this.score = story.score;
        this.title = story.title;
        this.parts = story.parts;
        this.descendants = story.descendants;

        this.domain = Uri.parse(url).getHost();
    }

    public Story(int id) {
        this.id = id;
    }

    public static Story make(String json) {
        Gson gson = StoryLoaderManager.instance().getGson();

        return gson.fromJson(json, Story.class);
    }

    public static Story make(Reader reader) {
        Gson gson = StoryLoaderManager.instance().getGson();

        return gson.fromJson(reader, Story.class);
    }
}