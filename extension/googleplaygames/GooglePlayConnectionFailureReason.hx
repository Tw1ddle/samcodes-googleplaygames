package extension.googleplaygames;

// Matches values at the top of GooglePlayGames.java
@:enum abstract GooglePlayConnectionFailureReason(Int) from Int to Int
{
	var SilentSignInFailedNeedUserLogin = 0;
}