package com.codepath.apps.basictwitter;

import java.util.ArrayList;

import org.json.JSONArray;

import com.codepath.apps.basictwitter.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

public class TimelineActivity extends Activity {
	private TwitterClient client;
	private ArrayList<Tweet> tweets;
	private TweetArrayAdapter tweets_adapter;
	private ListView tweets_view;

	// Store the oldest and newest tweet IDs.
	long oldest_id = Long.MAX_VALUE;
	long newest_id = 1;

	// ComposeActivity request code.
	static final int COMPOSE_INTENT = 888;
	public static final String INTENT_RESPONSE_TWEET = "response";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timeline);

		// Set up to display tweets.
		tweets_view = (ListView) findViewById(R.id.lvTweets);
		tweets = new ArrayList<Tweet>();
		tweets_adapter = new TweetArrayAdapter(this, tweets);
		tweets_view.setAdapter(tweets_adapter);

		// Load some tweets.
		client = TwitterApp.getRestClient();
		populateTimeline();

		// Add scroll listener.
		tweets_view.setOnScrollListener(new EndlessScrollListener() {
			@Override
			public void onLoadMore(int page, int totalItemsCount) {
				populateTimeline();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Set up menu.
		getMenuInflater().inflate(R.menu.compose, menu);
		return true;
	}

	public void populateTimeline() {
		// Determine a start ID for getting tweets. If there are no existing
		// tweets loaded, then load as many as possible. Otherwise, look for
		// the lowest ID and start below that.
		String start_id = "1";	// TODO: Use this for loading newer tweets.
		String max_id = null;
		if (!tweets.isEmpty()) {
			max_id = Long.toString(oldest_id - 1);
		}
		// Define a custom handler.
		client.getHomeTimeline(new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(JSONArray array) {
				ArrayList<Tweet> new_tweets = Tweet.fromJSONArray(array);
				// Determine newest and oldest tweet IDs.
				setMinAndMaxIDs(new_tweets);
				// Add new tweets to views.
				tweets_adapter.addAll(new_tweets);
			}

			@Override
			public void onFailure(Throwable e, String s) {
				Log.d("DEBUG", e.toString());
				Log.d("DEBUG", s.toString());
			}
		}, start_id, max_id);
	}

	// Launch a new activity to compose a new tweet.
	public void doCompose(MenuItem item) {
		Intent intent = new Intent(this, ComposeActivity.class);
		startActivityForResult(intent, COMPOSE_INTENT);
	}

	// Given an array of tweets, set the oldest and newest tweet IDs.
	private void setMinAndMaxIDs(ArrayList<Tweet> tweets) {
		if (tweets != null && !tweets.isEmpty()) {
			for (int i = 0; i < tweets.size(); ++i) {
				long tweet_id = tweets.get(i).getId();
				if (tweet_id < oldest_id) {
					oldest_id = tweet_id;
				}
				if (tweet_id > newest_id) {
					newest_id = tweet_id;
				}
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == COMPOSE_INTENT && resultCode == RESULT_OK) {
			// Get the newly posted tweet and add it to the timeline.
			Log.d("DEBUG", "Got activity result");
			Tweet tweet =
					(Tweet) data.getExtras().getSerializable(INTENT_RESPONSE_TWEET);
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
}
