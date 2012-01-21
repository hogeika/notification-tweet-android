package org.hogeika.android.app.NotificationTweet;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class TwitterLoginActivity extends Activity {
	public static final String RESULT_OAUTH_TOKEN = "oauth_token";
	public static final String RESULT_OAUTH_TOKEN_SECRET = "oauth_token_secret";
	public static final String CALLBACK_URL = "myapp://oauth";
	
	private static final int DIALOG_PROGRESS = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("Twitter Login");

		setContentView(R.layout.twitter_login);
		new Handler().post(new Runnable() {
			@Override
			public void run() {
				final String authUrl;
				final Twitter tmpTwitter;
				final RequestToken tmpRequestToken;
				showDialog(DIALOG_PROGRESS);
				try {
					// TODO in background
					tmpTwitter = NotificationSender.createTwitterFactory(TwitterLoginActivity.this).getInstance();
					tmpRequestToken = tmpTwitter.getOAuthRequestToken(TwitterLoginActivity.CALLBACK_URL);
					authUrl = tmpRequestToken.getAuthorizationURL();
				} catch (TwitterException e) {
					e.printStackTrace();
					setResult(RESULT_CANCELED);
					finish();
					return;
				} finally {
					safeDismissDialog(DIALOG_PROGRESS);
				}
				
				final WebView webView = (WebView) findViewById(R.id.WebView_twitter_login);
				CookieManager cm = CookieManager.getInstance();
				cm.removeAllCookie();
				WebSettings webSettings = webView.getSettings();
				webSettings.setJavaScriptEnabled(true);
				webView.setWebViewClient(new WebViewClient(){

					@Override
					public void onPageStarted(WebView view, String url, Bitmap favicon) {
						super.onPageStarted(view, url, favicon);
						showDialog(DIALOG_PROGRESS);
					}

					@Override
					public void onPageFinished(WebView view, String url) {
						super.onPageFinished(view, url);
						safeDismissDialog(DIALOG_PROGRESS);
						if(url != null && url.startsWith(CALLBACK_URL)){
							try {
								Uri uri = Uri.parse(url);
								String oauthToken = uri.getQueryParameter("oauth_token");
								String oauthVerifier = uri.getQueryParameter("oauth_verifier");

								AccessToken accessToken;
								showDialog(DIALOG_PROGRESS);
								try {
									// TODO in background
									accessToken = tmpTwitter.getOAuthAccessToken(tmpRequestToken, oauthVerifier);
								} finally{
									safeDismissDialog(DIALOG_PROGRESS);
								}
								Log.d("Twitter", "oauth_token = " + accessToken.getToken());
								Log.d("Twitter", "oauth_token_secret = " + accessToken.getTokenSecret());
								if(!tmpRequestToken.getToken().equals(oauthToken)){
									Log.d("Twitter", "Ugh!");
									setResult(RESULT_CANCELED);
									return;
								}
								String loginAccount = tmpTwitter.getScreenName();
								Log.d("TwitterLoginActivity", "Login as " + loginAccount);

								Intent result = new Intent();
								result.putExtra(RESULT_OAUTH_TOKEN, accessToken.getToken());
								result.putExtra(RESULT_OAUTH_TOKEN_SECRET, accessToken.getTokenSecret());
								setResult(RESULT_OK, result);
							} catch (TwitterException e) {
								e.printStackTrace();
								setResult(RESULT_CANCELED);
							} finally {
								finish();
							}
						}
					}
				});
				webView.loadUrl(authUrl);
			}
		});
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id){
		case DIALOG_PROGRESS:
			return new ProgressDialog(this);
		}
		return super.onCreateDialog(id);
	}
	
	private void safeDismissDialog(int id){
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					dismissDialog(DIALOG_PROGRESS);
				}catch(IllegalArgumentException e){
				}
			}
		});
	}
}
