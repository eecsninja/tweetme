package com.codepath.apps.basictwitter;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class TweetViewActivity extends Activity {
	// Views.
	ImageView profile_image;
	TextView name_field;
	TextView screen_name_field;
	TextView time_ago_field;
	TextView tweet_body_field;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tweet_view);

		setupViews();
	}

	private void setupViews() {
		// Set up views.
		profile_image = (ImageView) findViewById(R.id.ivDetailProfileImage);
		name_field = (TextView) findViewById(R.id.tvDetailName);
		screen_name_field = (TextView) findViewById(R.id.tvDetailScreenName);
		time_ago_field = (TextView) findViewById(R.id.tvDetailTimestamp);
		tweet_body_field = (TextView) findViewById(R.id.tvDetailBody);
	}
}
