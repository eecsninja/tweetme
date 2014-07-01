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

	// Removing this for now due to an issue with Tweets containing this User
	// being deleted from the DB when this is overwritten.
	// @Column(name = "remote_id", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
	private long uid;
	@Column(name = "name")
	private String name;
	@Column(name = "screen_name")
	private String screen_name;
	@Column(name = "image_url")
	private String profile_image_url;

	private int num_followers = 0;
	private int num_following = 0;
	private String tagline = "";

	// The default constructor is required for ActiveAndroid's Model class.
	public User() {
		super();
	}

	public User(long uid, String name, String screen_name, String profile_image_url) {
		this.uid = uid;
		this.name = name;
		this.screen_name = screen_name;
		this.profile_image_url = profile_image_url;
	}

	public static User fromJSON(JSONObject object) {
		User user = new User();
		try {
			user.name = object.getString("name");
			user.uid = object.getLong("id");
			user.screen_name = object.getString("screen_name");
			user.profile_image_url = object.getString("profile_image_url");
			user.num_followers = object.getInt("followers_count");
			user.num_following = object.getInt("friends_count");
			user.tagline = object.getString("description");
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

	public int getNumFollowers() {
		return num_followers;
	}

	public int getNumFollowing() {
		return num_following;
	}

	public String getTagline() {
		return tagline;
	}
}
