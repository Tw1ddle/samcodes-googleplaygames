package com.samcodes.googleplaygames

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameUtils;

import android.app.Activity;

public class GooglePlayGames extends Extension implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
	private static final String tag = "SamcodesGooglePlayGames";
	private static final int RC_SIGN_IN = 9001;
	
	private static HaxeObject callback = null;
	
	private GoogleApiClient googleApiClient = null; // Initialized in onCreate
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
	
	/**
	 * Called after {@link #onCreate} &mdash; or after {@link #onRestart} when the activity had been stopped, but is now again being displayed to the user.
	 */
	public void onStart() {
		super.onStart();
		Log.i(tag, "Starting SamcodesGooglePlayGames");
		
		if(autoSignIn) {
			googleApiClient.connect();
		}
	}

	/**
	 * Called after {@link #onStop} when the current activity is being re-displayed to the user (the user has navigated back to it).
	 */
	public void onRestart() {
		super.onRestart();
	}

	/**
	 * Called when the activity is no longer visible to the user, because another activity has been resumed and is covering this one.
	 */
	public void onStop() {
		super.onStop();
		Log.i(tag, "Stopping SamcodesGooglePlayGames");
		googleApiClient.disconnect();
	}

	/**
	 * Called when the activity is starting.
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		googleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(Games.API).addScope(Games.SCOPE_GAMES).build();
	}

	/**
	 * Perform any final cleanup before an activity is destroyed.
	 */
	public void onDestroy() {
		super.onDestroy();
	}

	/**
	 * Called as part of the activity lifecycle when an activity is going into the background, but has not (yet) been killed.
	 */
	public void onPause() {
		super.onPause();
	}

	/**
	 * Called after {@link #onRestart}, or {@link #onPause}, for your activity to start interacting with the user.
	 */
	public void onResume() {
		super.onResume();
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
		callHaxe("onConnectionFailed", new Object[ connectionResult.getErrorCode() ] {});

		// If auto sign-in is enabled, launch the sign-in flow
		if (autoSignIn) {
			autoSignIn = false;
			resolvingConnectionFailure = true;

			// Attempt to resolve the connection failure using BaseGameUtils.
			// The string.signin_other_error value references a generic error string in the strings.xml file.
			if (!BaseGameUtils.resolveConnectionFailure(this, googleApiClient, connectionResult, RC_SIGN_IN, "signin_other_error")) { // R.string.signin_other_error
				resolvingConnectionFailure = false;
				Log.i(tag, "onFailedToResolveConnectionFailure");
				callHaxe("onFailedToResolveConnectionFailure", new Object[] {});
			}
		}
	}

	@Override
	public void onConnectionSuspended(int i) {
		Log.i(tag, "onConnectionSuspended");
		googleApiClient.connect();
	}
}