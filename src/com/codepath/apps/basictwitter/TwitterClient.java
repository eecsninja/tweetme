package com.codepath.apps.basictwitter;

import org.scribe.builder.api.Api;
import org.scribe.builder.api.TwitterApi;

import android.content.Context;
import android.util.Log;

import com.codepath.oauth.OAuthBaseClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/*
 * 
 * This is the object responsible for communicating with a REST API. 
 * Specify the constants below to change the API being communicated with.
 * See a full list of supported API classes: 
 *   https://github.com/fernandezpablo85/scribe-java/tree/master/src/main/java/org/scribe/builder/api
 * Key and Secret are provided by the developer site for the given API i.e dev.twitter.com
 * Add methods for each relevant endpoint in the API.
 * 
 * NOTE: You may want to rename this object based on the service i.e TwitterClient or FlickrClient
 * 
 */
public class TwitterClient extends OAuthBaseClient {
	public static final Class<? extends Api> REST_API_CLASS = TwitterApi.class; // Change this
	public static final String REST_URL = "https://api.twitter.com/1.1"; // Change this, base API URL
	public static final String REST_CONSUMER_KEY = "sjXeElY8eJTH6K1v5DcvqPF98";       // Change this
	public static final String REST_CONSUMER_SECRET = "vGkKF5kbF5FUZf85oM9o1AG584ohLv1WG2TBZluTtYqk2UmZTo"; // Change this
	public static final String REST_CALLBACK_URL = "oauth://cpbasictweets"; // Change this (here and in manifest)

	public TwitterClient(Context context) {
		super(context, REST_API_CLASS, REST_URL, REST_CONSUMER_KEY, REST_CONSUMER_SECRET, REST_CALLBACK_URL);
	}

	public void getHomeTimeline(AsyncHttpResponseHandler handler,
								String start_id, String max_id) {
		getTimeline(getApiUrl("statuses/home_timeline.json"),
				handler, start_id, max_id);
	}

	public void getMentionsTimeline(AsyncHttpResponseHandler handler,
									String start_id, String max_id) {
		getTimeline(getApiUrl("statuses/mentions_timeline.json"),
					handler, start_id, max_id);
	}

	public void doTweet(AsyncHttpResponseHandler handler, String status) {
		// Make sure the status string is valid.
		if (status == null) {
			return;
		}
		// Start an update API call.
		String api_url = getApiUrl("statuses/update.json");
		RequestParams params = new RequestParams();
		params.put("status", status);
		// Call the POST API.
		Log.d("DEBUG", "Posting to " + api_url);
		client.post(api_url, params, handler);
	}

	// Get a timeline from a timeline JSON API.
	// Pass in start and max ID values, or null for either.
	private void getTimeline(String api_url, AsyncHttpResponseHandler handler,
							 String start_id, String max_id) {
		RequestParams params = new RequestParams();
		if (start_id != null)
			params.put("since_id", start_id);
		if (max_id != null)
			params.put("max_id", max_id);
		// Must match the GET/POST designation of the API.
		client.get(api_url, params, handler);
	}

	/* 1. Define the endpoint URL with getApiUrl and pass a relative path to the endpoint
	 * 	  i.e getApiUrl("statuses/home_timeline.json");
	 * 2. Define the parameters to pass to the request (query or body)
	 *    i.e RequestParams params = new RequestParams("foo", "bar");
	 * 3. Define the request method and make a call to the client
	 *    i.e client.get(apiUrl, params, handler);
	 *    i.e client.post(apiUrl, params, handler);
	 */
}
