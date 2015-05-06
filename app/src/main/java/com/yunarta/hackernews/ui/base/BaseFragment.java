package com.yunarta.hackernews.ui.base;

import android.app.Fragment;
import android.os.Bundle;

/**
 * Created by yunarta on 7/5/15.
 */
public class BaseFragment extends Fragment {

    boolean started;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            started = savedInstanceState.getBoolean(":start");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(":start", started);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!started) {
            started = true;
            onFirstStart();
        }
    }

    public boolean isStarted() {
        return started;
    }

    protected void onFirstStart() {

    }
}
