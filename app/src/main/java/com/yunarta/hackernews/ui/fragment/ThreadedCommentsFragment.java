package com.yunarta.hackernews.ui.fragment;

import android.animation.Animator;
import android.app.FragmentTransaction;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mobilesolutionworks.android.util.ViewUtils;
import com.yunarta.hackernews.BuildConfig;
import com.yunarta.hackernews.R;
import com.yunarta.hackernews.api.RestAPIManager;
import com.yunarta.hackernews.api.entity.Story;
import com.yunarta.hackernews.ui.base.BaseFragment;

import java.util.ArrayList;
import java.util.Arrays;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by yunarta on 7/5/15.
 */
public class ThreadedCommentsFragment extends BaseFragment {

    private String mTransitionName;

    private Story mStory;

    private int mPosition;

    private Handler mHandler;

    private BaseAdapterImpl mAdapter;

    private View mHeader;

    private Task<Story> mLoadTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new Handler();

        Bundle args = getArguments();
        if (args != null) {
            mStory = (Story) args.getSerializable("story");
            mPosition = args.getInt("position");
            mTransitionName = args.getString("transitionName");
        } else if (BuildConfig.DEBUG) {
            mStory = new Story(9504939);
            mStory.title = "NSA phone surveillance not authorized: U.S. appeals court";
            mStory.domain = "www.google.com";
            mStory.time = 1431006128;
            mStory.by = "me";
            mStory.descendants = "20";
            mStory.kids = new ArrayList<Integer>(Arrays.asList(9505349, 9505265, 9505178, 9505337, 9505210));
        }

        ArrayList<Story> comments = new ArrayList<Story>(mStory.kids.size());
        for (Integer kid : mStory.kids) {
            comments.add(new Story(kid));
        }
        mStory.comments = comments;

