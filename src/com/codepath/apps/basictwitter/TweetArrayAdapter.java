package com.codepath.apps.basictwitter;

import java.util.List;

import com.codepath.apps.basictwitter.models.Tweet;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TweetArrayAdapter extends ArrayAdapter<Tweet> {
	public TweetArrayAdapter(Context context, List<Tweet> tweet_list) {
		super(context, 0, tweet_list);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// Find or inflate the template.
		View view;
		if (convertView == null) {
			view = LayoutInflater.from(getContext())
					.inflate(R.layout.tweet_item, parent, false);
		} else {
			view = convertView;
		}
		// Find views within template.
		ImageView profile_image =
				(ImageView) view.findViewById(R.id.ivProfileImage);
		TextView user_name_field =
				(TextView) view.findViewById(R.id.tvUserName);
		TextView tweet_body_field =
				(TextView) view.findViewById(R.id.tvBody);
		// Set view properties.
		profile_image.setImageResource(android.R.color.transparent);
		ImageLoader image_loader = ImageLoader.getInstance();
		Tweet tweet = getItem(position);
		image_loader.displayImage(tweet.getUser().getProfileImageUrl(),
								  profile_image);
		user_name_field.setText(tweet.getUser().getName());
		tweet_body_field.setText(tweet.getBody());

		return view;
	}
}
