package com.codepath.apps.basictwitter.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.apps.basictwitter.R;

public class TweetsListFragment extends Fragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate layout.
		View view = inflater.inflate(
				R.layout.fragment_tweets_list, container, false);
		// Assign view references.
		// TODO
		// Return it!
		return view;
	}
}
