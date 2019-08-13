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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
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

public class GooglePlayGames extends Extension {
	private static final String tag = "SamcodesGooglePlayGames";
	private static final int RC_SIGN_IN = 9001;
	private static final int REQUEST_SHOW_LEADERBOARDS = 100;
	private static final int REQUEST_SHOW_ACHIEVEMENTS = 101;

	private static final int SILENT_SIGNIN_FAILED_NEED_USER_LOGIN = 1000000; // Custom error code used when silent signin fails and the user needs to sign in/authenticate via the UI

	private static HaxeObject callback = null;
	
	public GooglePlayGames() {
		super();
		Log.d(tag, "Constructed SamcodesGooglePlayGames");
	}
	
	public static void setListener(HaxeObject haxeCallback) {
		Log.w(tag, "Setting GooglePlayGames listener");
		callback = haxeCallback;
	}
	
	public static boolean isGooglePlayServicesAvailable() {
		GoogleApiAvailability googlePlayServicesAvailability = GoogleApiAvailability.getInstance();
		
		if(googlePlayServicesAvailability == null) {
			Log.w(tag, "Failed to get Google Play Services availability singleton, will assume services are unavailable");
			return false;
		}
		
		return googlePlayServicesAvailability.isGooglePlayServicesAvailable(Extension.mainActivity) == ConnectionResult.SUCCESS;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.w(tag, "Resuming SamcodesGooglePlayGames");
		
		// Since the state of the signed-in player can change when
		// the activity is not in the foreground, we try to sign in
		// silently when the app is brought back to foreground (onResume also triggers when the activity is launched)
		if(!isGooglePlayServicesAvailable()) {
			Log.w(tag, "Will fail to sign in, Google Play Services unavailable");
			return;
		}
		
		signInSilently();
	}
	
	@Override
	public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.w(tag, "onActivityResult");
		callHaxe("onActivityResult", new Object[] { requestCode, resultCode });
		
		// Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
		if (requestCode == GooglePlayGames.RC_SIGN_IN) {
			// The Task returned from this call is always completed, no need to attach a listener.
			Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
			handleSignInResult(task);
		}
		
