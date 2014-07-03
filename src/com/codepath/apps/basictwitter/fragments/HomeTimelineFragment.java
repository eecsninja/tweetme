package com.codepath.apps.basictwitter.fragments;

import java.util.ArrayList;

import com.codepath.apps.basictwitter.models.Tweet;
import com.loopj.android.http.AsyncHttpResponseHandler;

import android.os.Bundle;

// Shows all tweets in the user's home timeline.
public class HomeTimelineFragment extends TweetsListFragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void getTimeline(
			AsyncHttpResponseHandler handler, long start_id, long max_id) {
		client.getHomeTimeline(
				handler,
				getTweetIDString(start_id),
				getTweetIDString(max_id));
	}

	@Override
	protected ArrayList<Tweet> getTweetsFromDatabaseForTimeline(long start_id,
			long max_id) {
		return Tweet.getTweetsFromDB(
				null, Tweet.TweetType.TWEET_TYPE_ALL, start_id, max_id);
	}
}
