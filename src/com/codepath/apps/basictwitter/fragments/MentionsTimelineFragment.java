package com.codepath.apps.basictwitter.fragments;

import java.util.ArrayList;

import android.os.Bundle;

import com.codepath.apps.basictwitter.helpers.StoredAccountInfo;
import com.codepath.apps.basictwitter.models.Tweet;
import com.codepath.apps.basictwitter.models.User;
import com.loopj.android.http.AsyncHttpResponseHandler;

// Shows all tweets that mention the user.
public class MentionsTimelineFragment extends TweetsListFragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void getTimeline(
			AsyncHttpResponseHandler handler, long start_id, long max_id) {
		client.getMentionsTimeline(
				handler,
				getTweetIDString(start_id),
				getTweetIDString(max_id));
	}

	@Override
	protected ArrayList<Tweet> getTweetsFromDatabaseForTimeline(long start_id,
			long max_id) {
		User current_user = StoredAccountInfo.loadUserInfo(getActivity());
		if (current_user == null) {
			return new ArrayList<Tweet>();
		}
		return Tweet.getTweetsFromDB(
				current_user, Tweet.TweetType.TWEET_TYPE_MENTIONS, start_id, max_id);
	}
}
