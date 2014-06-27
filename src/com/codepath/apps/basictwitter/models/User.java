package com.codepath.apps.basictwitter.models;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = "User")
public class User extends Model implements Serializable {
	// Serialization ID.
	// TODO: Try Parcelable instead.
	private static final long serialVersionUID = -7455940383615647225L;

	@Column(name = "remote_id", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
	private long uid;
	@Column(name = "name")
	private String name;
	@Column(name = "screen_name")
	private String screen_name;
	@Column(name = "image_url")
	private String profile_image_url;

	// The default constructor is required for ActiveAndroid's Model class.
	public User() {
		super();
	}

	public static User fromJSON(JSONObject object) {
		User user = new User();
		try {
			user.name = object.getString("name");
			user.uid = object.getLong("id");
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

	public long getUniqueId() {
		return uid;
	}

	public String getScreenName() {
		return screen_name;
	}

	public String getProfileImageUrl() {
		return profile_image_url;
	}
}
