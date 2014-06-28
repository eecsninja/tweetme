package com.codepath.apps.basictwitter.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;

public class Tweet extends Model implements Serializable {
	// Serialization ID.
	// TODO: Try Parcelable instead.
	private static final long serialVersionUID = -4561762185420913284L;

	// Limit the number of tweets to return from database.
	private static int MAX_TWEETS_PER_QUERY = 25;

	@Column(name = "remote_id", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
	private long uid;			// Unique ID of tweet.
	@Column(name = "body")
	private String body;		// Content of tweet.
	@Column(name = "timestamp")
	private String timestamp;	// Time this tweet was created.
	@Column(name = "user")
	private User user;

	// The default constructor is required for ActiveAndroid's Model class.
	public Tweet() {
		super();
	}

	public static Tweet fromJSON(JSONObject json) {
		Tweet tweet = new Tweet();
		// Extract values from JSON object to fill in the fields.
		try {
			tweet.body = json.getString("text");
			tweet.uid = json.getLong("id");
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

	// Get all tweets from a user. Pass in user=null to get tweets from
	// all users.
	public static ArrayList<Tweet> getTweetsFromDB(
			User user, long min_uid, long max_uid) {
		From query = new Select().from(Tweet.class);
		if (user != null) {
			//query = query.where("user = ?", user.getId());
		}
		// Sort by ID, most recent first.
		//query = query.orderBy("remote_id DESC");
		if (min_uid != 0) {
			//query = query.where("remote_id >= ?", (min_uid));
		}
		if (max_uid != 0) {
			//query = query.where("remote_id <= ?", Long.toString(max_uid));
		}
		String[] args = query.getArguments();
		String query_string = "";
		Log.d("DEBUG", "query has " + args.length + " args");
		for (int i = 0; i < args.length; ++i)
			query_string += args[i] + " ";
		Log.d("DEBUG", "Querying: " + query_string);
		List<Tweet> results = query.execute();

		// Convert to array list.
		return new ArrayList<Tweet>(results);
	}

	@Override
	public String toString() {
		return getUser().getScreenName() + ": " + getBody();
	}

	public String getBody() {
		return body;
	}

	public long getUniqueId() {
		return uid;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public User getUser() {
		return user;
	}
}
