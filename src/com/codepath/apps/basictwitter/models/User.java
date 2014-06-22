package com.codepath.apps.basictwitter.models;

import org.json.JSONException;
import org.json.JSONObject;

public class User {
	private String name;
	private long id;
	private String screen_name;
	private String profile_image_url;

	public static User fromJSON(JSONObject object) {
		User user = new User();
		try {
			user.name = object.getString("name");
			user.id = object.getLong("id");
			user.screen_name = object.getString("screen_name");
			user.profile_image_url = object.getString("profile_image_url");
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return user;
	}

	public String getName() {
		return name;
	}

	public long getId() {
		return id;
	}

	public String getScreenName() {
		return screen_name;
	}

	public String getProfileImageUrl() {
		return profile_image_url;
	}
}
