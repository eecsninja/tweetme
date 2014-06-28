package com.codepath.apps.basictwitter.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;

import com.codepath.apps.basictwitter.R;

public class TimelineActivity extends FragmentActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timeline);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Set up menu.
		// TODO: This currently causes an exception, figure out why.
		// getMenuInflater().inflate(R.menu.compose, menu);
		return true;
	}
}
