package com.codepath.apps.basictwitter.helpers;

import android.app.Activity;
import android.content.SharedPreferences;

import com.codepath.apps.basictwitter.models.User;

public class StoredAccountInfo {
	// Storage name of current user info in shared preferences.
	public static final String CURRENT_USER_SHARED_PREF_KEY = "CurrentUser";

	// Store user info in shared preferences.
	public static void storeUserInfo(Activity activity, User user) {
		SharedPreferences settings =
				activity.getSharedPreferences(CURRENT_USER_SHARED_PREF_KEY, 0);
		SharedPreferences.Editor editor = settings.edit();
		// Store all main user fields.
		editor.putLong("id", user.getUniqueId());
		editor.putString("user_name", user.getName());
		editor.putString("screen_name", user.getScreenName());
		editor.putString("profile_image_url", user.getProfileImageUrl());
		editor.commit();
	}

	// Load user info from shared preferences.
	public static User loadUserInfo(Activity activity) {
		SharedPreferences settings =
				activity.getSharedPreferences(CURRENT_USER_SHARED_PREF_KEY, 0);
		User user = new User(
				settings.getLong("id", -1),
				settings.getString("user_name", "unknown"),
				settings.getString("screen_name", "unknown"),
				settings.getString("profile_image_url", "unknown"));
		if (user.getScreenName() != "unknown") {
			return user;
		}
		return null;
	}
}
