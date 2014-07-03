package com.codepath.apps.basictwitter.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;

public class Tweet extends Model implements Serializable {
	// Serialization ID.
	// TODO: Try Parcelable instead.
	private static final long serialVersionUID = -4561762185420913284L;

	// Limit the number of tweets to return from database. This is the default
	// number for the "count" field of the Twitter API's home_timeline call.
	// TODO: Allow varying numbers of tweets..
	private static int NUM_TWEETS_PER_QUERY = 20;

	// For selecting tweets from various timelines.
	public enum TweetType {
		// All tweets.
		TWEET_TYPE_ALL,
		// Tweets posted by the given user.
		TWEET_TYPE_USER,
		// Tweets that mention the given user.
		TWEET_TYPE_MENTIONS,
	}

	@Column(name = "remote_id", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
	private long uid;			// Unique ID of tweet.
	@Column(name = "body")
	private String body;		// Content of tweet.
	@Column(name = "timestamp")
	private String timestamp;	// Time this tweet was created.
	@Column(name = "user")
	private User user;
	// Copy of the screen name from |user|. This is used for querying purposes.
	// TODO: Figure out how to query the screen name from |user| directly.
	@Column(name = "user_screen_name")
	private String screen_name;

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
			tweet.screen_name = tweet.user.getScreenName();
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
			User user, TweetType type, long min_uid, long max_uid) {
		ArrayList<String> where_args = new ArrayList<String>();
		if (user != null) {
			switch (type) {
			case TWEET_TYPE_USER:
				// Match by user as author.
				where_args.add("user_screen_name = '" + user.getScreenName() + "'");
				break;
			case TWEET_TYPE_MENTIONS:
				// Match by mention of user.
				where_args.add("body LIKE '%" + "@" + user.getScreenName() + "%'");
				break;
			default:
				// Do nothing. All tweets should be loaded.
				break;
			}
		}
		// Set upper and lower bounds.
		if (min_uid != 0) {
			where_args.add("remote_id >= " + min_uid);
		}
		if (max_uid != 0) {
			where_args.add("remote_id <= " + max_uid);
		}

		From query = new Select().from(Tweet.class);
		query.where(joinWhereConditions(where_args));
		// Sort by ID, most recent first.
		query = query.orderBy("remote_id DESC");
		// Limit the number of queries.
		query = query.limit(NUM_TWEETS_PER_QUERY);
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

	// Joins a list of WHERE args together using AND, into one string.
	// TODO: This has been fixed in a newer version of ActiveAndroid.
	private static String joinWhereConditions(ArrayList<String> where_args) {
		String where_string = "";
		for (String arg : where_args) {
			if (!where_string.isEmpty()) {
				where_string += " AND ";
			}
			where_string += arg;
		}
		return where_string;
	}
}
