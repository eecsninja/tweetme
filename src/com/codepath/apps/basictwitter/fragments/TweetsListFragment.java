package com.codepath.apps.basictwitter.fragments;

import java.util.ArrayList;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.codepath.apps.basictwitter.R;
import com.codepath.apps.basictwitter.adapters.TweetArrayAdapter;
import com.codepath.apps.basictwitter.helpers.EndlessScrollListener;
import com.codepath.apps.basictwitter.models.Tweet;
import com.codepath.apps.basictwitter.models.User;

import eu.erikw.PullToRefreshListView;
import eu.erikw.PullToRefreshListView.OnRefreshListener;

public class TweetsListFragment extends Fragment {
	// Handles to views.
	protected ArrayList<Tweet> tweets;
	protected TweetArrayAdapter tweets_adapter;
	protected PullToRefreshListView tweets_view;

	// Store the oldest and newest tweet IDs.
	long oldest_id = Long.MAX_VALUE;
	long newest_id = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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

	// Functions to be implemented in derived classes.
	protected void launchTweetView(Tweet tweet) {}
	protected void loadNewTweets() {}
	protected void populateTimeline() {}

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
}
