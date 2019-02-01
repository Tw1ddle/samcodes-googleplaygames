package extension.googleplaygames;

@:enum abstract GooglePlayConnectionFailureReason(Int) from Int to Int
{
	var SilentSignInFailedNeedUserLogin = 1000000; // Matches value at the top of GooglePlayGames.java
	
	// Matches GoogleSignInStatusCodes constants
	var SignInFailed = 12500;
	var SignInCancelledByUser = 12501;
	var SignInAlreadyInProgress = 12502;
}