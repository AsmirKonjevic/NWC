package ba.leftor.nwc;

import java.util.Calendar;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ActivityDashboard extends Activity{
	
	private TextView txtSync;

	private MyWebRequestReceiver receiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// display user name on top
		TextView txtWelcome = (TextView) findViewById(R.id.txtWelcome);
		JSONUSerItem user = GlobalFunctionsAndConstants.getUser(this);
		if (user.fullName.length() > 1)
			txtWelcome.setText(ActivityDashboard.this.getResources().getString(R.string.welcome) + user.fullName);
		else
			txtWelcome.setText(ActivityDashboard.this.getResources().getString(R.string.welcome) + user.username);
		
		Button btnPremisse=(Button)findViewById(R.id.btnPremisse);
		Button btnCustomer=(Button)findViewById(R.id.btnCustomer);
		Button btnRejectedPremise=(Button)findViewById(R.id.btnRejectedPremisse);
		Button btnRejectedCustomer=(Button)findViewById(R.id.btnRejectedCustomer);
		Button btnWaterMeterForm=(Button)findViewById(R.id.btnWaterMeterForm);
		Button btnReportLeakForm=(Button)findViewById(R.id.btnReportLeakForm);
		Button btnReconnection=(Button)findViewById(R.id.btnViolationDisconnectionForm);
        Button btnLogout=(Button)findViewById(R.id.btnLogout);
		Button btnPremiseImage=(Button)findViewById(R.id.btnPremiseImage);
		Button btnViolationDisconnectionForm=(Button)findViewById(R.id.btnViolationDisconnectionForm);

        if(user.premisePermission != 1) {
        	btnPremisse.setVisibility(View.GONE);
        	
        }
        if(user.rejectedPremisePermission != 1) {
        	btnRejectedPremise.setVisibility(View.GONE);
        }
        
        if(user.rejectedCustomerPermission != 1) {
        	btnRejectedCustomer.setVisibility(View.GONE);
        }
        
        if(user.customerPermission != 1) {
        	btnCustomer.setVisibility(View.GONE);
        }
        
        if(user.waterMeterStatusPermission != 1) {
        	btnWaterMeterForm.setVisibility(View.GONE);
        }
        
        if(user.waterLeakPermission != 1) {
        	btnReportLeakForm.setVisibility(View.GONE);
        }
        if(user.reconnectionPermission != 1) {
        	btnReconnection.setVisibility(View.GONE);
        }

        if(user.premiseImagePermission != 1) {
        	btnPremiseImage.setVisibility(View.GONE);
        }
        
        btnLogout.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				SharedPreferences prefs=ActivityDashboard.this.getSharedPreferences(GlobalFunctionsAndConstants.PREFS_NAME, 0);
				Editor e=prefs.edit();
				e.remove("username");
				e.remove("pass");
				e.remove("premisePermission");
				e.remove("customerPermission");
				e.remove("rejectedCustomerPermission");
				e.remove("rejectedPremisePermission");
				e.remove("waterMeterStatusPermission");
				e.remove("waterLeakPermission");
				e.remove("premiseImagePermission");
				e.commit();
				GlobalFunctionsAndConstants.clearPrefs(ActivityDashboard.this);
				startActivity(new Intent(ActivityDashboard.this,ActivityLogin.class));
				ActivityDashboard.this.finish();
				Log.w("Logout","OK");
				
			}
		});
		
		btnPremisse.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				if(requireNewVersion())
					startActivity(new Intent(ActivityDashboard.this,ActivityPremise.class));
			}
		});
		
		btnViolationDisconnectionForm.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				if(requireNewVersion())
					startActivity(new Intent(ActivityDashboard.this,ActivityDisconnectionConnection.class));
			}
		});
		
		btnCustomer.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				if(requireNewVersion())
					startActivity(new Intent(ActivityDashboard.this,ActivityCustomer.class));
			}
		});
		
		btnRejectedPremise.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				if(requireNewVersion())
					startActivity(new Intent(ActivityDashboard.this,ActivityRejectedPremise.class));
			}
		});
		
		btnRejectedCustomer.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				if(requireNewVersion())
					startActivity(new Intent(ActivityDashboard.this,ActivityRejectedCustomer.class));
			}
		});	
		
		btnWaterMeterForm.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				if(requireNewVersion())
					startActivity(new Intent(ActivityDashboard.this,ActivityWaterMeterStatus.class));
			}
		});		
		
		btnReportLeakForm.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				if(requireNewVersion())
					startActivity(new Intent(ActivityDashboard.this,ActivityReportLeak.class));
			}
		});

		btnPremiseImage.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				if(requireNewVersion())
					startActivity(new Intent(ActivityDashboard.this,ActivityPremiseImage.class));
			}
		});
		
		//
		txtSync=(TextView) findViewById(R.id.txtSync);
		TextView txtAppVersion=(TextView)findViewById(R.id.txtAppVersion);
		TextView txtDbVersion=(TextView)findViewById(R.id.txtDbVersion);
		txtDbVersion.setText("Database version: "+MySQLiteHelper.DATABASE_VERSION);
		
		PackageManager manager = this.getPackageManager();
		PackageInfo info;
		try {
			info = manager.getPackageInfo(this.getPackageName(), 0);
			txtAppVersion.setText("Application version: "+info.versionCode + " (" + info.versionName + ")");
			checkForNewAppVersion(info.versionCode);
		} catch (NameNotFoundException e) {
		}
		     
		/**
		 * ONLY ALLOW SYNC IF INTERNET CONNECTION IS ENABLED AND IF THERE ARE SYNC ITEMS
		 */
		Button btnSync=(Button)findViewById(R.id.btnSync);
		btnSync.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				setSyncText();
				if(GlobalFunctionsAndConstants.isNetworkAvailable(ActivityDashboard.this) && !isMyServiceRunning(ActivityDashboard.this)){
			    	int numSyncPremise=0;
					int numSyncCustomer=0;
					int numSyncWaterReading=0;
					int numSyncReportLeak=0;
					MyDataSource dataSource=new MyDataSource(ActivityDashboard.this);
			    	dataSource.open();
			    	numSyncPremise=dataSource.premiseItemCount();
			    	numSyncCustomer=dataSource.customerItemCount();
			    	numSyncWaterReading=dataSource.waterMeterReadingItemCount();
			    	numSyncReportLeak=dataSource.waterLeakItemCount();
			    	dataSource.close();
			    	Log.w("SERVICE","RUCNO PROVJERAVAM IMA LI STA ZA SYNC");
			    	if(numSyncCustomer>0 || numSyncPremise>0 || numSyncWaterReading>0 || numSyncReportLeak>0){
			    		Log.w("SERVICE","STARTAM RUCNO SA DASHBOARDA");
			    		Intent service = new Intent(ActivityDashboard.this, ServiceNwcSync.class);
			    		ActivityDashboard.this.startService(service);
			    	}
			    }
				
			}
		});
		
		
		//start locator service with AlARM
		//set alarm for service !!!
		AlarmManager service = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
	    Intent i = new Intent(this, RecieverStartServiceLocator.class);
	    PendingIntent pending = PendingIntent.getBroadcast(this, 1, i,PendingIntent.FLAG_UPDATE_CURRENT);
	    Calendar cal = Calendar.getInstance();
	    service.cancel(pending);
	    
	    // Fetch every 5 min
	    // InexactRepeating allows Android to optimize the energy consumption
	    service.setRepeating(AlarmManager.RTC_WAKEUP,
	        cal.getTimeInMillis(), 60*5*1000, pending);
		
		//test error items
