package com.codepath.apps.basictwitter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import com.codepath.apps.basictwitter.models.Tweet;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.content.Context;
import android.text.format.DateUtils;
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
		TextView timestamp_field =
				(TextView) view.findViewById(R.id.tvTimeAgo);
		// Set view properties.
		profile_image.setImageResource(android.R.color.transparent);
		ImageLoader image_loader = ImageLoader.getInstance();
		Tweet tweet = getItem(position);
		image_loader.displayImage(tweet.getUser().getProfileImageUrl(),
								  profile_image);
		user_name_field.setText(tweet.getUser().getName());
		tweet_body_field.setText(tweet.getBody());
		timestamp_field.setText(getRelativeTimeAgo(tweet.getTimestamp()));

		return view;
	}

	// Returns relative time label, given an absolute timestamp string.
	private String getRelativeTimeAgo(String timestamp) {
		final String TWITTER_FORMAT = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
		SimpleDateFormat format =
				new SimpleDateFormat(TWITTER_FORMAT, Locale.ENGLISH);
		format.setLenient(true);
		String relative_date = "";
		try {
			long date_ms = format.parse(timestamp).getTime();
			relative_date = DateUtils.getRelativeTimeSpanString(
					date_ms,
					System.currentTimeMillis(),
					DateUtils.SECOND_IN_MILLIS).toString();
		} catch (ParseException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		return relative_date;
	}
}
