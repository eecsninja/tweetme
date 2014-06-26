package com.codepath.apps.basictwitter;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import com.codepath.apps.basictwitter.models.Tweet;
import com.codepath.apps.basictwitter.models.User;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class TweetViewActivity extends Activity {
	// Views.
	ImageView profile_image;
	TextView name_field;
	TextView screen_name_field;
	TextView time_ago_field;
	TextView tweet_body_field;

	// The tweet to display.
	Tweet tweet;

	// Intent request code and value key.
	public static final int TWEET_VIEW_CODE = 999;
	public static final String INTENT_TWEET_VIEW = "tweet";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tweet_view);

		// Get the tweet from the intent that launched this activity.
		tweet = (Tweet) getIntent().getSerializableExtra(INTENT_TWEET_VIEW);

		setupViews();
	}

	public void doReply(View view) {
		// When reply button is pressed, launch compose activity.
		Intent intent = new Intent(this, ComposeActivity.class);
		startActivityForResult(intent, ComposeActivity.COMPOSE_INTENT);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ComposeActivity.COMPOSE_INTENT && resultCode == RESULT_OK) {
			// Return the new tweet to the timeline.
			Serializable tweet = data.getExtras()
							.getSerializable(ComposeActivity.INTENT_RESPONSE_TWEET);

			Intent response = new Intent();
			response.putExtra(ComposeActivity.INTENT_RESPONSE_TWEET, tweet);
			setResult(resultCode, response);
			finish();
		}
	}

	private void setupViews() {
		// Set up views.
		profile_image = (ImageView) findViewById(R.id.ivDetailProfileImage);
		name_field = (TextView) findViewById(R.id.tvDetailName);
		screen_name_field = (TextView) findViewById(R.id.tvDetailScreenName);
		time_ago_field = (TextView) findViewById(R.id.tvDetailTimestamp);
		tweet_body_field = (TextView) findViewById(R.id.tvDetailBody);

		// Set up views.
		User user = tweet.getUser();
		ImageLoader.getInstance()
				.displayImage(user.getProfileImageUrl(), profile_image);
		name_field.setText(user.getName());
		screen_name_field.setText("@" + user.getScreenName());
		time_ago_field.setText(getRelativeTimeAgo(tweet.getTimestamp()));
		tweet_body_field.setText(tweet.getBody());
	}

	// Returns relative time label, given an absolute timestamp string.
	// TODO: combine with the same code in TweetArrayAdapter.
	private String getRelativeTimeAgo(String timestamp) {
		final String TWITTER_FORMAT = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
		SimpleDateFormat format =
				new SimpleDateFormat(TWITTER_FORMAT, Locale.ENGLISH);
		format.setLenient(true);
		String relative_date = "";
		try {
			long date_ms = format.parse(timestamp).getTime();
			relative_date = DateUtils.getRelativeTimeSpanString(
					date_ms,
					System.currentTimeMillis(),
					DateUtils.SECOND_IN_MILLIS).toString();
		} catch (ParseException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		return relative_date;
	}
}
