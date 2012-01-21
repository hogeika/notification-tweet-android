package org.hogeika.android.app.NotificationTweet;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import de.akquinet.android.androlog.Log;

public class MainActivity extends Activity {
	private MyApplication mApplication;
	private ArrayList<PackageData> mPackages;
	private PackageAdapter mAdapter;
	
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
				PackageData selected = mPackages.get(position);
				if(selected.getFlag()){
					selected.setFlag(false);
				}else{
					selected.setFlag(true);
				}
				SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
				Editor editor = pref.edit();
				editor.putBoolean("FLAG-" + selected.getPackageName(), selected.getFlag());
				editor.commit();
				mAdapter.notifyDataSetChanged();
			}
		});
    }
    
    @Override
	protected void onResume() {
		super.onResume();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        for(String key : pref.getAll().keySet()){
        	if(key.startsWith("FLAG-")){
        		String packageName = key.substring(5);
        		boolean flag = pref.getBoolean(key, false);
        		PackageData data = new PackageData(flag, packageName);
        		mPackages.add(data);
        	}
        }
        Button login = (Button) findViewById(R.id.Button_login);
        login.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mApplication.getNotificationSender().login(MainActivity.this);
			}
		});
        Button logout = (Button) findViewById(R.id.Button_logout);
        logout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mApplication.getNotificationSender().logout(MainActivity.this);
			}
		});
        String loginName = mApplication.getNotificationSender().getLoginName();
        logout.setText("Logout(" + loginName + ")");
        if(loginName == null){
        	login.setVisibility(View.VISIBLE);
        	logout.setVisibility(View.GONE);
        }else{
        	login.setVisibility(View.GONE);
        	logout.setVisibility(View.VISIBLE);
        }
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		mApplication.authorizeCallback(requestCode, resultCode, data);
	}
	
	private class PackageData {
    	private boolean mFlag;
    	private final String mPackageName;
    	private String mDescription;
    	private final Drawable mIcon;
    	
		public PackageData(boolean mFlag, String mPackageName) {
			super();
			this.mFlag = mFlag;
			this.mPackageName = mPackageName;
			this.mDescription = "";
			this.mIcon = null;
		}

		private boolean getFlag() {
			return mFlag;
		}
		private void setFlag(boolean flag) {
			this.mFlag = flag;
		}
		private String getPackageName() {
			return mPackageName;
		}
		private String getDescription() {
			return mDescription;
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
				convertView = layoutInflater.inflate(R.layout.listitem_package, parent, false);
			}
			TextView packageName = (TextView) convertView.findViewById(R.id.TextView_packageName);
			TextView packageDescription = (TextView) convertView.findViewById(R.id.TextView_packageDescription);
			ImageView packageIcon = (ImageView) convertView.findViewById(R.id.ImageView_packageIcon);
			CheckBox packageFlag = (CheckBox) convertView.findViewById(R.id.CheckBox_packageFlag);

			// Populate template
			PackageData data = getItem(position);
			packageName.setText(data.getPackageName());
			packageDescription.setText(data.getDescription());
			Drawable icon = data.getIcon();
			if (icon == null) {
				icon = getResources().getDrawable(android.R.drawable.ic_menu_search);
			}
			packageIcon.setImageDrawable(icon);
			packageFlag.setChecked(data.getFlag());

			return convertView;
		}
    	
    }
}

