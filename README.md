# Haxe Google Play Games

Unofficial Google Play Games leaderboards and achievements support for Android Haxe/OpenFL targets using the Play Games SDK.

### Features ###

Supports:
* Google Play Games sign-in and sign-out.
* Showing leaderboards and achievements popover screens.
* Submitting leaderboard scores, unlocking and updating achievement progress.
* React to player sign-in, sign-out and activity results with a custom listener.

Doesn't Support:
* Cloud syncing, multiplayer or social features.

If there is something you would like adding then open an issue. Pull requests welcomed too! Here it is in action:

![Screenshot of it working](https://github.com/Tw1ddle/samcodes-googleplaygames/blob/master/screenshots/screen1.png?raw=true "Screenshot")

### Install ###

```bash
haxelib install samcodes-googleplaygames
```

### Usage ###

Project.xml
```xml
<haxelib name="samcodes-googleplaygames" />
<setenv name="GOOGLEPLAYGAMESID" value="YOUR_GOOGLE_PLAY_GAMES_ID" />
```