		return super.onActivityResult(requestCode, resultCode, data);
	}
	
	public static void signIn(boolean viaAuthDialogIfNecessary) {
		Log.w(tag, "signIn");
		
		if(!isGooglePlayServicesAvailable()) {
			Log.w(tag, "Will fail to sign in, Google Play Services unavailable");
			return;
		}
		
		if(isSignedIn()) {
			Log.w(tag, "Will not attempt to sign in, user already appears to be signed in");
			return;
		}
		
		if(viaAuthDialogIfNecessary) {
			signInViaDialogIfNecessary();
		} else {
			signInSilently();
		}
	}
	
	public static void signOut() {
		Log.w(tag, "signOut");
		
		if(!isGooglePlayServicesAvailable()) {
			Log.w(tag, "Will fail to sign out, Google Play Services unavailable");
			return;
		}
		
		GoogleSignInClient signInClient = getSignInClient();
		if(signInClient == null) {
			Log.w(tag, "Will fail to sign out, failed to get signin client");
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
		Log.w(tag, "isSignedIn");
		
		if(!isGooglePlayServicesAvailable()) {
			Log.w(tag, "Will fail to check sign in status, Google Play Services unavailable");
			return false;
		}
		
		GoogleSignInAccount account = getLastSignedInAccount();
		if(account == null) {
			return false;
		}
		
		GoogleSignInOptions signInOptions = GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN;
		if(!GoogleSignIn.hasPermissions(account, signInOptions.getScopeArray())) {
			return false;
		}
		
		return true;
	}
	
	public static void showAchievements() {
		Log.w(tag, "showAchievements");
		
		if(!isGooglePlayServicesAvailable()) {
			Log.w(tag, "Will fail show achievements, Google Play Services unavailable");
			return;
		}
		
		if(!isSignedIn()) {
			Log.w(tag, "Will fail to show achivements, not signed in");
			return;
		}
		
		AchievementsClient achievementsClient = getAchievementsClient();
		if(achievementsClient == null) {
			Log.w(tag, "Will fail to show achievements, failed to get achievements cient");
			return;
		}
		
		achievementsClient.getAchievementsIntent().addOnSuccessListener(new OnSuccessListener<Intent>() {
			@Override
			public void onSuccess(Intent intent) {
				try {
					Extension.mainActivity.startActivityForResult(intent, REQUEST_SHOW_ACHIEVEMENTS);
				} catch(Exception e) {
					Log.w(tag, "Failed to start achievements activity for reason: " + e.getMessage());
				}
			}
		}).addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(Exception e) {
				Log.w(tag, "Failed to show achievements for reason: " + e.getMessage());
			}
		});
	}
	
	public static void showLeaderboard(String id, int timespan) {
		Log.w(tag, "showLeaderboard");
		
		if(!isGooglePlayServicesAvailable()) {
			Log.w(tag, "Will fail show leaderboard, Google Play Services unavailable");
			return;
		}
		
		if(!isSignedIn()) {
			Log.w(tag, "Will fail to show leaderboard, not signed in");
			return;
		}
		
		LeaderboardsClient leaderboardsClient = getLeaderboardsClient();
		if(leaderboardsClient == null) {
			Log.w(tag, "Will fail to show leaderboard, failed to get leaderboards client");
			return;
		}
		
		leaderboardsClient.getLeaderboardIntent(id, timespan).addOnSuccessListener(new OnSuccessListener<Intent>() {
			@Override
			public void onSuccess(Intent intent) {
				try {
					Extension.mainActivity.startActivityForResult(intent, REQUEST_SHOW_LEADERBOARDS);
				catch(Exception e) {
					Log.w(tag, "Failed to start leaderboards activity for reason: " + e.getMessage());
				}
			}
		}).addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(Exception e) {
				Log.w(tag, "Failed to show leaderboard for reason: " + e.getMessage());
			}
		});
	}
	
	public static void showLeaderboards() {
		Log.w(tag, "showLeaderboards");
		
		if(!isGooglePlayServicesAvailable()) {
			Log.w(tag, "Will fail show leaderboards, Google Play Services unavailable");
			return;
		}
		
		if(!isSignedIn()) {
			Log.w(tag, "Will fail to show leaderboards, not signed in");
			return;
		}
		
		LeaderboardsClient leaderboardsClient = getLeaderboardsClient();
		if(leaderboardsClient == null) {
			Log.w(tag, "Will fail to show leaderboards, failed to get leaderboards client");
			return;
		}
		
		leaderboardsClient.getAllLeaderboardsIntent().addOnSuccessListener(new OnSuccessListener<Intent>() {
			@Override
			public void onSuccess(Intent intent) {
				Extension.mainActivity.startActivityForResult(intent, REQUEST_SHOW_LEADERBOARDS);
			}
		}).addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(Exception e) {
				Log.w(tag, "Failed to show leaderboards for reason: " + e.getMessage());
			}
		});
	}
	
	public static void incrementAchievement(String id, int numSteps) {
		Log.w(tag, "incrementAchievement");
		
		if(!isGooglePlayServicesAvailable()) {
			Log.w(tag, "Will fail increment achievement, Google Play Services unavailable");
			return;
		}
		
		if(numSteps <= 0) {
			Log.w(tag, "Will fail to increment achievement, number of steps is negative");
			return;
		}
		
		if(!isSignedIn()) {
			Log.w(tag, "Will fail to increment achievement, not signed in");
			return;
		}
		
		AchievementsClient achievementsClient = getAchievementsClient();
		
		if(achievementsClient == null) {
			Log.w(tag, "Will fail to increment achievement, failed to get achievements client");
			return;
		}
		
		achievementsClient.increment(id, numSteps);
	}
	
	public static void revealAchievement(String id) {
		Log.w(tag, "revealAchievement");
		
		if(!isGooglePlayServicesAvailable()) {
			Log.w(tag, "Will fail reveal achievement, Google Play Services unavailable");
			return;
		}
		
		if(!isSignedIn()) {
			Log.w(tag, "Will fail to reveal achievement, not signed in");
			return;
		}
		
		AchievementsClient achievementsClient = getAchievementsClient();
		
		if(achievementsClient == null) {
			Log.w(tag, "Will fail to increment achievement, failed to get achievements client");
			return;
		}
		
		achievementsClient.reveal(id);
	}
	
	public static void revealAchievementImmediate(String id) {
		Log.w(tag, "revealAchievementImmediate");
		
		if(!isGooglePlayServicesAvailable()) {
			Log.w(tag, "Will fail reveal achievement (immediate), Google Play Services unavailable");
			return;
		}
		
		if(!isSignedIn()) {
			Log.w(tag, "Will fail to reveal achievement (immediate), not signed in");
			return;
		}
		
		AchievementsClient achievementsClient = getAchievementsClient();
		
		if(achievementsClient == null) {
			Log.w(tag, "Will fail to increment achievement, failed to get achievements client");
			return;
		}
		
		achievementsClient.revealImmediate(id);
	}
	
	public static void setAchievementSteps(String id, int numSteps) {
		Log.w(tag, "setAchievementSteps");
		
		if(!isGooglePlayServicesAvailable()) {
			Log.w(tag, "Will fail set achievement steps, Google Play Services unavailable");
			return;
		}
		
		if(numSteps <= 0) {
			Log.w(tag, "Will fail to increment achievement, number of steps is negative");
			return; // NOTE throws java.lang.IllegalStateException: Number of steps must be greater than 0 otherwise
		}
		
		if(!isSignedIn()) {
			Log.w(tag, "Will fail to set achievement steps, not signed in");
			return;
		}
		
		AchievementsClient achievementsClient = getAchievementsClient();
		
		if(achievementsClient == null) {
			Log.w(tag, "Will fail to increment achievement, failed to get achievements client");
			return;
		}
		
		achievementsClient.setSteps(id, numSteps);
	}
	
	public static void unlockAchievement(String id) {
		Log.w(tag, "unlockAchievement");
		
		if(!isGooglePlayServicesAvailable()) {
			Log.w(tag, "Will fail set unlock achievement, Google Play Services unavailable");
			return;
		}
		
		if(!isSignedIn()) {
			Log.w(tag, "Will fail to unlock achievement, not signed in");
			return;
		}
		
		AchievementsClient achievementsClient = getAchievementsClient();
		
		if(achievementsClient == null) {
			Log.w(tag, "Will fail to unlock achievement, failed to get achievements client");
			return;
		}
		
		achievementsClient.unlock(id);
	}
	
	public static void submitScore(String leaderboardId, int score, String scoreTag) {
		Log.w(tag, "submitScore");
		
		if(!isGooglePlayServicesAvailable()) {
			Log.w(tag, "Will fail to submit score, Google Play Services unavailable");
			return;
		}
		
		if(!isSignedIn()) {
			Log.w(tag, "Will fail to submit score, not signed in");
			return;
		}
		
		LeaderboardsClient leaderboardsClient = getLeaderboardsClient();
		if(leaderboardsClient == null) {
			Log.w(tag, "Will fail to submit score, failed to get leaderboards client");
			return;
		}
		
		if(scoreTag == null || scoreTag.equals("")) {
			leaderboardsClient.submitScore(leaderboardId, score);
		} else {
			leaderboardsClient.submitScore(leaderboardId, score, scoreTag);
		}
	}
	
	public static void setGravityForPopups(int horizontalGravity, int verticalGravity) {
		Log.w(tag, "setGravityForPopups");
		
		if(!isGooglePlayServicesAvailable()) {
			Log.w(tag, "Will fail to set gravity for popups, Google Play Services unavailable");
			return;
		}
		
		if(!isSignedIn()) {
			Log.w(tag, "Will fail to set gravity for popups, not signed in");
			return;
		}
		
		GamesClient gamesClient = getGamesClient();
		
		if(gamesClient == null) {
			Log.w(tag, "Failed to get games client, will fail to set gravity for popups");
			return;
		}
		
		gamesClient.setGravityForPopups(horizontalGravity | verticalGravity);
	}
	
	private static void signInSilently() {
		Log.w(tag, "signInSilently");
		
		if(!isGooglePlayServicesAvailable()) {
			Log.w(tag, "Will fail to sign in silently, Google Play Services unavailable");
			return;
		}
		
		if(isSignedIn()) {
			Log.w(tag, "Will not attempt to sign in, user already appears to be signed in");
			return;
		}
		
		GoogleSignInClient signInClient = getSignInClient();
		if(signInClient == null) {
			Log.w(tag, "Will fail to sign in silently, failed to get sign in client");
			return;
		}
		
		signInClient.silentSignIn().addOnCompleteListener(Extension.mainActivity, new OnCompleteListener<GoogleSignInAccount>() {
			@Override
			public void onComplete(Task<GoogleSignInAccount> task) {
				if (task.isSuccessful()) {
					callHaxe("onConnected", new Object[] {});
				} else {
					// Player will need to sign-in explicitly via the login UI
					callHaxe("onConnectionFailed", new Object[]{ new Integer(GooglePlayGames.SILENT_SIGNIN_FAILED_NEED_USER_LOGIN) });
				}
			}
		});
	}
	
	private static void signInViaDialogIfNecessary() {
		Log.w(tag, "signInViaDialogIfNecessary");
		
		if(!isGooglePlayServicesAvailable()) {
			Log.w(tag, "Will fail to sign in (via dialog if necessary), Google Play Services unavailable");
			return;
		}
		
		if(isSignedIn()) {
			Log.w(tag, "Will not attempt to sign in, user already appears to be signed in");
			return;
		}
		
		GoogleSignInClient signInClient = getSignInClient();
		if(signInClient == null) {
			Log.w(tag, "Will fail to sign in (via dialog if necessary), failed to get sign in client");
			return;
		}
		
		signInClient.silentSignIn().addOnCompleteListener(Extension.mainActivity, new OnCompleteListener<GoogleSignInAccount>() {
			@Override
			public void onComplete(Task<GoogleSignInAccount> task) {
				if (task.isSuccessful()) {
					callHaxe("onConnected", new Object[] {});
				} else {
					startSignInIntent();
				}
			}
		});
	}
	
	private static void startSignInIntent() {
		Log.w(tag, "startSignInIntent");
		
		if(!isGooglePlayServicesAvailable()) {
			Log.w(tag, "Will fail to start signin intent, Google Play Services unavailable");
			return;
		}
		
		if(isSignedIn()) {
			Log.w(tag, "Will not start sign in intent, user already appears to be signed in");
			return;
		}
		
		GoogleSignInClient signInClient = getSignInClient();
		if(signInClient == null) {
			Log.w(tag, "Will fail to sign in silently, failed to get sign in client");
			return;
		}
		
		Intent intent = signInClient.getSignInIntent();
		if(intent == null) {
			Log.w(tag, "Failed to get sign-in intent");
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
	
	private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
		if(completedTask == null) {
			Log.w(tag, "Sign in result returned a null task. This should never happen");
			return;
		}
		
		try {
			GoogleSignInAccount account = completedTask.getResult(ApiException.class);
			
			callHaxe("onConnected", new Object[] {});
			
		} catch (ApiException e) {
			if(e == null) {
				Log.w(tag, "ApiException was null, will fail to report sign in failure");
			}
			
			// The ApiException status code indicates the detailed failure reason.
			// Refer to the GoogleSignInStatusCodes class reference for more information.
			// Note, in principle this could also be one of the other CommonStatusCodes.
			Log.w(tag, "Sign in result contained a failure code of: " + e.getStatusCode());
			
			int statusCode = (int)(e.getStatusCode());
			
			callHaxe("onConnectionFailed", new Object[] { statusCode });
		}
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