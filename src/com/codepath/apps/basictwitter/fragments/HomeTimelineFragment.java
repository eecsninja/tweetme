package com.codepath.apps.basictwitter.fragments;

import java.util.ArrayList;

import org.json.JSONArray;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.codepath.apps.basictwitter.TwitterApp;
import com.codepath.apps.basictwitter.TwitterClient;
import com.codepath.apps.basictwitter.activities.ComposeActivity;
import com.codepath.apps.basictwitter.activities.TweetViewActivity;
import com.codepath.apps.basictwitter.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

public class HomeTimelineFragment extends TweetsListFragment {
	protected TwitterClient client;

	// Custom JSON response handler.
	protected class JSONHandler extends JsonHttpResponseHandler {
		private boolean is_refreshing = false;

		public JSONHandler(boolean do_refresh) {
			super();
			is_refreshing = do_refresh;
		}
		@Override
		public void onSuccess(JSONArray array) {
			ArrayList<Tweet> new_tweets = Tweet.fromJSONArray(array);
			// Add these to the TimelineActivity's collection of tweets.
			addTweets(new_tweets);
			if (is_refreshing) {
				tweets_view.onRefreshComplete();
			}
			// Store newly retrieved tweets in database.
			saveTweetsToDB(new_tweets);
		}

		@Override
		public void onFailure(Throwable e, String s) {
			Log.d("DEBUG", e.toString());
			Log.d("DEBUG", s.toString());
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Load REST client.
		client = TwitterApp.getRestClient();
	}

	// Launch a new activity to compose a new tweet.
	public void doCompose(MenuItem item) {
		Intent intent = new Intent(getActivity(), ComposeActivity.class);
		startActivityForResult(intent, ComposeActivity.COMPOSE_INTENT);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK) {
			return;
		}
		// A new tweet could be returned by either ComposeActivity or by
		// TweetViewActivity (indirectly).
		boolean do_get_tweet_result =
				(requestCode == ComposeActivity.COMPOSE_INTENT) ||
				(requestCode == TweetViewActivity.TWEET_VIEW_CODE &&
				 data.getExtras().containsKey(ComposeActivity.INTENT_RESPONSE_TWEET));
		if (do_get_tweet_result) {
			// Get the newly posted tweet and add it to the timeline.
			Log.d("DEBUG", "Got activity result");
			Tweet tweet =
					(Tweet) data.getExtras()
							.getSerializable(ComposeActivity.INTENT_RESPONSE_TWEET);
			Log.d("DEBUG", "Got tweet back");
			if (tweet.getId() > newest_id) {
				newest_id = tweet.getId();
			}
			// TODO: This does not take into account the possibility of other
			// new tweets having been added to home timeline during the time it
			// took to compose.
			tweets_adapter.insert(tweet, 0);
		}
	}

	@Override
	protected void populateTimeline() {
		// Determine a start ID for getting tweets. If there are no existing
		// tweets loaded, then load as many as possible. Otherwise, look for
		// the lowest ID and start below that.
		long max_id = 0;
		if (!tweets.isEmpty()) {
			max_id = oldest_id - 1;
		}
		// Get the timeline from Twitter.
		loadTweets(false, 1, max_id);
	}

	@Override
	protected void loadNewTweets() {
		// Get the tweets that are newer than the newest tweet that we've
		// already retrieved.
		loadTweets(true, newest_id, 0);

		// If not connected, then reset the refresh UI and bail out.
		if (!hasInternetConnectivity()) {
			tweets_view.onRefreshComplete();
		}
	}

	// Launches a TweetViewActivity to view a single tweet.
	@Override
	protected void launchTweetView(Tweet tweet) {
		Intent intent = new Intent(getActivity(), TweetViewActivity.class);
		intent.putExtra(TweetViewActivity.INTENT_TWEET_VIEW, tweet);
		startActivityForResult(intent, TweetViewActivity.TWEET_VIEW_CODE);
	}

	// Loads tweets within an ID range.
	protected void loadTweets(boolean refresh, long start_id, long max_id) {
		boolean has_internet = hasInternetConnectivity();
		// Warn user about lack of internet, if applicable.
		if (!has_internet) {
			warnOfLackOfInternetConnectivity();
		}
		// If there are tweets cached in the database, load those.
		ArrayList<Tweet> db_tweets =
				Tweet.getTweetsFromDB(null, start_id, max_id);
		if (!db_tweets.isEmpty()) {
			addTweets(db_tweets);
			return;
		}
		if (has_internet) {
			// Otherwise, load from Twitter API if connected.
			client.getHomeTimeline(
					new JSONHandler(refresh),
					getTweetIDString(start_id),
					getTweetIDString(max_id));
		}
	}
}
