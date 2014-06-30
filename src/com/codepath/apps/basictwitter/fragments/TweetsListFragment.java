package com.codepath.apps.basictwitter.fragments;

import java.util.ArrayList;

import org.json.JSONArray;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.codepath.apps.basictwitter.R;
import com.codepath.apps.basictwitter.TwitterApp;
import com.codepath.apps.basictwitter.TwitterClient;
import com.codepath.apps.basictwitter.activities.ComposeActivity;
import com.codepath.apps.basictwitter.activities.TweetViewActivity;
import com.codepath.apps.basictwitter.adapters.TweetArrayAdapter;
import com.codepath.apps.basictwitter.helpers.EndlessScrollListener;
import com.codepath.apps.basictwitter.models.Tweet;
import com.codepath.apps.basictwitter.models.User;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import eu.erikw.PullToRefreshListView;
import eu.erikw.PullToRefreshListView.OnRefreshListener;

// Generic tweet timeline container.
public class TweetsListFragment extends Fragment {
	// Handles to views.
	protected ArrayList<Tweet> tweets;
	protected TweetArrayAdapter tweets_adapter;
	protected PullToRefreshListView tweets_view;

	// Used to access Twitter API.
	protected TwitterClient client;

	// Should loaded tweets be saved to database?
	// TODO: this should always be the case, but using this flag
	// to temporarily disable database access for some timelines.
	protected boolean save_to_db = false;

	// Store the oldest and newest tweet IDs.
	long oldest_id = Long.MAX_VALUE;
	long newest_id = 1;

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
			if (save_to_db) {
				saveTweetsToDB(new_tweets);
			}
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

		// Non-view initialization.
		// Create containers and adapters.
		tweets = new ArrayList<Tweet>();
		tweets_adapter = new TweetArrayAdapter(getActivity(), tweets);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate layout.
		View view = inflater.inflate(
				R.layout.fragment_tweets_list, container, false);

		// Set up to display tweets.
		tweets_view = (PullToRefreshListView) view.findViewById(R.id.lvTweets);
		tweets_view.setAdapter(tweets_adapter);

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

		// Return it!
		return view;
	}

	// Add new tweets to the tweet list view.
	public void addTweets(ArrayList<Tweet> tweets) {
		if (tweets.isEmpty()) {
			return;
		}
		// Determine newest and oldest tweet IDs.
		setMinAndMaxIDs(tweets);
		// This assumes all the new tweets are newer than or older than
		// the existing tweets.
		// TODO: Add stricter checking.
		long min_new_tweet_id = tweets.get(tweets.size() - 1).getUniqueId();
		if (!this.tweets.isEmpty() &&
			min_new_tweet_id > this.tweets.get(0).getUniqueId()) {
			// If the new tweets are more recent than the existing tweets,
			// add new tweets to the beginning of the list.
			for (int i = 0; i < tweets.size(); ++i) {
				tweets_adapter.insert(tweets.get(i), i);
			}
		} else {
			tweets_adapter.addAll(tweets);
		}
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

	// Checks for presence of Internet connection. Returns true if connection
	// is available, false if not.
	protected boolean hasInternetConnectivity() {
		ConnectivityManager connectivity =
				(ConnectivityManager) getActivity().getSystemService(
						Context.CONNECTIVITY_SERVICE);

		NetworkInfo activeNetwork = connectivity.getActiveNetworkInfo();
		boolean has_connection = (activeNetwork != null) &&
								 activeNetwork.isConnectedOrConnecting();
		return has_connection;
	}

	// Warns of lack of internet connection using a toast.
	protected void warnOfLackOfInternetConnectivity() {
		Toast
			.makeText(getActivity().getApplicationContext(),
					  "No internet connection!", Toast.LENGTH_SHORT)
			.show();
	}

	// Helper function that converts a tweet ID number to a string. If the
	// ID is 0, treat it as undefined and return a null string.
	protected static String getTweetIDString(long id) {
		if (id == 0) {
			return null;
		}
		return Long.toString(id);
	}

	// Saves a list of tweets to SQL database.
	protected void saveTweetsToDB(ArrayList<Tweet> tweets) {
		for (Tweet tweet : tweets) {
			// Be sure to save both tweet and user data.
			User user = tweet.getUser();
			user.save();
			tweet.save();
		}
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
			Tweet tweet =
					(Tweet) data.getExtras()
							.getSerializable(ComposeActivity.INTENT_RESPONSE_TWEET);
			if (tweet.getId() > newest_id) {
				newest_id = tweet.getId();
			}
			// TODO: This does not take into account the possibility of other
			// new tweets having been added to home timeline during the time it
			// took to compose.
			tweets_adapter.insert(tweet, 0);
		}
	}

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
		if (save_to_db) {
			ArrayList<Tweet> db_tweets =
					Tweet.getTweetsFromDB(null, start_id, max_id);
			if (!db_tweets.isEmpty()) {
				addTweets(db_tweets);
				return;
			}
		}
		if (has_internet) {
			// Otherwise, load from Twitter API if connected.
			getTimeline(new JSONHandler(refresh), start_id, max_id);
		}
	}

	// Loads a timeline from the Twitter client.
	protected void getTimeline(
			AsyncHttpResponseHandler handler, long start_id, long max_id) {
		// This does nothing, because it is part of a generic fragment.
		// Derived classes should call a particular timeline function.
	}
}
