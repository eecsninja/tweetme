package com.codepath.apps.basictwitter.activities;

import java.util.ArrayList;

import org.json.JSONArray;

import com.codepath.apps.basictwitter.R;
import com.codepath.apps.basictwitter.TwitterApp;
import com.codepath.apps.basictwitter.TwitterClient;
import com.codepath.apps.basictwitter.adapters.TweetArrayAdapter;
import com.codepath.apps.basictwitter.helpers.EndlessScrollListener;
import com.codepath.apps.basictwitter.models.Tweet;
import com.codepath.apps.basictwitter.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;

import eu.erikw.PullToRefreshListView;
import eu.erikw.PullToRefreshListView.OnRefreshListener;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class TimelineActivity extends Activity {
	private TwitterClient client;
	private ArrayList<Tweet> tweets;
	private TweetArrayAdapter tweets_adapter;
	private PullToRefreshListView tweets_view;

	// Store the oldest and newest tweet IDs.
	long oldest_id = Long.MAX_VALUE;
	long newest_id = 1;

	// Custom JSON response handler.
	private class JSONHandler extends JsonHttpResponseHandler {
		private boolean is_refreshing = false;

		public JSONHandler(boolean do_refresh) {
			super();
			is_refreshing = do_refresh;
		}
		@Override
		public void onSuccess(JSONArray array) {
			ArrayList<Tweet> new_tweets = Tweet.fromJSONArray(array);
			// Determine newest and oldest tweet IDs.
			setMinAndMaxIDs(new_tweets);
			if (is_refreshing) {
				// Add new tweets to the beginning of the list.
				for (int i = 0; i < new_tweets.size(); ++i) {
					tweets_adapter.insert(new_tweets.get(i), i);
				}
				// Turn off the "refreshing" state of the pull down list view.
				tweets_view.onRefreshComplete();
			} else {
				tweets_adapter.addAll(new_tweets);
			}
			saveTweetsToDB(new_tweets);
		}

		@Override
		public void onFailure(Throwable e, String s) {
			Log.d("DEBUG", e.toString());
			Log.d("DEBUG", s.toString());
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timeline);

		// Set up to display tweets.
		tweets_view = (PullToRefreshListView) findViewById(R.id.lvTweets);
		tweets = new ArrayList<Tweet>();
		tweets_adapter = new TweetArrayAdapter(this, tweets);
		tweets_view.setAdapter(tweets_adapter);

		// Load some tweets.
		client = TwitterApp.getRestClient();

		// Add scroll listener.
		tweets_view.setOnScrollListener(new EndlessScrollListener() {
			@Override
			public void onLoadMore(int page, int totalItemsCount) {
				populateTimeline();
			}
		});
		// Add pull-down-to-refresh listener.
		tweets_view.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				loadNewTweets();
			}
		});
		// Add item click listener to launch detailed view.
		tweets_view.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// Get the tweet that was clicked.
				launchTweetView(tweets.get(position));
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
		if (!hasInternetConnectivity()) {
			warnOfLackOfInternetConnectivity();
			return;
		}
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

	public void loadNewTweets() {
		if (!hasInternetConnectivity()) {
			// If not connected, then reset the refresh UI and bail out.
			warnOfLackOfInternetConnectivity();
			tweets_view.onRefreshComplete();
			return;
		}
		// Get the tweets that are newer than the newest tweet that we've
		// already retrieved.
		loadTweets(true, newest_id, 0);
	}

	// Launch a new activity to compose a new tweet.
	public void doCompose(MenuItem item) {
		Intent intent = new Intent(this, ComposeActivity.class);
		startActivityForResult(intent, ComposeActivity.COMPOSE_INTENT);
	}

	// Given an array of tweets, set the oldest and newest tweet IDs.
	private void setMinAndMaxIDs(ArrayList<Tweet> tweets) {
		if (tweets != null && !tweets.isEmpty()) {
			for (int i = 0; i < tweets.size(); ++i) {
				long tweet_id = tweets.get(i).getUniqueId();
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
		if (resultCode != RESULT_OK) {
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

	// Launches a TweetViewActivity to view a single tweet.
	private void launchTweetView(Tweet tweet) {
		Intent intent = new Intent(this, TweetViewActivity.class);
		intent.putExtra(TweetViewActivity.INTENT_TWEET_VIEW, tweet);
		startActivityForResult(intent, TweetViewActivity.TWEET_VIEW_CODE);
	}

	// Checks for presence of Internet connection. Returns true if connection
	// is available, false if not.
	private boolean hasInternetConnectivity() {
		ConnectivityManager connectivity =
				(ConnectivityManager) this.getSystemService(
						Context.CONNECTIVITY_SERVICE);

		NetworkInfo activeNetwork = connectivity.getActiveNetworkInfo();
		boolean has_connection = (activeNetwork != null) &&
								 activeNetwork.isConnectedOrConnecting();
		return has_connection;
	}

	// Warns of lack of internet connection using a toast.
	private void warnOfLackOfInternetConnectivity() {
		Toast
			.makeText(this, "No internet connection!", Toast.LENGTH_SHORT)
			.show();
	}

	// Loads tweets within an ID range. If there are tweets cached in the
	// database, load those. Otherwise, load from online.
	private void loadTweets(boolean refresh, long start_id, long max_id) {
		client.getHomeTimeline(
				new JSONHandler(refresh),
				getTweetIDString(start_id),
				getTweetIDString(max_id));
	}

	// Helper function that converts a tweet ID number to a string. If the
	// ID is 0, treat it as undefined and return a null string.
	private static String getTweetIDString(long id) {
		if (id == 0) {
			return null;
		}
		return Long.toString(id);
	}

	// Saves a list of tweets to SQL database.
	private void saveTweetsToDB(ArrayList<Tweet> tweets) {
		for (Tweet tweet : tweets) {
			// Be sure to save both tweet and user data.
			User user = tweet.getUser();
			user.save();
			tweet.save();
		}
	}
}
