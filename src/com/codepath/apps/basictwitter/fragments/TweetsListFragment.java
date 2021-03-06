package com.codepath.apps.basictwitter.fragments;

import java.util.ArrayList;

import org.json.JSONArray;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.Toast;

import com.codepath.apps.basictwitter.R;
import com.codepath.apps.basictwitter.TwitterApp;
import com.codepath.apps.basictwitter.TwitterClient;
import com.codepath.apps.basictwitter.adapters.TweetArrayAdapter;
import com.codepath.apps.basictwitter.helpers.EndlessScrollListener;
import com.codepath.apps.basictwitter.models.Tweet;
import com.codepath.apps.basictwitter.models.User;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import eu.erikw.PullToRefreshListView;
import eu.erikw.PullToRefreshListView.OnRefreshListener;

// Generic tweet timeline container.
public abstract class TweetsListFragment extends Fragment {
	// Handles to views.
	protected ArrayList<Tweet> tweets;
	protected TweetArrayAdapter tweets_adapter;
	protected PullToRefreshListView tweets_view;

	// Used to access Twitter API.
	protected TwitterClient client;

	// Store the oldest and newest tweet IDs.
	long oldest_id = Long.MAX_VALUE;
	long newest_id = 1;

	// For listening to tweet clicks.
	OnTweetClickedListener tweet_clicked_listener;
	// For listening to profile icon clicks.
	OnProfileIconClickedListener profile_icon_clicked_listener;
	// For listening to network requests.
	NetworkRequestObserver network_request_observer;

	// Custom JSON response handler.
	protected class JSONHandler extends JsonHttpResponseHandler {
		private boolean is_refreshing = false;

		public JSONHandler(boolean do_refresh) {
			super();
			is_refreshing = do_refresh;
		}
		@Override
		public void onSuccess(JSONArray array) {
			network_request_observer.onNetworkRequestEnd();

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
			network_request_observer.onNetworkRequestEnd();
			Log.d("DEBUG", e.toString());
			Log.d("DEBUG", s.toString());
		}
	}

	// Listener interface for handling clicks on tweets.
	public interface OnTweetClickedListener {
		public void onTweetClicked(Tweet tweet);
	}

	// Listener interface for handling clicks on profile icons.
	public interface OnProfileIconClickedListener {
		public void onProfileIconClicked(ImageView profile_icon);
	}

	// Listener for network requests.
	public interface NetworkRequestObserver {
		// Called when a network request begins and ends, respectively.
		public void onNetworkRequestBegin();
		public void onNetworkRequestEnd();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Get handler for clicks on tweets.
		if (activity instanceof OnTweetClickedListener) {
			tweet_clicked_listener = (OnTweetClickedListener) activity;
		} else {
			throw new ClassCastException(activity.toString()
					+ " must implement TweetsListFragment.OnTweetClickedListener");
		}
		// Get handler for clicks on profile icon.
		if (activity instanceof OnProfileIconClickedListener) {
			profile_icon_clicked_listener = (OnProfileIconClickedListener) activity;
		} else {
			throw new ClassCastException(activity.toString()
					+ " must implement TweetsListFragment.OnProfileIconClickedListener");
		}
		// Get network request observer.
		if (activity instanceof NetworkRequestObserver) {
			network_request_observer = (NetworkRequestObserver) activity;
		} else {
			throw new ClassCastException(activity.toString()
					+ " must implement TweetsListFragment.NetworkRequestObserver");
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
		tweets_adapter = new TweetArrayAdapter(
				getActivity(), tweets,
				new android.view.View.OnClickListener() {
					@Override
					public void onClick(View view) {
						ImageView profile_icon = (ImageView) view;
						profile_icon_clicked_listener.onProfileIconClicked(profile_icon);
					}
				});
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
				// Get the tweet that was clicked and pass it to the listener.
				tweet_clicked_listener.onTweetClicked(tweets.get(position));
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

	// Loads tweets within an ID range.
	protected void loadTweets(boolean refresh, long start_id, long max_id) {
		boolean has_internet = hasInternetConnectivity();
		// Warn user about lack of internet, if applicable.
		if (!has_internet) {
			warnOfLackOfInternetConnectivity();
		}
		// If there are tweets cached in the database, load those.
		ArrayList<Tweet> db_tweets = getTweetsFromDatabaseForTimeline(start_id, max_id);
		if (!db_tweets.isEmpty()) {
			addTweets(db_tweets);
			// Modify the arguments if some cached tweets were loaded.
			// This sets up the subsequent timeline request to load
			// only newer tweets.
			start_id = newest_id + 1;
		}
		if (has_internet) {
			// Otherwise, load from Twitter API if connected.
			getTimeline(new JSONHandler(refresh), start_id, max_id);

			// Tell the network request observer that a request is underway.
			network_request_observer.onNetworkRequestBegin();
		}
	}

	// Loads a timeline from the Twitter client.
	abstract protected void getTimeline(
			AsyncHttpResponseHandler handler, long start_id, long max_id);

	// Loads cached tweets for the particular timeline.
	abstract protected ArrayList<Tweet> getTweetsFromDatabaseForTimeline(
			long start_id, long max_id);
}
