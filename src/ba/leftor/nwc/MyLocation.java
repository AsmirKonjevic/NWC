package ba.leftor.nwc;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class MyLocation {
    LocationManager lm;
    LocationResult locationResult;
    boolean gps_enabled=false;

    public boolean getLocation(Context context, LocationResult result)
    {
    	Log.w("GET LOCATION","POZVAN GET LOCATION");
        //I use LocationResult callback class to pass location value from MyLocation to user code.
        locationResult=result;
        if(lm==null)
            lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        
        //exceptions will be thrown if provider is not permitted.
        try{gps_enabled=lm.isProviderEnabled(LocationManager.GPS_PROVIDER);}catch(Exception ex){}
    	Log.w("GET LOCATION","PROVERAVAM PROVAJDER");

        //don't start listeners if no provider is enabled
        if(!gps_enabled)
            return false;

        if(gps_enabled)
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGps);
        
        return true;
    }

    LocationListener locationListenerGps = new LocationListener() {
        public void onLocationChanged(Location location) {
            locationResult.gotLocation(location,0);
            lm.removeUpdates(this);
        }
        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };



    //provider=0 - GPS, ==1 - wifi
    public static abstract class LocationResult{
        public abstract void gotLocation(Location location,int provider);
    }
    
	public void cancelUpdates() {
		if(lm!=null){
			lm.removeUpdates(locationListenerGps);
		}
	}
}