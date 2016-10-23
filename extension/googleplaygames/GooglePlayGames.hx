package extension.googleplaygames;

#if android

import extension.googleplaygames.GooglePlayGravity;
import extension.googleplaygames.GooglePlayLeaderboardTimespan;
import extension.googleplaygames.GooglePlayListener;

import openfl.utils.JNI;

class GooglePlayGames {
	public static function init() {
		initBindings();
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
		connect();
	}
	
	public static function signOut():Void {
		disconnect();
	}
	
	public static function resetConnection():Void {
		reconnect();
	}
	
	public static function isConnected():Bool {
		return is_connected();
	}
	
	public static function getCurrentAccountName():String {
		return get_current_account_name();
	}
	
	public static function setGravityForPopups(horizontalGravity:HorizontalGravity, verticalGravity:VerticalGravity):Void {
		set_gravity_for_popups(horizontalGravity, verticalGravity);
	}

	private static function initBindings():Void {
		var packageName:String = "com/samcodes/googleplaygames/GooglePlayGames";
		
		if (set_listener == null) {
			set_listener = JNI.createStaticMethod(packageName, "setListener", "(Lorg/haxe/lime/HaxeObject;)V");
		}
		
		if (show_leaderboard == null) {
			show_leaderboard = openfl.utils.JNI.createStaticMethod(packageName, "showLeaderboard", "(Ljava/lang/String;I)V");
		}
		
		if (show_leaderboards == null) {
			show_leaderboards = openfl.utils.JNI.createStaticMethod(packageName, "showLeaderboards", "()V");
		}
		
		if (show_achievements == null) {
			show_achievements = openfl.utils.JNI.createStaticMethod(packageName, "showAchievements", "()V");
		}
		
		if (increment_achievement == null) {
			increment_achievement = openfl.utils.JNI.createStaticMethod(packageName, "incrementAchievement", "(Ljava/lang/String;I)V");
		}

		if(reveal_achievement == null) {
			reveal_achievement = openfl.utils.JNI.createStaticMethod(packageName, "revealAchievement", "(Ljava/lang/String;)V");
		}
		
		if (reveal_achievement_immediate == null) {
			reveal_achievement_immediate = openfl.utils.JNI.createStaticMethod(packageName, "revealAchievementImmediate", "(Ljava/lang/String;)V");
		}
		
		if (set_achievement_steps == null) {
			set_achievement_steps = openfl.utils.JNI.createStaticMethod(packageName, "setAchievementSteps", "(Ljava/lang/String;I)V");
		}
		
		if (unlock_achievement == null) {
			unlock_achievement = openfl.utils.JNI.createStaticMethod(packageName, "unlockAchievement", "(Ljava/lang/String;)V");
		}
		
		if (submit_score == null) {
			submit_score = openfl.utils.JNI.createStaticMethod(packageName, "submitScore", "(Ljava/lang/String;ILjava/lang/String;)V");
		}
		
		if (connect == null) {
			connect = openfl.utils.JNI.createStaticMethod(packageName, "connect", "()V");
		}
		
		if (disconnect == null) {
			disconnect = openfl.utils.JNI.createStaticMethod(packageName, "disconnect", "()V");
		}
		
		if (reconnect == null) {
			reconnect = openfl.utils.JNI.createStaticMethod(packageName, "reconnect", "()V");
		}
		
		if (is_connected == null) {
			is_connected = openfl.utils.JNI.createStaticMethod(packageName, "isConnected", "()Z");
		}
		
		if (get_current_account_name == null) {
			get_current_account_name = openfl.utils.JNI.createStaticMethod(packageName, "getCurrentAccountName", "()Ljava/lang/String;");
		}
		
		if (set_gravity_for_popups == null) {
			set_gravity_for_popups = openfl.utils.JNI.createStaticMethod(packageName, "setGravityForPopups", "(II)V");
		}
	}

	private static var set_listener: Dynamic = null;
	private static var show_leaderboard: Dynamic = null;
	private static var show_leaderboards: Dynamic = null;
	private static var show_achievements: Dynamic = null;
	private static var increment_achievement: Dynamic = null;
	private static var reveal_achievement: Dynamic = null;
	private static var reveal_achievement_immediate: Dynamic = null;
	private static var set_achievement_steps: Dynamic = null;
	private static var unlock_achievement: Dynamic = null;
	private static var submit_score: Dynamic = null;
	private static var connect: Dynamic = null;
	private static var disconnect: Dynamic = null;
	private static var reconnect: Dynamic = null;
	private static var is_connected: Dynamic = null;
	private static var get_current_account_name: Dynamic = null;
	private static var set_gravity_for_popups: Dynamic = null;
}

#end