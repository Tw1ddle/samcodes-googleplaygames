package extension;

// Matches the Android constants (http://developer.android.com/reference/android/view/Gravity.html)
@:enum abstract HorizontalGravity(Int) {
	var LEFT = 3;
	var CENTER = 1;
	var RIGHT = 5;
}

@:enum abstract VerticalGravity(Int) {
	var BOTTOM = 80;
	var CENTER = 16;
	var TOP = 48;
}