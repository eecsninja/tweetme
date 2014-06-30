package com.codepath.apps.basictwitter.activities;

import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;

import com.codepath.apps.basictwitter.R;
import com.codepath.apps.basictwitter.TwitterApp;
import com.codepath.apps.basictwitter.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;

public class ProfileActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);

		TwitterApp.getRestClient().getProfileInfo(
				new JsonHttpResponseHandler() {
					@Override
					public void onSuccess(JSONObject json) {
						User user = User.fromJSON(json);
						// Display user screen name on the action bar.
						getActionBar().setTitle("@" + user.getScreenName());
					}
				});
	}
}
