package com.codepath.apps.basictwitter;

import java.util.ArrayList;

import org.json.JSONArray;

import com.codepath.apps.basictwitter.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class TimelineActivity extends Activity {
	private TwitterClient client;
	private ArrayList<Tweet> tweets;
	private ArrayAdapter<Tweet> tweets_adapter;
	private ListView tweets_view;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timeline);

		client = TwitterApp.getRestClient();
		populateTimeline();

		// Load and display tweets.
		tweets_view = (ListView) findViewById(R.id.lvTweets);
		tweets = new ArrayList<Tweet>();
		tweets_adapter = new ArrayAdapter<Tweet>(this, android.R.layout.simple_list_item_1);
		tweets_view.setAdapter(tweets_adapter);
	}

	public void populateTimeline() {
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
		});
	}
}
