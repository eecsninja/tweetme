package com.codepath.apps.basictwitter.models;

import java.util.ArrayList;
import java.util.Collection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Tweet {
	private String body;		// Content of tweet.
	private long user_id;		// UID of user who tweeted this.
	private String timestamp;	// Time this tweet was created.
	private User user;

	public static Tweet fromJSON(JSONObject json) {
		Tweet tweet = new Tweet();
		// Extract values from JSON object to fill in the fields.
		try {
			tweet.body = json.getString("text");
			tweet.user_id = json.getLong("id");
			tweet.timestamp = json.getString("created_at");
			tweet.user = User.fromJSON(json.getJSONObject("user"));
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return tweet;
	}

	public static ArrayList<Tweet> fromJSONArray(JSONArray array) {
		ArrayList<Tweet> tweet_array = new ArrayList<Tweet>(array.length());
		for (int i = 0; i < array.length(); ++i) {
			JSONObject tweet_object = null;
			try {
				tweet_object = array.getJSONObject(i);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}

			Tweet tweet = Tweet.fromJSON(tweet_object);
			if (tweet != null) {
				tweet_array.add(tweet);
			}
		}
		return tweet_array;
	}

	public String getBody() {
		return body;
	}

	public long getUserId() {
		return user_id;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public User getUser() {
		return user;
	}
}
