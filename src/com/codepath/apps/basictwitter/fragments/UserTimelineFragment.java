package com.codepath.apps.basictwitter.fragments;

import java.util.ArrayList;

import android.os.Bundle;

import com.codepath.apps.basictwitter.helpers.StoredAccountInfo;
import com.codepath.apps.basictwitter.models.Tweet;
import com.codepath.apps.basictwitter.models.User;
import com.loopj.android.http.AsyncHttpResponseHandler;

// Shows all tweets from the user.
public class UserTimelineFragment extends TweetsListFragment {
	// Screen name of user whose timeline to lookup.
	// null means the current user.
	String screen_name = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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

	@Override
	protected ArrayList<Tweet> getTweetsFromDatabaseForTimeline(long start_id,
			long max_id) {
		User user = null;
		// If screen_name == null, then this is attempting to load the current
		// user's info.
		if (screen_name != null) {
			user = User.getUserByScreenName(screen_name);
		} else {
			user = StoredAccountInfo.loadUserInfo(getActivity());
		}
		// If there is no current account info stored yet, then
		// don't return anything.
		if (user == null) {
			return new ArrayList<Tweet>();
		}
		return Tweet.getTweetsFromDB(
				user, Tweet.TweetType.TWEET_TYPE_USER, start_id, max_id);
	}
}
