package com.codepath.apps.basictwitter;

import org.json.JSONObject;

import com.loopj.android.http.JsonHttpResponseHandler;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ComposeActivity extends Activity {
	private TwitterClient client;

	// Views.
	Button tweet_button;
	EditText text_field;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_compose);

		// Load REST client handle.
		client = TwitterApp.getRestClient();

		// Load views.
		tweet_button = (Button) findViewById(R.id.btTweet);
		text_field = (EditText) findViewById(R.id.etTweet);
	}

	// Submit the tweet!
	public void onSubmit(View v) {
		String text = text_field.getText().toString();
		// No sense in tweeting an empty string.
		if (text.isEmpty()) {
			Toast.makeText(this, "Write something.", Toast.LENGTH_SHORT).show();
			return;
		}
		client.doTweet(new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(JSONObject object) {
				Log.d("DEBUG", "Success!");
				finishWithToast(RESULT_OK, "Successfully tweeted!");
			}

			@Override
			public void onFailure(Throwable e, String s) {
				Log.d("DEBUG", e.toString());
				Log.d("DEBUG", s.toString());
				finishWithToast(RESULT_CANCELED, "Tweet post failed!");
			}
		}, text);
	}

	// Finishes the activity with a toast to provide status info.
	private void finishWithToast(int result_code, String toast_text) {
		Toast.makeText(this, toast_text, Toast.LENGTH_SHORT).show();
		// Finish the activity.
		Intent data = new Intent();
		setResult(result_code, data);
		Log.d("DEBUG", "Finish!");
		finish();
	}
}
