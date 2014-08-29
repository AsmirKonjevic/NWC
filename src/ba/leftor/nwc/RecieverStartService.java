package ba.leftor.nwc;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RecieverStartService extends BroadcastReceiver{

	/**
	 * Provjeri prvo da li je servi pokrenut; ako nije onda provjeri da li ima sta za sinhroniziranje. ako nema iskljuci alarm!
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent service = new Intent(context, ServiceNwcSync.class);
		
	    if(GlobalFunctionsAndConstants.isNetworkAvailable(context) && !isMyServiceRunning(context)){
	    	int numSyncPremise=0;
			int numSyncCustomer=0;
			int numSyncWaterReading=0;
			int numSyncReportLeak=0;
			MyDataSource dataSource=new MyDataSource(context);
	    	dataSource.open();
	    	numSyncPremise=dataSource.premiseItemCount();
	    	numSyncCustomer=dataSource.customerItemCount();
	    	numSyncWaterReading=dataSource.waterMeterReadingItemCount();
	    	numSyncReportLeak=dataSource.waterLeakItemCount();
	    	dataSource.close();
	    	if(numSyncCustomer>0 || numSyncPremise>0 || numSyncWaterReading>0 || numSyncReportLeak>0){
	    		context.startService(service);
	    	} else {
	    		Intent i = new Intent(context, RecieverStartService.class);
	    	    PendingIntent pending = PendingIntent.getBroadcast(context, 0, i,
	    	        PendingIntent.FLAG_CANCEL_CURRENT);
	    	    AlarmManager am = (AlarmManager) context
	    		        .getSystemService(Context.ALARM_SERVICE);
	    	    am.cancel(pending);
	    	}
	    }
	}
	
	private boolean isMyServiceRunning(Context c) {
	    ActivityManager manager = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (ServiceNwcSync.class.getName().equals(service.service.getClassName())) {
	        	Log.w("IS RUNNING","SERVIce IS RUNNING!!");
	            return true;
	        }
	    }
	    return false;
	}

}
