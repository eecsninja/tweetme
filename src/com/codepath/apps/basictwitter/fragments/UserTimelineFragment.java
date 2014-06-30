package com.codepath.apps.basictwitter.fragments;

import com.loopj.android.http.AsyncHttpResponseHandler;

import android.os.Bundle;

// Shows all tweets from the user.
public class UserTimelineFragment extends TweetsListFragment {
	// Screen name of user whose timeline to lookup.
	// null means the current user.
	String screen_name = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Do not load from database for now.
		save_to_db = false;
	}

	// Accessor to specify a particular user's screen name.
	public void setScreenName(String screen_name) {
		this.screen_name = screen_name;
	}

	@Override
	protected void getTimeline(
			AsyncHttpResponseHandler handler, long start_id, long max_id) {
		client.getUserTimeline(
				handler,
				screen_name,
				getTweetIDString(start_id),
				getTweetIDString(max_id));
	}
}
