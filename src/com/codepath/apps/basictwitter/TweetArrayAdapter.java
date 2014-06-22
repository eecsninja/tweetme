package com.codepath.apps.basictwitter;

import java.util.List;

import com.codepath.apps.basictwitter.models.Tweet;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class TweetArrayAdapter extends ArrayAdapter<Tweet> {
	public TweetArrayAdapter(Context context, List<Tweet> tweet_list) {
		super(context, 0, tweet_list);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return super.getView(position, convertView, parent);
	}
}
