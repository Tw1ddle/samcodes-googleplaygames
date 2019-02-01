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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.AnnotatedData;
import com.google.android.gms.games.EventsClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.games.event.Event;
import com.google.android.gms.games.event.EventBuffer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import android.app.Activity;

public class GooglePlayGames extends Extension {
	private static final String tag = "SamcodesGooglePlayGames";
	private static final int RC_SIGN_IN = 9001;
	private static final int REQUEST_SHOW_LEADERBOARDS = 100;
	private static final int REQUEST_SHOW_ACHIEVEMENTS = 101;

	private static HaxeObject callback = null;
	
	public GooglePlayGames() {
		super();
		Log.d(tag, "Constructed SamcodesGooglePlayGames");
	}
	
	public static void setListener(HaxeObject haxeCallback) {
		Log.i(tag, "Setting GooglePlayGames listener");
		callback = haxeCallback;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.i(tag, "Resuming SamcodesGooglePlayGames");
		
		// Since the state of the signed-in player can change when
		// the activity is not in the foreground, we try to sign in
		// silently when the app is brought back to foreground (onResume also triggers when the activity is launched)
		signInSilently();
	}
	
	@Override
	public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i(tag, "onActivityResult");
		callHaxe("onActivityResult", new Object[] { requestCode, resultCode });
		
