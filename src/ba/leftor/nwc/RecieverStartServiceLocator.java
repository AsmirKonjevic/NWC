package ba.leftor.nwc;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RecieverStartServiceLocator extends BroadcastReceiver{

	/**
	 * Provjeri prvo da li je servi pokrenut; ako nije onda ga pokreni
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent service = new Intent(context, ServiceNwcLocator.class);
		
	    if(GlobalFunctionsAndConstants.isNetworkAvailable(context) && !isMyServiceRunning(context)){
	    	context.startService(service);
	    }
	}
	
	private boolean isMyServiceRunning(Context c) {
	    ActivityManager manager = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (ServiceNwcLocator.class.getName().equals(service.service.getClassName())) {
	        	Log.w("LOCATOR IS RUNNING","SERVIce IS RUNNING!!");
	            return true;
	        }
	    }
	    return false;
	}

}