        mLoadTask = Task.forResult(mStory);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mHeader = inflater.inflate(R.layout.cell_commentheader, null);
        return inflater.inflate(R.layout.fragment_storycomments, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mLoadTask = RestAPIManager.loadStory(getActivity(), mStory);
                mLoadTask.onSuccess(new Continuation<Story, Story>() {
                    @Override
                    public Story then(Task<Story> task) throws Exception {
                        ArrayList<Story> comments = new ArrayList<Story>(mStory.kids.size());
                        for (Integer kid : mStory.kids) {
                            comments.add(new Story(kid));
                        }

                        mStory.comments = comments;
                        return mStory;
                    }
                }).onSuccess(new UpdateStory()).
                        continueWith(new Continuation<Object, Object>() {
                            @Override
                            public Object then(Task<Object> task) throws Exception {
                                SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipe_refresh_layout);
                                refreshLayout.setRefreshing(false);

                                return null;
                            }
                        });
            }
        });

        View header = mHeader;
        header.setTransitionName(mTransitionName);

        ListView listView = (ListView) view.findViewById(R.id.list);
        listView.addHeaderView(mHeader, null, false);
        listView.setOnScrollListener(new OnScrollListenerImpl());
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Story story = mStory.comments.get(position - 1);

                Resources resources = getResources();
                view.setBackgroundColor(resources.getColor(android.R.color.white));
                view.setElevation(resources.getDimension(R.dimen.card_elevation));

                String transitionName = "comment-" + story.id;

                TransitionInflater ti = TransitionInflater.from(getActivity());

                setSharedElementReturnTransition(ti.inflateTransition(R.transition.tr_read_story));
                setExitTransition(ti.inflateTransition(android.R.transition.explode));

                Bundle args = new Bundle();
                args.putString("transitionName", transitionName);
                args.putInt("position", position - 1);

                args.putSerializable("story", story);

                ThreadedCommentsFragment fragment = new ThreadedCommentsFragment();
                fragment.setArguments(args);

                fragment.setSharedElementEnterTransition(ti.inflateTransition(R.transition.tr_read_story));

                fragment.setEnterTransition(ti.inflateTransition(android.R.transition.explode));

                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.addToBackStack("top");
                ft.addSharedElement(view, transitionName);
                ft.replace(R.id.fragment_container, fragment);
                ft.commit();
            }
        });

        mLoadTask.onSuccess(new UpdateStory());
    }

    private class BaseAdapterImpl extends BaseAdapter {

        Story mStory;

        public BaseAdapterImpl(Story story) {
            mStory = story;
        }

        @Override
        public int getCount() {
            return mStory.comments.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.cell_comment, null);
            }

            Story story = mStory.comments.get(position);
            convertView.setTransitionName("comment-" + story.id);

            ViewUtils.vuSetText(convertView, "#" + (position + 1), R.id.number);
            if (TextUtils.isEmpty(story.text)) {
                ViewUtils.vuSetText(convertView, "...", R.id.text).setMaxLines(1);
                ViewUtils.vuSetText(convertView, "", R.id.meta);
                ViewUtils.vuSetText(convertView, "", R.id.comment_num);
                ViewUtils.vuSetVisibility(convertView, View.GONE, R.id.read_more);
                ViewUtils.vuSetVisibility(convertView, View.INVISIBLE, R.id.story);
            } else {
                TextView text = (TextView) convertView.findViewById(R.id.text);
                text.setMaxLines(Integer.MAX_VALUE);
                String commentText = Html.fromHtml(story.text).toString();

                text.setText(commentText);

                int maxLines = text.getLineCount();

                text.setMaxLines(4);
                text.setText(commentText);

                if (maxLines > 4) {
                    text.setMaxLines(4);

                    ViewUtils.vuSetText(convertView, "Read more (" + (maxLines - 4) + " lines)", R.id.read_more).setVisibility(View.VISIBLE);
                } else {
                    ViewUtils.vuSetVisibility(convertView, View.GONE, R.id.read_more);
                }

                ViewUtils.vuSetText(convertView, DateUtils.getRelativeTimeSpanString(story.time * 1000) + " by " + story.by, R.id.meta);
                ViewUtils.vuSetText(convertView, String.valueOf(story.kids.size()), R.id.comment_num);
                ViewUtils.vuSetVisibility(convertView, View.VISIBLE, R.id.story);

                if (story.state == 2) {
                    story.state = 3;

                    try {
                        View storyPanel = convertView.findViewById(R.id.story);
                        Animator anim = ViewAnimationUtils.createCircularReveal(storyPanel, storyPanel.getWidth(), 0, 0, Math.max(storyPanel.getWidth(), storyPanel.getHeight()));
                        anim.start();
                    } catch (Exception e) {
                    }
                }
            }

            return convertView;
        }
    }

    private class OnScrollListenerImpl implements AbsListView.OnScrollListener {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

        @Override
        public void onScroll(AbsListView view, final int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            final int last = Math.min(firstVisibleItem + visibleItemCount, totalItemCount);
            Runnable runnable = new Runnable() {
                public void run() {
                    loadStories(firstVisibleItem, last);
                }
            };
            mHandler.postDelayed(runnable, 20);
        }
    }

    private void loadStories(int first, int last) {
        for (int i = first; i < last; i++) {
            if (i == 0) continue;

            final Story story = mStory.comments.get(i - 1);
            if (story.state == 0) {
                story.state = 1;

                final int position = i;
                RestAPIManager.loadStory(getActivity(), story).onSuccess(new Continuation<Story, Object>() {
                    @Override
                    public Object then(Task<Story> task) throws Exception {
                        story.update(task.getResult());
                        story.state = 2;
                        ListView listView = (ListView) getView().findViewById(R.id.list);
                        if (listView.getFirstVisiblePosition() <= position && position <= listView.getLastVisiblePosition()) {
                            mAdapter.notifyDataSetChanged();
                        }
                        return null;
                    }
                });
            }
        }
    }

    private class UpdateStory implements Continuation<Story, Object> {

        @Override
        public Object then(Task<Story> task) throws Exception {
            ViewUtils.vuSetText(mHeader, "#" + (mPosition + 1), R.id.number);
            ViewUtils.vuSetText(mHeader, DateUtils.getRelativeTimeSpanString(mStory.time * 1000) + " by " + mStory.by, R.id.meta);
            ViewUtils.vuSetText(mHeader, String.valueOf(mStory.kids.size()), R.id.comment_num);

            TextView text = (TextView) mHeader.findViewById(R.id.text);
            text.setText(Html.fromHtml(mStory.text));

            mAdapter = new BaseAdapterImpl(mStory);

            ListView listView = (ListView) getView().findViewById(R.id.list);
            listView.setAdapter(mAdapter);

            return null;
        }
    }
}
