package ba.leftor.nwc;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RecieverBootStart extends BroadcastReceiver {

	  // Restart service every 15 minutes
	  private static final long REPEAT_TIME = 1000*60*15;

	  @Override
	  public void onReceive(Context context, Intent intent) {
	    AlarmManager service = (AlarmManager) context
	        .getSystemService(Context.ALARM_SERVICE);
	    Intent i = new Intent(context, RecieverStartService.class);
	    PendingIntent pending = PendingIntent.getBroadcast(context, 0, i,
	        PendingIntent.FLAG_CANCEL_CURRENT);
	    Calendar cal = Calendar.getInstance();
	    // Start 30 seconds after boot completed
	    cal.add(Calendar.SECOND, 30);
	    service.cancel(pending);
	    //
	    // Fetch every 30 seconds
	    // InexactRepeating allows Android to optimize the energy consumption
	    service.setRepeating(AlarmManager.RTC_WAKEUP,
	        cal.getTimeInMillis(), REPEAT_TIME, pending);


	  } 

}
