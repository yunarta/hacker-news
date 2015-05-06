package com.yunarta.hackernews;

import android.app.Application;

import java.io.File;
import java.io.FileFilter;

/**
 * Created by yunarta on 7/5/15.
 */
public class HackerNewsApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        File dir = new File(getFilesDir(), "cache");
        dir.mkdirs();

        File[] files = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.lastModified() + 5000 > System.currentTimeMillis();
            }
        });
        for (File file : files) {
            file.delete();
        }
    }
}