		return super.onActivityResult(requestCode, resultCode, data);
	}
	
	public static void signIn() {
		Log.i(tag, "signIn");
		
		signInSilently();
	}
	
	public static void signOut() {
		Log.i(tag, "signOut");
		
		GoogleSignInClient signInClient = GoogleSignIn.getClient(Extension.mainActivity, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
		if(signInClient == null) {
			Log.w(tag, "Failed to get GoogleSignInClient");
			return;
		}
		
		signInClient.signOut().addOnCompleteListener(Extension.mainActivity, new OnCompleteListener<Void>() {
			@Override
			public void onComplete(Task<Void> task) {
				callHaxe("onDisconnected", new Object[] {});
			}
		});
	}
	
	public static boolean isSignedIn() {
		Log.i(tag, "isSignedIn");
		
		return getLastSignedInAccount() != null;
	}
	
	public static void showAchievements() {
		Log.i(tag, "showAchievements");
		
		if(!isSignedIn()) {
			return;
		}
		
		AchievementsClient achievementsClient = getAchievementsClient();
		if(achievementsClient == null) {
			return;
		}
		
		Task<Intent> achievementsIntent = achievementsClient.getAchievementsIntent();
		if(achievementsIntent == null) {
			return;
		}
		
		achievementsIntent.addOnSuccessListener(new OnSuccessListener<Intent>() {
			@Override
			public void onSuccess(Intent intent) {
				Extension.mainActivity.startActivityForResult(intent, REQUEST_SHOW_ACHIEVEMENTS);
			}
		});
	}
	
	public static void showLeaderboard(String id, int timespan) {
		Log.i(tag, "showLeaderboard");
		
		if(!isSignedIn()) {
			return;
		}
		
		LeaderboardsClient leaderboardsClient = getLeaderboardsClient();
		if(leaderboardsClient == null) {
			return;
		}
		
		Task<Intent> leaderboardIntent = leaderboardsClient.getLeaderboardIntent(id, timespan);
		if(leaderboardIntent == null) {
			return;
		}
		
		leaderboardIntent.addOnSuccessListener(new OnSuccessListener<Intent>() {
			@Override
			public void onSuccess(Intent intent) {
				Extension.mainActivity.startActivityForResult(intent, REQUEST_SHOW_LEADERBOARDS);
			}
		});
	}
	
	public static void showLeaderboards() {
		Log.i(tag, "showLeaderboards");
		
		if(!isSignedIn()) {
			return;
		}
		
		LeaderboardsClient leaderboardsClient = getLeaderboardsClient();
		if(leaderboardsClient == null) {
			return;
		}
		
		Task<Intent> leaderboardIntent = leaderboardsClient.getAllLeaderboardsIntent();
		if(leaderboardIntent == null) {
			return;
		}
		
		leaderboardIntent.addOnSuccessListener(new OnSuccessListener<Intent>() {
			@Override
			public void onSuccess(Intent intent) {
				Extension.mainActivity.startActivityForResult(intent, REQUEST_SHOW_LEADERBOARDS);
			}
		});
	}
	
	public static void incrementAchievement(String id, int numSteps) {
		Log.i(tag, "incrementAchievement");
		
		if(numSteps <= 0) {
			return;
		}
		
		if(!isSignedIn()) {
			return;
		}
		
		AchievementsClient achievementsClient = getAchievementsClient();
		
		if(achievementsClient == null) {
			return;
		}
		
		achievementsClient.increment(id, numSteps);
	}
	
	public static void revealAchievement(String id) {
		Log.i(tag, "revealAchievement");
		
		if(!isSignedIn()) {
			return;
		}
		
		AchievementsClient achievementsClient = getAchievementsClient();
		
		if(achievementsClient == null) {
			return;
		}
		
		achievementsClient.reveal(id);
	}
	
	public static void revealAchievementImmediate(String id) {
		Log.i(tag, "revealAchievementImmediate");
		
		if(!isSignedIn()) {
			return;
		}
		
		AchievementsClient achievementsClient = getAchievementsClient();
		
		if(achievementsClient == null) {
			return;
		}
		
		achievementsClient.revealImmediate(id);
	}
	
	public static void setAchievementSteps(String id, int numSteps) {
		Log.i(tag, "setAchievementSteps");
		
		if(numSteps <= 0) {
			return; // NOTE throws java.lang.IllegalStateException: Number of steps must be greater than 0 otherwise
		}
		
		if(!isSignedIn()) {
			return;
		}
		
		AchievementsClient achievementsClient = getAchievementsClient();
		
		if(achievementsClient == null) {
			return;
		}
		
		achievementsClient.setSteps(id, numSteps);
	}
	
	public static void unlockAchievement(String id) {
		Log.i(tag, "unlockAchievement");
		
		if(!isSignedIn()) {
			return;
		}
		
		AchievementsClient achievementsClient = getAchievementsClient();
		
		if(achievementsClient == null) {
			return;
		}
		
		achievementsClient.unlock(id);
	}
	
	public static void submitScore(String leaderboardId, int score, String scoreTag) {
		Log.i(tag, "submitScore");
		
		if(!isSignedIn()) {
			return;
		}
		
		LeaderboardsClient leaderboardsClient = getLeaderboardsClient();
		if(leaderboardsClient == null) {
			return;
		}
		
		if(scoreTag == null || scoreTag.equals("")) {
			leaderboardsClient.submitScore(leaderboardId, score);
		} else {
			leaderboardsClient.submitScore(leaderboardId, score, scoreTag);
		}
	}
	
	public static void setGravityForPopups(int horizontalGravity, int verticalGravity) {
		Log.i(tag, "setGravityForPopups");
		
		if(!isSignedIn()) {
			Log.i(tag, "Not signed in, will fail to set gravity for popups");
		}
		
		GamesClient gamesClient = getGamesClient();
		
		if(gamesClient == null) {
			Log.i(tag, "Failed to get games client, will fail to set gravity for popups");
		}
		
		gamesClient.setGravityForPopups(horizontalGravity | verticalGravity);
	}
	
	public static void signInSilently() {
		Log.i(tag, "signInSilently");
		
		GoogleSignInClient signInClient = getSignInClient();
		if(signInClient == null) {
			Log.w(tag, "Failed to get sign in client");
			return;
		}
		
		signInClient.silentSignIn().addOnCompleteListener(Extension.mainActivity, new OnCompleteListener<GoogleSignInAccount>() {
			@Override
			public void onComplete(Task<GoogleSignInAccount> task) {
				if (task.isSuccessful()) {
					callHaxe("onConnected", new Object[] {});
				} else {
					// Player will need to sign-in explicitly via the login UI
					startSignInIntent();
				}
			}
		});
	}
	
	public static void startSignInIntent() {
		Log.i("startSignInIntent");
		
		GoogleSignInClient signInClient = getSignInClient();
		if(signInClient == null) {
			Log.i(tag, "Failed to get sign in client");
			return;
		}
		
		Intent intent = signInClient.getSignInIntent();
		if(intent == null) {
			Log.i(tag, "Failed to get sign-in intent");
			return;
		}
		
		Extension.mainActivity.startActivityForResult(intent, RC_SIGN_IN);
	}
	
	private static void callHaxe(final String name, final Object[] args) {
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
	
	private static GoogleSignInAccount getLastSignedInAccount() {
		return GoogleSignIn.getLastSignedInAccount(Extension.mainActivity);
	}
	
	private static GoogleSignInClient getSignInClient() {
		return GoogleSignIn.getClient(Extension.mainActivity, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
	}
	
	private static GamesClient getGamesClient() {
		return Games.getGamesClient(Extension.mainActivity, getLastSignedInAccount());
	}
	
	private static LeaderboardsClient getLeaderboardsClient() {
		return Games.getLeaderboardsClient(Extension.mainActivity, getLastSignedInAccount());
	}
	
	private static AchievementsClient getAchievementsClient() {
		return Games.getAchievementsClient(Extension.mainActivity, getLastSignedInAccount());
	}
}