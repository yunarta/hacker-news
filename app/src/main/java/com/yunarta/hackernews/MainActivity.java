package com.yunarta.hackernews;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;

import com.yunarta.hackernews.ui.fragment.TopStoriesFragment;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container, new TopStoriesFragment(), "main").commit();
//            ft.replace(R.id.fragment_container, new StoryCommentsFragment(), "main").commit();
        }
    }
}
