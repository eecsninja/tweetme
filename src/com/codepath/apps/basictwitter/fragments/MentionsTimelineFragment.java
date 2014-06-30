package com.codepath.apps.basictwitter.fragments;

import com.loopj.android.http.AsyncHttpResponseHandler;

import android.os.Bundle;

// Shows all tweets that mention the user.
public class MentionsTimelineFragment extends TweetsListFragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Do not load from database for now.
		save_to_db = false;
	}

	@Override
	protected void getTimeline(
			AsyncHttpResponseHandler handler, long start_id, long max_id) {
		client.getMentionsTimeline(
				handler,
				getTweetIDString(start_id),
				getTweetIDString(max_id));
	}
}
