package org.hogeika.android.app.NotificationTweet;

import java.util.List;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.Log;

public class NotificationSender {
	public static final String PREF_TWITTER_OAUTH_TOKEN = "twitter_oauth_token";
	public static final String PREF_TWITTER_OAUTH_TOEKN_SECRET = "twitter_oauth_token_secret";

	private static final int REQUEST_LOGIN_TWITTER = 1;
	private int mRequestCodeLogin = REQUEST_LOGIN_TWITTER;

	private final Context mContext;
	private final TwitterFactory mFactory;
	private Twitter mTwitter = null;

	static TwitterFactory createTwitterFactory(Context context) {
		Resources res = context.getResources();
		ConfigurationBuilder conf = new ConfigurationBuilder();
		conf.setOAuthConsumerKey(res.getString(R.string.twitter_oauth_consumer_key));
		conf.setOAuthConsumerSecret(res.getString(R.string.twitter_oauth_consumer_secret));
		TwitterFactory factory = new TwitterFactory(conf.build());
		return factory;
	}
	
	public NotificationSender(Context context){
		mContext = context;
		mFactory = createTwitterFactory(context);

		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		String oauthToken = pref.getString(PREF_TWITTER_OAUTH_TOKEN, "");
		String oauthTokenSecret = pref.getString(PREF_TWITTER_OAUTH_TOEKN_SECRET, "");
		if(oauthToken.length() > 0 && oauthTokenSecret.length()>0){
			AccessToken accessToken = new AccessToken(oauthToken, oauthTokenSecret);
			mTwitter = mFactory.getInstance(accessToken);
		}
	}
	
	public void setRequestCode(int login){
		mRequestCodeLogin = login;
	}

	public void login(Activity activity){
		Intent intent = new Intent(activity, TwitterLoginActivity.class);
		activity.startActivityForResult(intent, mRequestCodeLogin);
	}

	public void authorizeCallback(int requestCode, int resultCode, Intent data) {
		if(requestCode == mRequestCodeLogin && resultCode == Activity.RESULT_OK){
			String oauthToken = data.getStringExtra(TwitterLoginActivity.RESULT_OAUTH_TOKEN);
			String oauthTokenSecret = data.getStringExtra(TwitterLoginActivity.RESULT_OAUTH_TOKEN_SECRET);

			if(oauthToken.length()>0 && oauthTokenSecret.length()>0){
				SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
				Editor editor = pref.edit();
				editor.putString(PREF_TWITTER_OAUTH_TOKEN, oauthToken);
				editor.putString(PREF_TWITTER_OAUTH_TOEKN_SECRET, oauthTokenSecret);
				editor.commit();

				AccessToken accessToken = new AccessToken(oauthToken, oauthTokenSecret);
				mTwitter = mFactory.getInstance(accessToken);
			}	
		}
	}
	
	public void logout(Activity activity) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setPositiveButton("Logout", new OnClickListener() {		
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mTwitter = null;
				SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
				Editor editor = pref.edit();
				editor.remove(PREF_TWITTER_OAUTH_TOKEN);
				editor.remove(PREF_TWITTER_OAUTH_TOEKN_SECRET);
				editor.commit();
			}
		});
		builder.setNegativeButton("Cancel", null);
		AlertDialog dialog = builder.create();
		dialog.setOwnerActivity(activity);
		dialog.show();
	}	
	
	public String getLoginName(){
		if(mTwitter == null){
			return null;
		}
		try {
			return mTwitter.getScreenName();
		} catch (IllegalStateException e) {
		} catch (TwitterException e) {
		}
		return null;
	}

	public void sendNotification(Notification notification, String packageName, List<CharSequence> texts) {
		if(mTwitter == null){
			return;
		}
		StringBuffer buf = new StringBuffer();
		buf.append(packageName);
		buf.append(" : ");
		if(texts.size() > 0){
			buf.append(texts.get(0));
		}
		try {
			Log.d("NotificationSender", "Send : " + buf.toString());
			mTwitter.updateStatus(buf.toString());
		} catch (TwitterException e) {
			e.printStackTrace();
		}
	}
	
}