//		MyDataSource ds=new MyDataSource(this);
//		ds.open();
//		ArrayList<JSONPremiseItem> lista=ds.getBadPremiseItems(20);
//		for (JSONPremiseItem jsonPremiseItem : lista) {
//			Log.w("ERROR SYNC ITEM",jsonPremiseItem.errors);
//		}
		
	}
	
	
	private Boolean requireNewVersion(){
		SharedPreferences prefs=this.getSharedPreferences(GlobalFunctionsAndConstants.PREFS_NAME, 0);
		int newVersion=prefs.getInt("newVersion", 0);
		
		PackageManager manager = this.getPackageManager();
		PackageInfo info;
		int curentVersion=0;
		try {
			info = manager.getPackageInfo(this.getPackageName(), 0);
			curentVersion=info.versionCode;
		} catch (NameNotFoundException e) {
		}
		//show dialog if there is new version
		if(newVersion > curentVersion) {
			MyDataSource dataSource=new MyDataSource(ActivityDashboard.this);
	    	dataSource.open();
	    	int numSyncPremise=dataSource.premiseItemCount();
	    	int numSyncCustomer=dataSource.customerItemCount();
	    	int numSyncWaterStatus=dataSource.waterMeterReadingItemCount();
	    	int numSyncReportLeak=dataSource.waterLeakItemCount();
	    	dataSource.close();
	    	
	    	//reci da ima update ali da mora uraditi sync prvo
	    	if((numSyncCustomer+numSyncPremise+numSyncReportLeak+numSyncWaterStatus)>0){
	    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		    	builder.setMessage(getString(R.string.new_version_required_sync))
		    	     .setCancelable(false)
		    	     .setPositiveButton(this.getResources().getString(R.string.close),
		    	          new DialogInterface.OnClickListener(){
		    	          public void onClick(DialogInterface dialog, int id){
		    	        	  dialog.dismiss();
		    	          }
		    	     });
		    	AlertDialog alert = builder.create();
		    	alert.show();
	    		
	    		return false;
	    	}
			
			
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    	builder.setMessage(getString(R.string.new_version_required))
	    	     .setCancelable(false)
	    	     .setPositiveButton("Download and install",
	    	          new DialogInterface.OnClickListener(){
	    	          public void onClick(DialogInterface dialog, int id){
	    	        	  String url = getString(R.string.url_application_installer);
	    	        	  Intent i = new Intent(Intent.ACTION_VIEW);
	    	        	  i.setData(Uri.parse(url));
	    	        	  startActivity(i);
	    	          }
	    	     });
	    	     builder.setNegativeButton(this.getResources().getString(R.string.close),
	    	          new DialogInterface.OnClickListener(){
	    	          public void onClick(DialogInterface dialog, int id){
	    	               dialog.dismiss();
	    	          }
	    	     });
	    	AlertDialog alert = builder.create();
	    	alert.show();
	    	return false;
		}
		return true;
	}
	
	/**
	 * Check if there is new app version and if there is show dialog for user to download it
	 * @param int curentVersion
	 */
	private void checkForNewAppVersion(int curentVersion){
		SharedPreferences prefs=this.getSharedPreferences(GlobalFunctionsAndConstants.PREFS_NAME, 0);
		int newVersion=prefs.getInt("newVersion", 0);
	
		//show dialog if there is new version
		if(newVersion > curentVersion) {
			MyDataSource dataSource=new MyDataSource(ActivityDashboard.this);
	    	dataSource.open();
	    	int numSyncPremise=dataSource.premiseItemCount();
	    	int numSyncCustomer=dataSource.customerItemCount();
	    	int numSyncReportLeak=dataSource.waterLeakItemCount();
	    	int numSyncWaterStatus=dataSource.waterMeterReadingItemCount();
	    	dataSource.close();
	    	
	    	//reci da ima update ali da mora uraditi sync prvo
	    	if((numSyncCustomer+numSyncPremise+numSyncReportLeak+numSyncWaterStatus)>0){
	    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		    	builder.setMessage("New update has been posted (Version " + newVersion + "). To allow download please SYNC ALL ITEMS and restart the application!")
		    	     .setCancelable(false)
		    	     .setPositiveButton(this.getResources().getString(R.string.close),
		    	          new DialogInterface.OnClickListener(){
		    	          public void onClick(DialogInterface dialog, int id){
		    	        	  dialog.dismiss();
		    	          }
		    	     });
		    	AlertDialog alert = builder.create();
		    	alert.show();
	    		
	    		return;
	    	}
			
			
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    	builder.setMessage("New update has been posted (Version " + newVersion + "). Please download and install the latest app version!")
	    	     .setCancelable(false)
	    	     .setPositiveButton("Download and install",
	    	          new DialogInterface.OnClickListener(){
	    	          public void onClick(DialogInterface dialog, int id){
	    	        	  String url = getString(R.string.url_application_installer);
	    	        	  Intent i = new Intent(Intent.ACTION_VIEW);
	    	        	  i.setData(Uri.parse(url));
	    	        	  startActivity(i);
	    	          }
	    	     });
	    	     builder.setNegativeButton(this.getResources().getString(R.string.close),
	    	          new DialogInterface.OnClickListener(){
	    	          public void onClick(DialogInterface dialog, int id){
	    	               dialog.dismiss();
	    	          }
	    	     });
	    	AlertDialog alert = builder.create();
	    	alert.show();
		}
		
	}
	
	private boolean isMyServiceRunning(Context c) {
	    ActivityManager manager = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (ServiceNwcSync.class.getName().equals(service.service.getClassName())) {
	        	Log.w("IS RUNNING","SErVIce IS RUNNING!!");
	            return true;
	        }
	    }
	    return false;
	}
	

	@Override
	protected void onResume() {
		IntentFilter intentFilter = new IntentFilter(
                "ba.leftor.nwc.intent.action.UPDATE_SYNC");
		receiver = new MyWebRequestReceiver();
        //registering our receiver
        this.registerReceiver(receiver, intentFilter);
		super.onResume();
		setSyncText();
		
	}
	
	private void setSyncText(){
		int numSyncPremise=0;
		int numSyncCustomer=0;
		int numSyncWaterReading=0;
		int numSyncReportLeak=0;
		MyDataSource dataSource=new MyDataSource(this);
    	dataSource.open();
    	numSyncPremise=dataSource.premiseItemCount();
    	numSyncCustomer=dataSource.customerItemCount();
    	numSyncWaterReading=dataSource.waterMeterReadingItemCount();
    	numSyncReportLeak=dataSource.waterLeakItemCount();
    	dataSource.close();
    	if(txtSync!=null) txtSync.setText("Items waiting to be synced: "+(int)(numSyncCustomer+numSyncPremise+numSyncWaterReading+numSyncReportLeak));
	}


	@Override
	protected void onPause() {
		super.onPause();
		if(receiver!=null)
			this.unregisterReceiver(receiver);
	}
	
	public class MyWebRequestReceiver extends BroadcastReceiver{
 
        @Override
        public void onReceive(Context context, Intent intent) {
            //String responseString = intent.getStringExtra("RESPONSE");
        	Log.w("BROADCAST","RECEIVED");
            setSyncText();
 
        }
 
 
    }
	
	
}
