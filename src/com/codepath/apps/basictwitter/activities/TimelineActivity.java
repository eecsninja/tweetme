package com.codepath.apps.basictwitter.activities;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.codepath.apps.basictwitter.R;
import com.codepath.apps.basictwitter.fragments.HomeTimelineFragment;
import com.codepath.apps.basictwitter.fragments.MentionsTimelineFragment;
import com.codepath.apps.basictwitter.fragments.TweetsListFragment;
import com.codepath.apps.basictwitter.helpers.FragmentTabListener;
import com.codepath.apps.basictwitter.models.Tweet;
import com.codepath.apps.basictwitter.models.User;

public class TimelineActivity extends FragmentActivity {
	// Handles to fragments.
	TweetsListFragment home_timeline = null;
	TweetsListFragment mentions_timeline = null;

	static final String HOME_TIMELINE_TAG = "home";
	static final String MENTIONS_TIMELINE_TAG = "mentions";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timeline);
		setupTabs();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Set up menu.
		getMenuInflater().inflate(R.menu.timeline, menu);
		return true;
	}

	// Launch a new activity to compose a new tweet.
	public void doCompose(MenuItem item) {
		Intent intent = new Intent(this, ComposeActivity.class);
		startActivityForResult(intent, ComposeActivity.COMPOSE_INTENT);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK) {
			return;
		}
		// A new tweet could be returned by either ComposeActivity or by
		// TweetViewActivity (indirectly).
		boolean do_get_tweet_result =
				(requestCode == ComposeActivity.COMPOSE_INTENT) ||
				(requestCode == TweetViewActivity.TWEET_VIEW_CODE &&
				 data.getExtras().containsKey(ComposeActivity.INTENT_RESPONSE_TWEET));
		if (do_get_tweet_result) {
			// Get the newly posted tweet and add it to the home timeline.
			Tweet tweet =
					(Tweet) data.getExtras()
							.getSerializable(ComposeActivity.INTENT_RESPONSE_TWEET);
			loadTabFragments();
			ArrayList<Tweet> tweets = new ArrayList<Tweet>();
			tweets.add(tweet);
			if (home_timeline != null) {
				home_timeline.addTweets(tweets);
			}
			// Add it to the mentions timeline if the user is mentioned.
			User user = tweet.getUser();
			if (mentions_timeline != null &&
				tweet.getBody().contains("@" + user.getScreenName())) {
				mentions_timeline.addTweets(tweets);
			}
		}
	}

	// View the user's profile.
	public void doProfileView(MenuItem item) {
		Intent intent = new Intent(this, ProfileActivity.class);
		startActivity(intent);
	}

	private void setupTabs() {
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayShowTitleEnabled(true);

		Tab tab1 = actionBar
				.newTab()
				.setText("Home")
				.setIcon(R.drawable.ic_action_home_timeline)
				.setTag("HomeTimelineFragment")
				.setTabListener(
						new FragmentTabListener<HomeTimelineFragment>(
								R.id.flContainer, this, HOME_TIMELINE_TAG,
								HomeTimelineFragment.class));

		actionBar.addTab(tab1);
		actionBar.selectTab(tab1);

		Tab tab2 = actionBar
				.newTab()
				.setText("Mentions")
				.setIcon(R.drawable.ic_action_mentions_timeline)
				.setTag("MentionsTimelineFragment")
				.setTabListener(
						new FragmentTabListener<MentionsTimelineFragment>(
								R.id.flContainer, this, MENTIONS_TIMELINE_TAG,
								MentionsTimelineFragment.class));

		actionBar.addTab(tab2);
	}

	// Loads the fragments contained in each tab, if they exist.
	private void loadTabFragments() {
		if (home_timeline == null) {
			home_timeline =
					(TweetsListFragment) getSupportFragmentManager()
							.findFragmentByTag(HOME_TIMELINE_TAG);
		}
		if (mentions_timeline == null) {
			mentions_timeline =
					(TweetsListFragment) getSupportFragmentManager()
							.findFragmentByTag(MENTIONS_TIMELINE_TAG);
		}
	}
}
