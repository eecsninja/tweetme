package com.codepath.apps.basictwitter.activities;

import org.json.JSONObject;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.apps.basictwitter.R;
import com.codepath.apps.basictwitter.TwitterApp;
import com.codepath.apps.basictwitter.fragments.TweetsListFragment;
import com.codepath.apps.basictwitter.models.Tweet;
import com.codepath.apps.basictwitter.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ProfileActivity
		extends FragmentActivity
		implements
				TweetsListFragment.OnTweetClickedListener,
				TweetsListFragment.OnProfileIconClickedListener {
	static final String SCREEN_NAME_EXTRA = "screen_name";

	@Override
	public void onProfileIconClicked(ImageView profile_icon) {
		// Do not do anything. The profile view contains only this user's
		// tweets so there is no sense in launching another profile activity.
	}

	@Override
	public void onTweetClicked(Tweet tweet) {
		// TODO: Launch detailed tweet view.
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);

		// Load screen name, if available.
		String screen_name = getIntent().getStringExtra(SCREEN_NAME_EXTRA);
		loadProfileInfo(screen_name);

		// TODO: Load a particular user's timeline.
	}

	private void loadProfileInfo(String screen_name) {
		TwitterApp.getRestClient().getProfileInfo(
				new JsonHttpResponseHandler() {
					@Override
					public void onSuccess(JSONObject json) {
						User user = User.fromJSON(json);
						// Display user screen name on the action bar.
						getActionBar().setTitle("@" + user.getScreenName());
						// Update the views to reflect profile info.
						populateProfileHeader(user);
					}
				}, screen_name);
	}

	// Fills out the view elements with user info.
	private void populateProfileHeader(User user) {
		TextView name_field = (TextView) findViewById(R.id.tvProfileViewName);
		TextView tagline_field = (TextView) findViewById(R.id.tvProfileViewTagline);
		TextView followers_field = (TextView) findViewById(R.id.tvNumFollowers);
		TextView following_field = (TextView) findViewById(R.id.tvNumFollowing);
		ImageView profile_image = (ImageView) findViewById(R.id.ivProfileViewImage);

		name_field.setText(user.getName());
		tagline_field.setText(user.getTagline());
		followers_field.setText("" + user.getNumFollowers() + " Followers");
		following_field.setText("" + user.getNumFollowing() + " Following");
		ImageLoader.getInstance()
				.displayImage(user.getProfileImageUrl(), profile_image);
	}
}
