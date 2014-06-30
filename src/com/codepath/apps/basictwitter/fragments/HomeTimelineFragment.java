package com.codepath.apps.basictwitter.fragments;

import com.loopj.android.http.AsyncHttpResponseHandler;

import android.os.Bundle;

// Shows all tweets in the user's home timeline.
public class HomeTimelineFragment extends TweetsListFragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Load from database if possible.
		save_to_db = true;
	}

	@Override
	protected void getTimeline(
			AsyncHttpResponseHandler handler, long start_id, long max_id) {
		client.getHomeTimeline(
				handler,
				getTweetIDString(start_id),
				getTweetIDString(max_id));
	}
}
