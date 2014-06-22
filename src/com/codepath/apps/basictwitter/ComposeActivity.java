package com.codepath.apps.basictwitter;

import org.json.JSONObject;

import com.codepath.apps.basictwitter.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ComposeActivity extends Activity {
	// Peter Thiel: We wanted flying cars, instead we got...
	private static final int MAX_TWEET_LENGTH = 140;

	private TwitterClient client;

	// Views.
	TextView label_text;
	Button tweet_button;
	EditText text_field;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_compose);

		// Load REST client handle.
		client = TwitterApp.getRestClient();

		// Load views.
		label_text = (TextView) findViewById(R.id.tvEnterTweetLabel);
		tweet_button = (Button) findViewById(R.id.btTweet);
		text_field = (EditText) findViewById(R.id.etTweet);

		setNumCharsRemaining();

		// Attach text listener.
		text_field.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				setNumCharsRemaining();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
	}

	private void setNumCharsRemaining() {
		// Show the number of characters remaining.
		int num_chars_remaining =
				MAX_TWEET_LENGTH - text_field.getText().length();
		label_text.setText("Enter your tweet (" + num_chars_remaining + "):");
	}

	// Submit the tweet!
	public void onSubmit(View v) {
		String text = text_field.getText().toString();
		// No sense in tweeting an empty string.
		if (text.isEmpty()) {
			Toast.makeText(this, "Write something.", Toast.LENGTH_SHORT).show();
			return;
		}
		if (text.length() > MAX_TWEET_LENGTH) {
			Toast.makeText(this, "Tweet is too long!", Toast.LENGTH_SHORT).show();
			return;
		}
		client.doTweet(new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(JSONObject object) {
				Log.d("DEBUG", "Success!");
				finishWithResponse(RESULT_OK, object);
			}

			@Override
			public void onFailure(Throwable e, String s) {
				Log.d("DEBUG", e.toString());
				Log.d("DEBUG", s.toString());
				finishWithResponse(RESULT_CANCELED, null);
			}
		}, text);
	}

	// Finishes the activity and returns the JSON response if available.
	private void finishWithResponse(int result_code, JSONObject response) {
		// Generate a toast to provide status info.
		String toast_text = "";
		if (result_code == RESULT_OK) {
			toast_text = "Successfully tweeted!";
		} else {
			toast_text = "Tweet post failed!";
		}
		Toast.makeText(this, toast_text, Toast.LENGTH_SHORT).show();
		// Finish the activity.
		Intent data = new Intent();
		if (response != null) {
			data.putExtra(TimelineActivity.INTENT_RESPONSE_TWEET, Tweet.fromJSON(response));
		}
		setResult(result_code, data);
		finish();
	}
}
