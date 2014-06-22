package com.codepath.apps.basictwitter;

import java.util.ArrayList;

import org.json.JSONArray;

import com.codepath.apps.basictwitter.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

public class TimelineActivity extends Activity {
	private TwitterClient client;
	private ArrayList<Tweet> tweets;
	private TweetArrayAdapter tweets_adapter;
	private ListView tweets_view;

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

	public void populateTimeline() {
		// Determine a start ID for getting tweets. If there are no existing
		// tweets loaded, then load as many as possible. Otherwise, look for
		// the lowest ID and start below that.
		// TODO: iterating over all tweets every time is inefficient. Need to
		// cache this somehow.
		String max_id = null;
		if (tweets != null && !tweets.isEmpty()) {
			long lowest_id = Long.MAX_VALUE;
			for (int i = 0; i < tweets.size(); ++i) {
				long tweet_id = tweets.get(i).getId();
				if (tweet_id < lowest_id) {
					lowest_id = tweet_id;
				}
			}
			max_id = Long.toString(lowest_id - 1);
		}
		// Define a custom handler.
		client.getHomeTimeline(new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(JSONArray array) {
				tweets_adapter.addAll(Tweet.fromJSONArray(array));
			}

			@Override
			public void onFailure(Throwable e, String s) {
				Log.d("DEBUG", e.toString());
				Log.d("DEBUG", s.toString());
			}
		}, max_id);
	}
}
