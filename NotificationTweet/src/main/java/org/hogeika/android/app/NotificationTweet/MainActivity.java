package org.hogeika.android.app.NotificationTweet;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import de.akquinet.android.androlog.Log;

public class MainActivity extends Activity {
	private MyApplication mApplication;
	private ArrayList<PackageData> mPackages;
	private PackageAdapter mAdapter;
	private boolean mEnableFilter = false;
	
    /**
     * Called when the activity is first created.
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it is null.</b>
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initializes the logging
        Log.init();

        // Log a message (only on dev platform)
        Log.i(this, "onCreate");

        setContentView(R.layout.main);
        
        mApplication = (MyApplication)getApplication();
        mPackages = new ArrayList<PackageData>();

		ListView listView = (ListView) findViewById(R.id.ListView_packages);
		mAdapter = new PackageAdapter(this, mPackages);
		listView.setAdapter(mAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
				SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mApplication);
				PackageData selected = mPackages.get(position);
				boolean flag = pref.getBoolean("FLAG-" + selected.getPackageName(), false);
				Editor editor = pref.edit();
				editor.putBoolean("FLAG-" + selected.getPackageName(), !flag);
				editor.commit();
				mAdapter.notifyDataSetChanged();
			}
		});
    }
    
    @Override
	protected void onResume() {
		super.onResume();
		PackageManager pm = getPackageManager(); 
		List<PackageInfo> packages = pm.getInstalledPackages(0);
		for(PackageInfo pinfo : packages){
			String packageName = pinfo.packageName;
    		Drawable icon = null;
    		String appName = "";
    		try {
				icon = pm.getApplicationIcon(packageName);
				ApplicationInfo info = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
				if(info != null){
					appName = pm.getApplicationLabel(info).toString();
				}
			} catch (NameNotFoundException e) {
			}
    		PackageData data = new PackageData(packageName, icon, appName);
    		mPackages.add(data);
		}
	}
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if(mEnableFilter){
			menu.findItem(R.id.menu_filter).setVisible(false);
			menu.findItem(R.id.menu_all).setVisible(true);			
		}else {
			menu.findItem(R.id.menu_filter).setVisible(true);
			menu.findItem(R.id.menu_all).setVisible(false);			
		}
		
        String loginName = mApplication.getNotificationSender().getLoginName();
        if(loginName != null){
        	menu.findItem(R.id.menu_login).setVisible(false);
        	menu.findItem(R.id.menu_logout).setVisible(true);
        }else{
        	menu.findItem(R.id.menu_login).setVisible(true);
        	menu.findItem(R.id.menu_logout).setVisible(false);
        }
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_filter:
			mEnableFilter = true;
			mAdapter.notifyDataSetInvalidated();
			return true;
			
		case R.id.menu_all:
			mEnableFilter = false;
			mAdapter.notifyDataSetInvalidated();
			return true;
			
		case R.id.menu_login:
			mApplication.getNotificationSender().login(this);
			return true;

		case R.id.menu_logout:
			mApplication.getNotificationSender().logout(this);
			return true;

		case R.id.menu_accessiblity:
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.setClassName("com.android.settings", "com.android.settings.AccessibilitySettings");
			startActivity(intent);
			return true;
		default:
			return super.onMenuItemSelected(featureId, item);
		}
		
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		mApplication.authorizeCallback(requestCode, resultCode, data);
	}
	
	private class PackageData {
    	private final String mPackageName;
    	private String mAppName;
    	private final Drawable mIcon;
    	
		public PackageData(String mPackageName, Drawable icon, String appName) {
			super();
			this.mPackageName = mPackageName;
			this.mIcon = icon;
			this.mAppName = appName;
		}
		private String getPackageName() {
			return mPackageName;
		}
		private String getAppName() {
			return mAppName;
		}
		private Drawable getIcon() {
			return mIcon;
		}
    }
    
    private class PackageAdapter extends ArrayAdapter<PackageData> {

		public PackageAdapter(Context context, List<PackageData> objects) {
			super(context, R.layout.listitem_package, objects);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// Inflate a view template
			if (convertView == null) {
				LayoutInflater layoutInflater = getLayoutInflater();
				convertView = layoutInflater.inflate(R.layout.listitem_package, null);
			}
			View itemContainer = (View) convertView.findViewById(R.id.Layout_contaienr);
			TextView packageName = (TextView) convertView.findViewById(R.id.TextView_packageName);
			TextView appName = (TextView) convertView.findViewById(R.id.TextView_appName);
			ImageView packageIcon = (ImageView) convertView.findViewById(R.id.ImageView_packageIcon);
			CheckBox packageFlag = (CheckBox) convertView.findViewById(R.id.CheckBox_packageFlag);

			PackageData data = getItem(position);
			packageName.setText(data.getPackageName());
			appName.setText(data.getAppName());
			Drawable icon = data.getIcon();
			if (icon == null) {
				icon = getResources().getDrawable(android.R.drawable.ic_menu_search);
			}
			packageIcon.setImageDrawable(icon);

			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mApplication);
			packageFlag.setChecked(pref.getBoolean("FLAG-" + data.getPackageName(), false));
			if(pref.contains("HINT-" + data.getPackageName())){
				convertView.setBackgroundColor(Color.BLUE);
				itemContainer.setVisibility(View.VISIBLE);
			}else{
				convertView.setBackgroundColor(android.R.color.background_dark);
				if(mEnableFilter){
					itemContainer.setVisibility(View.GONE);
				} else {
					itemContainer.setVisibility(View.VISIBLE);
				}
			}
			return convertView;
		}
    	
    }
}

