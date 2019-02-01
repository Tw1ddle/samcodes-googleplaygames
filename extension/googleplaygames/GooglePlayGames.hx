package extension.googleplaygames;

#if android

import extension.googleplaygames.GooglePlayGravity;
import extension.googleplaygames.GooglePlayLeaderboardTimespan;
import extension.googleplaygames.GooglePlayListener;

import lime.system.JNI;

class GooglePlayGames {
	public static function init() {
	}
	
	public static function setListener(listener:GooglePlayListener):Void {
		set_listener(listener);
	}
	
	public static function showLeaderboard(id:String, timespan:LeaderboardTimespan):Void {
		show_leaderboard(id, timespan);
	}
	
	public static function showLeaderboards():Void {
		show_leaderboards();
	}
	
	public static function showAchievements():Void {
		show_achievements();
	}
	
	public static function incrementAchievement(id:String, numSteps:Int):Void {
		increment_achievement(id, numSteps);
	}
	
	public static function revealAchievement(id:String):Void {
		reveal_achievement(id);
	}
	
	public static function revealAchievementImmediate(id:String):Void {
		reveal_achievement_immediate(id);
	}
	
	public static function setAchievementSteps(id:String, numSteps:Int):Void {
		set_achievement_steps(id, numSteps);
	}
	
	public static function unlockAchievement(id:String):Void {
		unlock_achievement(id);
	}
	
	public static function submitScore(leaderboardId:String, score:Int, ?scoreTag:String = ""):Void {
		submit_score(leaderboardId, score, scoreTag);
	}
	
	public static function signIn():Void {
		sign_in();
	}
	
	public static function signOut():Void {
		sign_out();
	}
	
	public static function isSignedIn():Bool {
		return is_signed_in();
	}
	
	public static function setGravityForPopups(horizontalGravity:HorizontalGravity, verticalGravity:VerticalGravity):Void {
		set_gravity_for_popups(horizontalGravity, verticalGravity);
	}
	
	private static inline var packageName:String = "com/samcodes/googleplaygames/GooglePlayGames";
	private static inline function bindJNI(jniMethod:String, jniSignature:String) {
		return JNI.createStaticMethod(packageName, jniMethod, jniSignature);
	}
	private static var set_listener = bindJNI("setListener", "(Lorg/haxe/lime/HaxeObject;)V");
	private static var show_leaderboard = bindJNI("showLeaderboard", "(Ljava/lang/String;I)V");
	private static var show_leaderboards = bindJNI("showLeaderboards", "()V");
	private static var show_achievements = bindJNI("showAchievements", "()V");
	private static var increment_achievement = bindJNI("incrementAchievement", "(Ljava/lang/String;I)V");
	private static var reveal_achievement = bindJNI("revealAchievement", "(Ljava/lang/String;)V");
	private static var reveal_achievement_immediate = bindJNI("revealAchievementImmediate", "(Ljava/lang/String;)V");
	private static var set_achievement_steps = bindJNI("setAchievementSteps", "(Ljava/lang/String;I)V");
	private static var unlock_achievement = bindJNI("unlockAchievement", "(Ljava/lang/String;)V");
	private static var submit_score = bindJNI("submitScore", "(Ljava/lang/String;ILjava/lang/String;)V");
	private static var sign_in = bindJNI("signIn", "()V");
	private static var sign_out = bindJNI("signOut", "()V");
	private static var is_signed_in = bindJNI("isSignedIn", "()Z");
	private static var set_gravity_for_popups = bindJNI("setGravityForPopups", "(II)V");
}

#end