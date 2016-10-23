package extension.googleplaygames;

// Matches the Android constants (http://developer.android.com/reference/android/view/Gravity.html)
@:enum abstract LeaderboardTimespan(Int) {
	var DAILY = 0;
	var WEEKLY = 1;
	var ALL_TIME = 2;
}