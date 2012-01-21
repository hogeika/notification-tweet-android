package org.hogeika.android.app.NotificationTweet;

import java.util.List;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Notification;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class NotificationMonitorService extends AccessibilityService {
	private static final String LOG_TAG = "NotificationMonitorService";
    private static final int NOTIFICATION_TIMEOUT_MILLIS = 80;
	
    private MyApplication mApplication;
    
	@Override
	public void onCreate() {
        Log.d(LOG_TAG, "onCreate()");
		super.onCreate();
		mApplication = (MyApplication) getApplication();
	}

	@Override
	protected void onServiceConnected() {
        Log.d(LOG_TAG, "onServiceConnected()");
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.notificationTimeout = NOTIFICATION_TIMEOUT_MILLIS;
        info.flags = AccessibilityServiceInfo.DEFAULT;
        setServiceInfo(info);	}

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d(LOG_TAG, "onAccessibilityEvent()");
        int eventType = event.getEventType();
        switch (eventType) {
        case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED :
        	Log.d(LOG_TAG, "TYPE_NOTIFICATION_STATE_CHANGED");
        	Log.d(LOG_TAG, "pkg : " + event.getPackageName());
        	Log.d(LOG_TAG, "data : " + event.getParcelableData().toString());
//        	List<CharSequence> texts = event.getText();
//        	for(int i = 0; i < texts.size(); i++){
//        		Log.d(LOG_TAG, "text(" + i + ") : " + texts.get(i));
//        	}
        	Log.d(LOG_TAG, "description : " + event.getContentDescription());
        	
        	String packageNmae = event.getPackageName().toString();
        	SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        	String key = "FLAG-" + packageNmae;
			if(pref.contains(key)){
        		boolean flag = pref.getBoolean(key, false);
        		if(flag){
        			StringBuffer buf = new StringBuffer();
                	List<CharSequence> texts = event.getText();
                	for(int i = 0; i < texts.size(); i++){
                		buf.append(texts.get(i));
                		buf.append('\n');
                		Log.d(LOG_TAG, "text(" + i + ") : " + texts.get(i));
                	}
                	mApplication.getNotificationSender().sendNotification((Notification) event.getParcelableData(), packageNmae, texts);
       		}
        	}else{
        		Editor editor = pref.edit();
        		editor.putBoolean(key, false);
        		editor.commit();
        	}
        	break;
        default :
            Log.w(LOG_TAG, "Unknown accessibility event type " + eventType);        	
        }
	}

	@Override
	public void onInterrupt() {
		// TODO Auto-generated method stub

	}

}
