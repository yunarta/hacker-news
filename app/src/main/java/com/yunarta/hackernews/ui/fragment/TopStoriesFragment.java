package com.yunarta.hackernews.ui.fragment;

import android.animation.Animator;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.yunarta.hackernews.R;
import com.yunarta.hackernews.api.RestAPIManager;
import com.yunarta.hackernews.api.entity.Story;
import com.yunarta.hackernews.api.entity.TopStories;
import com.yunarta.hackernews.ui.base.BaseFragment;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by yunarta on 7/5/15.
 */
public class TopStoriesFragment extends BaseFragment {

    private Handler mHandler;

    private TopStories mStories;

    private Task<TopStories> mLoadTask;

    private BaseAdapterImpl mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new Handler();
        mLoadTask = RestAPIManager.topStories(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_topstories, null);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mLoadTask.onSuccess(new Continuation<TopStories, Object>() {
            @Override
            public Object then(Task<TopStories> task) throws Exception {
                mStories = task.getResult();
                mAdapter = new BaseAdapterImpl(mStories);

                ListView listView = (ListView) view.findViewById(R.id.list);
                listView.setOnScrollListener(new OnScrollListenerImpl());
                listView.setAdapter(mAdapter);

//                loadStories(listView.getFirstVisiblePosition(), listView.getLastVisiblePosition());
                return null;
            }
        }).continueWith(new Continuation<Object, Object>() {
            @Override
            public Object then(Task<Object> task) throws Exception {
                mLoadTask = null;
                return null;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private class BaseAdapterImpl extends BaseAdapter {

        TopStories mStories;

        public BaseAdapterImpl(TopStories stories) {
            mStories = stories;
        }

        @Override
        public int getCount() {
            return mStories.count();
        }

        @Override
        public Object getItem(int position) {
            Log.d("[hn]", "get item");
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.cell_story, null);
            }

            Story story = mStories.get(position);

            TextView textView;

            textView = (TextView) convertView.findViewById(R.id.number);
            textView.setText("#" + (position + 1));

            TextView title = (TextView) convertView.findViewById(R.id.title);
            TextView domain = (TextView) convertView.findViewById(R.id.domain);
            TextView meta = (TextView) convertView.findViewById(R.id.meta);
            TextView commentNum = (TextView) convertView.findViewById(R.id.comment_num);

            View storyPanel =  convertView.findViewById(R.id.story);
            Log.d("[hn]", "get view, title = " + story.title);
            if (TextUtils.isEmpty(story.title)) {
                title.setText("...");
                domain.setText("");
                meta.setText("");
                commentNum.setText("");

                storyPanel.setVisibility(View.INVISIBLE);
            } else {
                title.setText(story.title);
                domain.setText(story.domain);
                meta.setText(DateUtils.getRelativeTimeSpanString(story.time) + " by " + story.by);
                commentNum.setText(story.descendants);

                storyPanel.setVisibility(View.VISIBLE);

                if (story.state == 2) {
                    story.state = 3;

                    try {
                        Animator anim = ViewAnimationUtils.createCircularReveal(storyPanel, storyPanel.getWidth(), 0, 0, Math.max(storyPanel.getWidth(), storyPanel.getHeight()));
                        anim.start();
                    } catch (Exception e) {
                        // e.printStackTrace();
                    }
                }
            }

            return convertView;
        }
    }

    private class OnScrollListenerImpl implements AbsListView.OnScrollListener {

        private int firstItem = -1;

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            Log.d("[hn]", "scroll = " + scrollState);
//            switch (scrollState) {
//                case SCROLL_STATE_IDLE:
//                case SCROLL_STATE_TOUCH_SCROLL:
//                case SCROLL_STATE_FLING:
//            }
        }

        @Override
        public void onScroll(AbsListView view, final int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            final int last = Math.min(firstVisibleItem + visibleItemCount, totalItemCount);
            Log.d("[hn]", "first = " + firstVisibleItem + ", last = " + last);
            Runnable runnable = new Runnable() {
                public void run() {
//                    if (firstItem != firstVisibleItem && last != 0) {
//                        firstItem = firstVisibleItem;
                        loadStories(firstVisibleItem, last);
//                    }
                }
            };
            mHandler.postDelayed(runnable, 20);
        }
    }

    private void loadStories(int first, int last) {
        for (int i = first; i < last; i++) {
            final Story story = mStories.get(i);
            if (story.state == 0) {
                story.state = 1;

                final int position = i;
                RestAPIManager.loadStory(getActivity(), story).onSuccess(new Continuation<Story, Object>() {
                    @Override
                    public Object then(Task<Story> task) throws Exception {
                        story.update(task.getResult());
                        story.state = 2;
                        if (getView() != null) {
                            ListView listView = (ListView) getView().findViewById(R.id.list);
                            Log.d("[hn]", "position = " + position + " first = " + listView.getFirstVisiblePosition() + " last = " + listView.getLastVisiblePosition());
                            if (listView.getFirstVisiblePosition() <= position && position <= listView.getLastVisiblePosition()) {
                                Log.d("[hn]", "updating view, title = " + story.title);
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                        return null;
                    }
                });
            }
        }
    }
}
