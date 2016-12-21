package com.samcodes.googleplaygames;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.opengl.GLSurfaceView;
import java.util.ArrayList;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import org.haxe.lime.HaxeObject;
import org.haxe.extension.Extension;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.*;
import com.google.android.gms.games.*;

import android.app.Activity;

public class GooglePlayGames extends Extension implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
	private static final String tag = "SamcodesGooglePlayGames";
	private static final int RC_SIGN_IN = 9001;
	private static final int REQUEST_SHOW_LEADERBOARDS = 100;
	private static final int REQUEST_SHOW_ACHIEVEMENTS = 101;
	
	private static HaxeObject callback = null;
	
	private static GoogleApiClient googleApiClient = null; // Initialized in onCreate
	private boolean autoSignIn = true; // Whether to sign in automatically on launch
	private boolean resolvingConnectionFailure = false; // Set to true when you're in the middle of the sign in flow, to know you should not attempt to connect in onStart()
	
	public GooglePlayGames() {
		Log.d(tag, "Constructed SamcodesGooglePlayGames");
	}
	
	public static void setListener(HaxeObject haxeCallback) {
		Log.i(tag, "Setting GooglePlayGames listener");
		callback = haxeCallback;
	}
	
	public static void callHaxe(final String name, final Object[] args) {
		if (callback == null) {
			Log.d(tag, "Would have called " + name + " from java but did not because no GooglePlayGames listener was installed");
			return;
		}
		
		callbackHandler.post(new Runnable() {
			public void run() {
				Log.d(tag, "Calling " + name + " from java");
				callback.call(name, args);
			}
		});
	}
	
	public void onStart() {
		super.onStart();
		Log.i(tag, "Starting SamcodesGooglePlayGames");
		
		if(autoSignIn && !resolvingConnectionFailure) {
			googleApiClient.connect();
		}
	}

	public void onStop() {
		Log.i(tag, "Stopping SamcodesGooglePlayGames");
		googleApiClient.disconnect();
		super.onStop();
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		googleApiClient = new GoogleApiClient.Builder(mainActivity).addApi(Games.API).addScope(Games.SCOPE_GAMES).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
	}
	
	@Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i(tag, "onActivityResult");
		callHaxe("onActivityResult", new Object[] { requestCode, resultCode });
		
		return super.onActivityResult(requestCode, resultCode, data);
    }
	
    @Override
    public void onConnected(Bundle connectionHint) {
		Log.i(tag, "onConnected");
		callHaxe("onConnected", new Object[] {});
    }

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		if (resolvingConnectionFailure) {
			return;
		}
		
		Log.i(tag, "onConnectionFailed");
		callHaxe("onConnectionFailed", new Object[] { connectionResult.getErrorCode() });
	}

	@Override
	public void onConnectionSuspended(int i) {
		Log.i(tag, "onConnectionSuspended");
		callHaxe("onConnectionSuspended", new Object[] { i });
		googleApiClient.connect();
	}
	
	private static boolean checkClient() {
		if(googleApiClient == null) {
			Log.w(tag, "googleApiClient is null");
			return false;
		}
		
		if(!googleApiClient.hasConnectedApi(Games.API)) {
			Log.w(tag, "googleApiClient is not connected to the games service");
			return false;
		}
		
		return true;
	}
	
	public static void showAchievements() {
		Log.i(tag, "showAchievements");
		
		if(!checkClient()) {
			return;
		}
		
		mainActivity.startActivityForResult(Games.Achievements.getAchievementsIntent(googleApiClient), REQUEST_SHOW_ACHIEVEMENTS);
	}
	
	public static void showLeaderboard(String id, int timespan) {
		Log.i(tag, "showLeaderboard");
		
		if(!checkClient()) {
			return;
		}
		
		mainActivity.startActivityForResult(Games.Leaderboards.getLeaderboardIntent(googleApiClient, id, timespan), REQUEST_SHOW_LEADERBOARDS);
	}
	
	public static void showLeaderboards() {
		Log.i(tag, "showLeaderboards");
		
		if(!checkClient()) {
			return;
		}
		
		mainActivity.startActivityForResult(Games.Leaderboards.getAllLeaderboardsIntent(googleApiClient), REQUEST_SHOW_LEADERBOARDS);
	}
	
	public static void incrementAchievement(String id, int numSteps) {
		Log.i(tag, "incrementAchievement");
		
		if(numSteps <= 0) {
			return;
		}
		
		if(!checkClient()) {
			return;
		}
		
		Games.Achievements.increment(googleApiClient, id, numSteps);
	}
	
	public static void revealAchievement(String id) {
		Log.i(tag, "revealAchievement");
		
		if(!checkClient()) {
			return;
		}
		
		Games.Achievements.reveal(googleApiClient, id);
	}
	
	public static void revealAchievementImmediate(String id) {
		Log.i(tag, "revealAchievementImmediate");
		
		if(!checkClient()) {
			return;
		}
		
		Games.Achievements.revealImmediate(googleApiClient, id);
	}
	
	public static void setAchievementSteps(String id, int numSteps) {
		Log.i(tag, "setAchievementSteps");
		
		if(numSteps <= 0) {
			return; // NOTE throws java.lang.IllegalStateException: Number of steps must be greater than 0 otherwise
		}
		
		if(!checkClient()) {
			return;
		}
		
		Games.Achievements.setSteps(googleApiClient, id, numSteps);
	}
	
	public static void unlockAchievement(String id) {
		Log.i(tag, "unlockAchievement");
		
		if(!checkClient()) {
			return;
		}
		
		Games.Achievements.unlock(googleApiClient, id);
	}
	
	public static void submitScore(String leaderboardId, int score, String scoreTag) {
		Log.i(tag, "submitScore");
		
		if(!checkClient()) {
			return;
		}
		
		if(scoreTag == null || scoreTag.equals("")) {
			Games.Leaderboards.submitScore(googleApiClient, leaderboardId, score);
		} else {
			Games.Leaderboards.submitScore(googleApiClient, leaderboardId, score, scoreTag);
		}
	}
	
	public static void connect() {
		Log.i(tag, "connect");
		
		if(googleApiClient == null) {
			Log.i(tag, "Could not connect because googleApiClient was null");
			return;
		}
		
		if(googleApiClient.hasConnectedApi(Games.API)) {
			Log.i(tag, "Could not connect because googleApiClient was already connected to the games API");
			return;
		}
		
		googleApiClient.connect();
	}
	
	public static void disconnect() {
		Log.i(tag, "disconnect");
		
		if(googleApiClient == null) {
			Log.i(tag, "Could not disconnect because googleApiClient was null");
			return;
		}
		
		if(!googleApiClient.hasConnectedApi(Games.API)) {
			Log.i(tag, "Could not disconnect because googleApiClient was not connected to the games API");
			return;
		}
		
		googleApiClient.disconnect();
	}
	
	public static void reconnect() {
		Log.i(tag, "reconnect");
		if(!checkClient()) {
			return;
		}
		
		googleApiClient.reconnect();
	}
	
	public static boolean isConnected() {
		Log.i(tag, "isConnected");
		return checkClient();
	}
	
	public static void setGravityForPopups(int horizontalGravity, int verticalGravity) {
		Log.i(tag, "setGravityForPopups");
		
		if(googleApiClient == null) {
			Log.i(tag, "Could not set popup gravity because googleApiClient was null");
			return;
		}
		
		Games.setGravityForPopups(googleApiClient, horizontalGravity | verticalGravity);
	}
	
	public static String getCurrentAccountName() {
		Log.i(tag, "getCurrentAccountName");
		
		if(!checkClient()) {
			Log.i(tag, "Failed to get current account name, returning empty string");
			return "";
		}
		
		String currentAccountName = Games.getCurrentAccountName(googleApiClient);
		
		if(currentAccountName == null) {
			Log.i(tag, "Failed to get current account name, returning empty string");
			return "";
		}
		
		return currentAccountName;
	}
}