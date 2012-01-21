package org.hogeika.android.app.NotificationTweet;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

public class MyApplication extends Application {
	public static final int REQUEST_LOGIN_TWITTER = 3;

	private NotificationSender mSender;
	
	@Override
	public void onCreate() {
		Log.d("MyApplication", "onCreate()");
		super.onCreate();
		mSender = new NotificationSender(this);
		mSender.setRequestCode(REQUEST_LOGIN_TWITTER);
	}

	public void authorizeCallback(int requestCode, int resultCode, Intent data) {
		if(requestCode == REQUEST_LOGIN_TWITTER){
			mSender.authorizeCallback(requestCode, resultCode, data);
		}
	}	
	
	public NotificationSender getNotificationSender(){
		return mSender;
	}
}
