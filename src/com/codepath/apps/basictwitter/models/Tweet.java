package com.codepath.apps.basictwitter.models;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Tweet implements Serializable {
	// Serialization ID.
	// TODO: Try Parcelable instead.
	private static final long serialVersionUID = -4561762185420913284L;

	private String body;		// Content of tweet.
	private long id;			// Unique ID of tweet.
	private String timestamp;	// Time this tweet was created.
	private User user;

	public static Tweet fromJSON(JSONObject json) {
		Tweet tweet = new Tweet();
		// Extract values from JSON object to fill in the fields.
		try {
			tweet.body = json.getString("text");
			tweet.id = json.getLong("id");
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

	@Override
	public String toString() {
		return getUser().getScreenName() + ": " + getBody();
	}

	public String getBody() {
		return body;
	}

	public long getId() {
		return id;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public User getUser() {
		return user;
	}
}
