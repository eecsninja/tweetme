package com.codepath.apps.basictwitter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ComposeActivity extends Activity {
	// Views.
	Button tweet_button;
	EditText text_field;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_compose);

		// Load views.
		tweet_button = (Button) findViewById(R.id.btTweet);
		text_field = (EditText) findViewById(R.id.etTweet);
	}

	// Submit the tweet!
	public void onSubmit(View v) {
		// TODO: call the POST API.
		// Finish the activity.
		Intent data = new Intent();
		setResult(RESULT_OK, data);
		finish();
	}
}
