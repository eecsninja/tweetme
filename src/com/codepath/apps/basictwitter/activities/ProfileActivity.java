package com.codepath.apps.basictwitter.activities;

import java.util.ArrayList;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.apps.basictwitter.R;
import com.codepath.apps.basictwitter.TwitterApp;
import com.codepath.apps.basictwitter.fragments.TweetsListFragment;
import com.codepath.apps.basictwitter.fragments.UserTimelineFragment;
import com.codepath.apps.basictwitter.helpers.StoredAccountInfo;
import com.codepath.apps.basictwitter.models.Tweet;
import com.codepath.apps.basictwitter.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ProfileActivity
		extends FragmentActivity
		implements
				TweetsListFragment.OnTweetClickedListener,
				TweetsListFragment.OnProfileIconClickedListener,
				TweetsListFragment.NetworkRequestObserver {
	static final String SCREEN_NAME_EXTRA = "screen_name";

	// Screen name of the user whose profile is being shown.
	String screen_name;

	// Fragment displaying the current user's timeline.
	UserTimelineFragment user_timeline;

	@Override
	public void onProfileIconClicked(ImageView profile_icon) {
		// Do not do anything. The profile view contains only this user's
		// tweets so there is no sense in launching another profile activity.
	}

	@Override
	public void onTweetClicked(Tweet tweet) {
		// Launches a TweetViewActivity to view a single tweet.
		// TODO: This is duplicated from TimelineActivity. See if it can be
		// shared.
		Intent intent = new Intent(this, TweetViewActivity.class);
		intent.putExtra(TweetViewActivity.INTENT_TWEET_VIEW, tweet);
		startActivityForResult(intent, TweetViewActivity.TWEET_VIEW_CODE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK) {
			return;
		}
		// A new tweet could be returned indirectly by TweetViewActivity.
		boolean do_get_tweet_result =
				requestCode == TweetViewActivity.TWEET_VIEW_CODE &&
				data.getExtras().containsKey(ComposeActivity.INTENT_RESPONSE_TWEET);
		if (do_get_tweet_result && screen_name == null) {
			// If the profile being viewed is the current user's profile, add the
			// new tweet to the timeline.
			Tweet tweet =
					(Tweet) data.getExtras()
							.getSerializable(ComposeActivity.INTENT_RESPONSE_TWEET);
			ArrayList<Tweet> tweets = new ArrayList<Tweet>();
			tweets.add(tweet);
			user_timeline.addTweets(tweets);
		}
	}

	// Inherited from TweetsListFragment.NetworkRequestObserver.
	@Override
	public void onNetworkRequestBegin() {
		setProgressBarIndeterminateVisibility(true);
	}

	// Inherited from TweetsListFragment.NetworkRequestObserver.
	@Override
	public void onNetworkRequestEnd() {
		setProgressBarIndeterminateVisibility(false);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// MUST request the feature before setting content view
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		setContentView(R.layout.activity_profile);

		// Load screen name, if available.
		screen_name = getIntent().getStringExtra(SCREEN_NAME_EXTRA);
		loadProfileInfo(screen_name);

		// Create the user timeline fragment dynamically.
		FragmentTransaction transaction =
				getSupportFragmentManager().beginTransaction();
		// Replace the container with UserTimelineFragment.
		user_timeline = new UserTimelineFragment();
		user_timeline.setScreenName(screen_name);
		transaction.replace(R.id.flTimelineFragment, user_timeline);
		// Execute the changes specified
		transaction.commit();
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

						// Store the user info in shared preferences if it is the
						// current user, as indicated by |screen_name| == null.
						if (ProfileActivity.this.screen_name == null) {
							StoredAccountInfo.storeUserInfo(ProfileActivity.this, user);
						}
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
