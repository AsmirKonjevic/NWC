package ba.leftor.nwc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import ba.leftor.nwc.ActivityDashboard.MyWebRequestReceiver;
import ba.leftor.nwc.CustomMultiPartEntity.ProgressListener;

public class ServiceNwcSync extends IntentService{
		
	/** For showing and hiding our notification. */
    NotificationManager mNM;
    
    /** FOR currentProgress & totalSize for setting progress while uploading*/
	private int currentProgress=0;
	private long totalSize;

    
	/**
	 * Default intent service constructor
	 */
	public ServiceNwcSync() {
		super("ServiceNwcSync");
	}

	private void sendUpdateMessage(){
		 Log.w("SERVICE","SEN UPDATE MESSAGE METHOD AFTER SYNC!!");
		 Intent broadcastIntent = new Intent("ba.leftor.nwc.intent.action.UPDATE_SYNC");
	        broadcastIntent.putExtra("RESPONSE", "UPDATE OK");
	        sendBroadcast(broadcastIntent);
	}
	/**
	 * Main method of IntentService!
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

		//check if there is items to sync
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
    	
        // Display a notification about us starting.
    	if(numSyncCustomer > 0 || numSyncPremise > 0 || numSyncWaterReading>0 || numSyncReportLeak>0) {
    		syncWithServer();
    	}
    	Log.w("Service","SERVICE STOP SELF");
		
	}

	private void syncWithServer(){
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
    	
    	if(numSyncCustomer>0 || numSyncPremise>0 || numSyncWaterReading>0 || numSyncReportLeak>0) {
    		showNotification("Sync started", true);
    	}
    	
    	//sync all premises
    	if(numSyncPremise>0){
    		ArrayList<JSONPremiseItem> premises=dataSource.getPremiseItems(-1);
    		int counter=0;
    		for (JSONPremiseItem premise : premises) {
    			showNotification("Sending premise " + counter + " of " + numSyncPremise,false);
    			syncPremise(premise);
    			sendUpdateMessage();
			}
    	}
    	
    	//sync all customers
    	if(numSyncCustomer>0){
    		ArrayList<JSONCustomerItem> customers=dataSource.getCustomerItems(-1);
    		int counter=0;
    		for (JSONCustomerItem customer : customers) {
    			showNotification("Sending customer " + counter + " of " + numSyncCustomer,false);
    			syncCustomerItem(customer);
    			sendUpdateMessage();
			}
    	}
    	
    	//sync all reading reports
    	if(numSyncWaterReading>0){
    		ArrayList<JSONWaterReadingItem> readings=dataSource.getWaterReadingItems(-1);
    		int counter=0;
    		for (JSONWaterReadingItem reading : readings) {
    			showNotification("Sending water reading " + counter + " of " + numSyncWaterReading,false);
    			syncWaterMeterReadingItem(reading);
    			sendUpdateMessage();
			}
    	}
    	
    	//sync all leak reports
    	if(numSyncReportLeak>0){
    		ArrayList<JSONReportLeakItem> readings=dataSource.getWaterLeakItems(-1);
    		int counter=0;
    		for (JSONReportLeakItem reading : readings) {
    			showNotification("Sending leak report " + counter + " of " + numSyncReportLeak,false);
    			syncWaterLeakItem(reading);
    			sendUpdateMessage();
			}
    	}
    	dataSource.close();
    	
    	if(numSyncCustomer>0 || numSyncPremise>0 || numSyncWaterReading>0 || numSyncReportLeak>0) {
    		showNotification("Sync complete", true);
    	}
    	
    	
	}
	

	@Override
    public void onDestroy() {
        // Tell the user we stopped.
        //Toast.makeText(this, R.string.remote_service_stopped, Toast.LENGTH_SHORT).show();
    }

    
    
    /**
     * Show a notification while this service is running.
     */
    private void showNotification(String text,Boolean showTicker) {

    	Notification notification;
    	// Set the icon, scrolling text and timestamp
    	notification = new Notification();
        notification.icon=android.R.drawable.stat_notify_sync;
    	if(showTicker){
            notification.tickerText=text;
    	}
        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, getText(R.string.remote_service_label),
                       text, null);

        // Send the notification.
        mNM.notify(34523, notification);
    }
    
    private void syncCustomerItem(JSONCustomerItem customer){
    	try {
    		currentProgress=0;
			totalSize=0;
        	//load user
			SharedPreferences prefs=this.getSharedPreferences(GlobalFunctionsAndConstants.PREFS_NAME, 0);
        	JSONUSerItem user=new JSONUSerItem();
        	user.username=prefs.getString("serviceUsername", "");
        	user.password=prefs.getString("servicePass", "");
        	if(user.password.equals("")){
        		user.username=prefs.getString("username", "");
        		user.password=prefs.getString("pass", "");
        	}
        	
            String urlString = this.getApplicationContext().getResources().getString(R.string.url_customer);
            HttpClient client = new DefaultHttpClient();
            HttpEntity resEntity;
            HttpPost post = new HttpPost(urlString);
            FileBody ownerFileBody=null;
            if(customer.photo_owner!=null)
            	ownerFileBody = new FileBody(new File(customer.photo_owner));
            
            FileBody alternateContactFileBody=null;
            if(customer.photo_alternate!=null)
            	alternateContactFileBody = new FileBody(new File(customer.photo_alternate));

	        FileBody instrumentFileBody=null;
	        if(customer.photo_instrument!=null)
	        	instrumentFileBody = new FileBody(new File(customer.photo_instrument));
            
            
            CustomMultiPartEntity reqEntity = new CustomMultiPartEntity(new ProgressListener()
			{
				public void transferred(long num)
				{
					if(((int) ((num / (float) totalSize) * 100) - currentProgress)>10){
						currentProgress=(int) ((num / (float) totalSize) * 100);
						showNotification("Uploading customer entry " + currentProgress + "%",false);
					}
				}
			});
            
            
            
            /** parse variables + gps **/
            
            reqEntity.addPart("Customer[gps_latitude_created]", new StringBody(customer.gps_latitude_created));
            reqEntity.addPart("Customer[gps_longitude_created]", new StringBody(customer.gps_longitude_created));
            reqEntity.addPart("Customer[gps_altitude_created]", new StringBody(customer.gps_altitude_created));
            

    		
            reqEntity.addPart("Customer[house_connection_number]", new StringBody(customer.house_connection_number));
            reqEntity.addPart("Customer[full_name]", new StringBody(customer.full_name));
            reqEntity.addPart("Customer[national_id]", new StringBody(customer.national_id));
            reqEntity.addPart("dayExpirationDate", new StringBody(customer.day_expiration_date));
            reqEntity.addPart("yearExpirationDate", new StringBody(customer.year_expiration_date));
            reqEntity.addPart("Customer[email]", new StringBody(customer.email));
            reqEntity.addPart("Customer[phone_land_line]", new StringBody(customer.phone_land_line));
            reqEntity.addPart("Customer[mobile_phone]", new StringBody(customer.mobile_phone));
            reqEntity.addPart("Customer[po_box]", new StringBody(customer.po_box));
            reqEntity.addPart("Customer[zip_code]", new StringBody(customer.zip_code));
            reqEntity.addPart("Customer[land_id]", new StringBody(customer.land_id));
            
            reqEntity.addPart("Customer[alternate_contact_name]", new StringBody(customer.alternate_contact_name));
            reqEntity.addPart("Customer[contact_national_id]", new StringBody(customer.contact_national_id));
            reqEntity.addPart("Customer[contact_phone_land_line]", new StringBody(customer.contact_phone_land_line));
            reqEntity.addPart("Customer[contact_mobile_phone]", new StringBody(customer.mobile_phone));
            reqEntity.addPart("contactDayExpirationDate", new StringBody(customer.contact_day_expiration_date));
            reqEntity.addPart("contactYearExpirationDate", new StringBody(customer.contact_year_expiration_date));
            reqEntity.addPart("Customer[contact_email]", new StringBody(customer.contact_email));
            reqEntity.addPart("Customer[contact_street_name]", new StringBody(customer.contact_street_name));
            reqEntity.addPart("Customer[contact_zip_code]", new StringBody(customer.contact_zip_code));
            reqEntity.addPart("Customer[contact_po_box]", new StringBody(customer.contact_po_box));
            
            reqEntity.addPart("monthExpirationDate", new StringBody(customer.month_expiration_date));
            reqEntity.addPart("Customer[gender]", new StringBody(customer.gender));
            reqEntity.addPart("Customer[nationality_id]", new StringBody(customer.nationality_id));
            reqEntity.addPart("contactMonthExpirationDate", new StringBody(customer.contact_month_expiration_date));
            reqEntity.addPart("Customer[inhabitant_type_id]", new StringBody(customer.inhabitant_type_id));
            reqEntity.addPart("Customer[tarsheed_type_id]", new StringBody(customer.tarsheed_type_id));
            
            reqEntity.addPart("Customer[tarsheed_given]", new StringBody(customer.tarsheed_given));
            
            reqEntity.addPart("newPremiseLat", new StringBody(customer.newPremiseLat));
            reqEntity.addPart("newPremiseLng", new StringBody(customer.newPremiseLng));
            reqEntity.addPart("newPremiseAlt", new StringBody(customer.newPremiseAlt));
            
            
            /** END PARSE VARIABLES **/
            if(ownerFileBody!=null)
            	reqEntity.addPart("Customer[photo_owner]", ownerFileBody);
            if(alternateContactFileBody!=null)
            	reqEntity.addPart("Customer[photo_alternate_contact]", alternateContactFileBody);
	        if(instrumentFileBody!=null)
	        	reqEntity.addPart("Customer[photo_instrument]", instrumentFileBody);
            
	        
            reqEntity.addPart("username", new StringBody(user.username));
            reqEntity.addPart("pass", new StringBody(user.password));

            totalSize = reqEntity.getContentLength();
            
            post.setEntity(reqEntity);
            HttpResponse response = client.execute(post);
            resEntity = response.getEntity();
            String responseStr = EntityUtils.toString(resEntity);
            if(responseStr != null) {
	        	if(responseStr.contains("OK")) {
	        		deleteUploadedCustomer(customer.id);
	        		showNotification("Upload successfull",false);
	        		Log.w("NWC_SERVICE_UPLOAD","UPLOADED CUSTOMER HCnum:"+customer.house_connection_number);
	        	} else {
	        		if(responseStr.contains("ERROR"))
	        			setCustomerError(customer.id, responseStr);
        			Log.w("FAILED_CUSTOMER","FAILED TO UPLOAD CUSTOMER HCN:"+customer.house_connection_number);
        			Log.w("ERROR SERVICE CUSTOMER",""+responseStr);
	        	}
	        }
		} catch (Exception e) {
			Log.w("NWC_SERVICE_PREMISE","ERROR UPLOADING",e);
			
		}
    }
    

    /**
     * SEND WATER READING ITEM TO SERVER
     * @param water_reading
     */
    private void syncWaterMeterReadingItem (JSONWaterReadingItem reading){

		try {
        	
			currentProgress=0;
			totalSize=0;
			//load user
			SharedPreferences prefs=this.getSharedPreferences(GlobalFunctionsAndConstants.PREFS_NAME, 0);
        	JSONUSerItem user=new JSONUSerItem();
        	user.username=prefs.getString("serviceUsername", "");
        	user.password=prefs.getString("servicePass", "");
        	if(user.password.equals("")){
        		user.username=prefs.getString("username", "");
        		user.password=prefs.getString("pass", "");
        	}
	    	
	        String urlString = ServiceNwcSync.this.getResources().getString(R.string.url_water_meter_status);
	        HttpClient client = new DefaultHttpClient();
	        HttpEntity resEntity;
	        HttpPost post = new HttpPost(urlString);
	        
	        FileBody photoFileBody=null;
	        if(reading.photo_water_meter!=null)
	        	photoFileBody = new FileBody(new File(reading.photo_water_meter));
	        
	        
	        List<String> dynamicPictures = null;
	        if(reading.dynamic_photos!=null){
	        	if(reading.dynamic_photos.length()>0){
	        		Log.w("SYNC DYNAMIC","BEFORE DECODE");
	        		java.lang.reflect.Type type = new TypeToken<List<String>>(){}.getType();
	        		
	        		Gson gson=new Gson();
	        		try {
						dynamicPictures=gson.fromJson(reading.dynamic_photos, type);
					} catch (Exception e) {}
	        		
	        	}
	        }
	        
	        CustomMultiPartEntity reqEntity = new CustomMultiPartEntity(new ProgressListener()
			{
				public void transferred(long num)
				{
					if(((int) ((num / (float) totalSize) * 100) - currentProgress)>10){
						currentProgress=(int) ((num / (float) totalSize) * 100);
						showNotification("Uploading premise entry " + currentProgress + "%",false);
					}
				}
			});
	        
	        
	        
	        /** parse variables + gps **/
	        
	        if(dynamicPictures != null){
    			for (String pic : dynamicPictures) {
    				FileBody tmpFileBody;
    				if(pic!=null){
    					tmpFileBody=null;
    					try {
    						tmpFileBody = new FileBody(new File(pic));
						} catch (Exception e) {}
    					if(tmpFileBody != null){
    						reqEntity.addPart("DynamicPictures[]", tmpFileBody);
    						Log.w("SYNC DYNAMIC","ADDED PIC: "+pic);
    					}
    				}
    				
				}
    		}
	        //bug fix long time to find, go to hell null, go to hell you little nullable bitch, thanks verni
	        if(reading.premise_name==null){
	        	reading.premise_name="";
	        }
	        
			reqEntity.addPart("WaterMeterReading[gps_latitude_created]", new StringBody(reading.gps_latitude_created));
	        reqEntity.addPart("WaterMeterReading[gps_longitude_created]", new StringBody(reading.gps_longitude_created));
	        reqEntity.addPart("WaterMeterReading[gps_altitude_created]", new StringBody(reading.gps_altitude_created));
	        reqEntity.addPart("WaterMeterReading[area_name]", new StringBody(reading.area_name));

	        reqEntity.addPart("WaterMeterReading[district_id]", new StringBody(Integer.toString(reading.district_id)));
			
	        reqEntity.addPart("WaterMeterReading[house_connection_number]", new StringBody(reading.house_connection_number));
	        reqEntity.addPart("WaterMeterReading[sec_db_number]", new StringBody(reading.sec_db_number));
	        reqEntity.addPart("WaterMeterReading[water_meter_number]", new StringBody(reading.water_meter_number));
	        reqEntity.addPart("WaterMeterReading[water_meter_reading_number]", new StringBody(reading.water_meter_reading));
	        Log.w("SYNC DYNAMIC","premise name: "+reading.house_connection_number);
	        reqEntity.addPart("WaterMeterReading[premise_name]", new StringBody(reading.premise_name));
	        reqEntity.addPart("WaterMeterReading[manual_source]", new StringBody(Integer.toString(reading.manual_source)));
	
	        /** END PARSE VARIABLES **/
	        
	        
	        if(photoFileBody!=null)
	        	reqEntity.addPart("WaterMeterReading[photo_instrument]", photoFileBody);
	
	        
	
	        //add username & password
	        reqEntity.addPart("username", new StringBody(user.username));
	        reqEntity.addPart("pass", new StringBody(user.password));
	
	        totalSize = reqEntity.getContentLength();
	        
	        post.setEntity(reqEntity);
	        HttpResponse response = client.execute(post);
	        resEntity = response.getEntity();
	        String responseStr = EntityUtils.toString(resEntity);
	        if(responseStr != null) {
	        	if(responseStr.contains("OK") && responseStr.length()<5) {
	        		deleteUploadedWaterReading(reading.id);
	        		showNotification("Upload successfull",false);
	        		Log.w("NWC_SERVICE_READING","UPLOADED READING HCN:"+reading.house_connection_number);
	        	} else {
	        		if(responseStr.contains("ERROR"))
	        			setWaterReadingError(reading.id, responseStr);
        			Log.w("FAILED_READING","FAILED TO UPLOAD PREMISE HCN:"+reading.house_connection_number);
        			Log.w("ERROR SERVICE READING",""+responseStr);
	        	}
	        }
		} catch (Exception e) {
			Log.w("NWC_SERVICE_READING","ERROR UPLOADING",e);
			
		}
		
    	
    } 
    

    /**
     * SEND LEAK REPORT ITEM TO SERVER
     * @param water_reading
     */
    private void syncWaterLeakItem (JSONReportLeakItem reading){

		try {
        	
			currentProgress=0;
			totalSize=0;
			//load user
			SharedPreferences prefs=this.getSharedPreferences(GlobalFunctionsAndConstants.PREFS_NAME, 0);
        	JSONUSerItem user=new JSONUSerItem();
        	user.username=prefs.getString("serviceUsername", "");
        	user.password=prefs.getString("servicePass", "");
        	if(user.password.equals("")){
        		user.username=prefs.getString("username", "");
        		user.password=prefs.getString("pass", "");
        	}
	    	
	        String urlString = ServiceNwcSync.this.getResources().getString(R.string.url_report_leak);
	        HttpClient client = new DefaultHttpClient();
	        HttpEntity resEntity;
	        HttpPost post = new HttpPost(urlString);
	        
	        FileBody photoFileBody=null;
	        if(reading.photo_water_leak!=null)
	        	photoFileBody = new FileBody(new File(reading.photo_water_leak));
	        
	        List<String> dynamicPictures = null;
	        if(reading.dynamic_photos!=null){
	        	if(reading.dynamic_photos.length()>0){
	        		Log.w("SYNC DYNAMIC","BEFORE DECODE");
	        		java.lang.reflect.Type type = new TypeToken<List<String>>(){}.getType();
	        		
	        		Gson gson=new Gson();
	        		try {
						dynamicPictures=gson.fromJson(reading.dynamic_photos, type);
					} catch (Exception e) {}
	        		
	        	}
	        }
	        
	        CustomMultiPartEntity reqEntity = new CustomMultiPartEntity(new ProgressListener()
			{
				public void transferred(long num)
				{
					if(((int) ((num / (float) totalSize) * 100) - currentProgress)>10){
						currentProgress=(int) ((num / (float) totalSize) * 100);
						showNotification("Uploading premise entry " + currentProgress + "%",false);
					}
				}
			});
	        
	        
	        
	        /** parse variables + gps **/

    		if(dynamicPictures != null){
    			for (String pic : dynamicPictures) {
    				FileBody tmpFileBody;
    				if(pic!=null){
    					tmpFileBody=null;
    					try {
    						tmpFileBody = new FileBody(new File(pic));
						} catch (Exception e) {}
    					if(tmpFileBody != null){
    						reqEntity.addPart("DynamicPictures[]", tmpFileBody);
    						Log.w("SYNC DYNAMIC","ADDED PIC: "+pic);
    					}
    				}
    				
				}
    		}
	        
	        
			reqEntity.addPart("LeakReport[gps_latitude_created]", new StringBody(reading.gps_latitude_created));
	        reqEntity.addPart("LeakReport[gps_longitude_created]", new StringBody(reading.gps_longitude_created));
	        reqEntity.addPart("LeakReport[gps_altitude_created]", new StringBody(reading.gps_altitude_created));
			reqEntity.addPart("LeakReport[ticket_serial_number]", new StringBody(reading.ticket_serial_number));
	        reqEntity.addPart("LeakReport[manual_source]", new StringBody(Integer.toString(reading.manual_source)));
	        reqEntity.addPart("LeakReport[illegal_connect]", new StringBody(Integer.toString(reading.illegal_connect)));

	        reqEntity.addPart("LeakReport[leak_type]", new StringBody(Integer.toString(reading.leak_type)));
	        reqEntity.addPart("LeakReport[violation_penalty]", new StringBody(reading.violation_penalty));
	        reqEntity.addPart("LeakReport[water_meter_reading_number]", new StringBody(reading.water_meter_reading_number));
	        reqEntity.addPart("LeakReport[ticket_id]", new StringBody(reading.ticket_id));
	        reqEntity.addPart("LeakReport[manual_water_meter_number]", new StringBody(reading.water_meter_number));
	        reqEntity.addPart("LeakReport[manual_sec_db_number]", new StringBody(reading.sec_db_number));
	        reqEntity.addPart("LeakReport[manual_premise_name]", new StringBody(reading.premise_name));
	        reqEntity.addPart("LeakReport[premise_type_id]", new StringBody(reading.premise_type_id));
	        if(reading.house_connection_number!=null)
				reqEntity.addPart("LeakReport[house_connection_number]", new StringBody(reading.house_connection_number));
	        
			reqEntity.addPart("LeakReport[leak_description]", new StringBody(reading.description));
	
	        /** END PARSE VARIABLES **/
	        
	        
	        if(photoFileBody!=null)
	        	reqEntity.addPart("LeakReport[photo_leak]", photoFileBody);
	
	        
	
	        //add username & password
	        reqEntity.addPart("username", new StringBody(user.username));
	        reqEntity.addPart("pass", new StringBody(user.password));
	
	        totalSize = reqEntity.getContentLength();
	        
	        post.setEntity(reqEntity);
	        HttpResponse response = client.execute(post);
	        resEntity = response.getEntity();
	        String responseStr = EntityUtils.toString(resEntity);
	        if(responseStr != null) {
	        	if(responseStr.contains("OK") && responseStr.length()<5) {
	        		deleteUploadedWaterLeak(reading.id);
	        		showNotification("Upload successfull",false);
	        		Log.w("NWC_SERVICE_LEAK","UPLOADED LEAK");
	        	} else {
	        		if(responseStr.contains("ERROR"))
	        			setWaterLeakError(reading.id, responseStr);
        			Log.w("FAILED_READING","FAILED TO UPLOAD LEAK ");
        			Log.w("ERROR SERVICE LEAK","Err:"+responseStr);
	        	}
	        }
		} catch (Exception e) {
			Log.w("NWC_SERVICE_LEAK","ERROR UPLOADING",e);
		}
    } 
    
    /**
     * SEND PREMISE ITEM TO SERVER
     * @param premise
     */
    private void syncPremise (JSONPremiseItem premise){

		try {
        	
			currentProgress=0;
			totalSize=0;
			//load user
			SharedPreferences prefs=this.getSharedPreferences(GlobalFunctionsAndConstants.PREFS_NAME, 0);
        	JSONUSerItem user=new JSONUSerItem();
        	user.username=prefs.getString("serviceUsername", "");
        	user.password=prefs.getString("servicePass", "");
        	if(user.password.equals("")){
        		user.username=prefs.getString("username", "");
        		user.password=prefs.getString("pass", "");
        	}

        	String urlString;
			if(premise.only_images==1)
				urlString = ServiceNwcSync.this.getResources().getString(R.string.url_premise_images);
			else
				urlString = ServiceNwcSync.this.getResources().getString(R.string.url_premise);
			
	        HttpClient client = new DefaultHttpClient();
	        HttpEntity resEntity;
	        HttpPost post = new HttpPost(urlString);
	        
	        FileBody house1FileBody=null;
	        if(premise.photo_house1!=null)
	        	house1FileBody = new FileBody(new File(premise.photo_house1));
	        
	        FileBody house2FileBody=null;
	        if(premise.photo_house2!=null)
	        	house2FileBody = new FileBody(new File(premise.photo_house2));
	        
	        FileBody house3FileBody=null;
	        if(premise.photo_house3!=null)
	        	house3FileBody = new FileBody(new File(premise.photo_house3));
	        
	        FileBody connectionFileBody=null;
	        if(premise.photo_premise_connection!=null)
	        	connectionFileBody = new FileBody(new File(premise.photo_premise_connection));
	        
	        
	        FileBody STCFileBody=null;
	        if(premise.photo_stc!=null)
	        	STCFileBody = new FileBody(new File(premise.photo_stc));
	        
	        FileBody SECFileBody=null;
	        if(premise.photo_sec!=null)
	        	SECFileBody = new FileBody(new File(premise.photo_sec));
	        
	        
	        CustomMultiPartEntity reqEntity = new CustomMultiPartEntity(new ProgressListener()
			{
				public void transferred(long num)
				{
					if(((int) ((num / (float) totalSize) * 100) - currentProgress)>10){
						currentProgress=(int) ((num / (float) totalSize) * 100);
						showNotification("Uploading premise entry " + currentProgress + "%",false);
					}
				}
			});
	        
	        
	        reqEntity.addPart("Premise[house_connection_number]", new StringBody(premise.house_connection_number));
	      	
	        if(premise.gps_latitude!=null && premise.gps_altitude!=null && premise.gps_longitude!=null){
		        reqEntity.addPart("Premise[gps_latitude]", new StringBody(premise.gps_latitude));
		        reqEntity.addPart("Premise[gps_longitude]", new StringBody(premise.gps_longitude));
		        reqEntity.addPart("Premise[gps_altitude]", new StringBody(premise.gps_altitude));
	        }
	        if(premise.gps_latitude_created!=null && premise.gps_longitude_created!=null && premise.gps_altitude_created!=null){
		        reqEntity.addPart("Premise[gps_latitude_created]", new StringBody(premise.gps_latitude_created));
		        reqEntity.addPart("Premise[gps_longitude_created]", new StringBody(premise.gps_longitude_created));
		        reqEntity.addPart("Premise[gps_altitude_created]", new StringBody(premise.gps_altitude_created));
	        }
	        
	        /** parse variables + gps **/
	        Log.w("SYNC PREMISE ONLY IMAGES", Integer.toString(premise.only_images));
	        
	        if(premise.only_images!=1) {
	        	//cijeli premise
	 
		        reqEntity.addPart("Premise[premise_id]", new StringBody(premise.premise_id));
		        reqEntity.addPart("Premise[stc_db_number]", new StringBody(premise.stc_db_number));
		        reqEntity.addPart("Premise[sec_db_number]", new StringBody(premise.sec_db_number));
		        reqEntity.addPart("Premise[total_electrical_meters]", new StringBody(premise.total_electrical_meters));
		        reqEntity.addPart("Premise[floor_count]", new StringBody(premise.floor_count));
		        reqEntity.addPart("Premise[premise_name]", new StringBody(premise.premise_name));
		        reqEntity.addPart("Premise[use_of_building]", new StringBody(premise.use_of_building));
		        reqEntity.addPart("Premise[water_meter_number]", new StringBody(premise.water_meter_number));
		        reqEntity.addPart("Premise[water_meter_status_id]", new StringBody(premise.water_meter_status));
		
		        reqEntity.addPart("Premise[premise_type_id]", new StringBody(premise.premise_type_id));
		        reqEntity.addPart("hcnAutoGenerated", new StringBody(premise.hcnAutoGenerated));
		        
		        //add district
		        reqEntity.addPart("district", new StringBody(premise.district));
		
	        }
			
	        /** END PARSE VARIABLES **/
	        
	        
	        if(house1FileBody!=null)
	        	reqEntity.addPart("Premise[photo_house]", house1FileBody);
	
	        if(house2FileBody!=null)
	        	reqEntity.addPart("Premise[photo_house_2]", house2FileBody);
	
	        if(house3FileBody!=null)
	        	reqEntity.addPart("Premise[photo_house_3]", house3FileBody);
	
	        if(connectionFileBody!=null)
	        	reqEntity.addPart("Premise[photo_premise_connection]", connectionFileBody);
		
	        if(STCFileBody!=null)
	        	reqEntity.addPart("Premise[photo_stc]", STCFileBody);
	
	        if(SECFileBody!=null)
	        	reqEntity.addPart("Premise[photo_sec]", SECFileBody);
	
	        //add username & password
	        reqEntity.addPart("username", new StringBody(user.username));
	        reqEntity.addPart("pass", new StringBody(user.password));
	
	        totalSize = reqEntity.getContentLength();
	        
	        post.setEntity(reqEntity);
	        HttpResponse response = client.execute(post);
	        resEntity = response.getEntity();
	        String responseStr = EntityUtils.toString(resEntity);
	        if(responseStr != null) {
	        	if(responseStr.contains("OK") && responseStr.length()<5) {
	        		deleteUploadedPremise(premise.id);
	        		showNotification("Upload successfull",false);
	        		Log.w("NWC_SERVICE_PREMISE","UPLOADED PREMISE HCN:"+premise.house_connection_number);
	        	} else {
	        		if(responseStr.contains("ERROR"))
	        			setPremiseError(premise.id, responseStr);
        			Log.w("FAILED_PREMISE","FAILED TO UPLOAD PREMISE HCN:"+premise.house_connection_number);
        			Log.w("ERROR SERVICE CUSTOMER",""+responseStr);
	        	}
	        }
		} catch (Exception e) {
			Log.w("NWC_SERVICE_PREMISE","ERROR UPLOADING",e);
			
		}
		
    	
    }

    /**
     * DELETES premise item from database!
     * @param id
     */
    private void deleteUploadedPremise(int id){
    	//DELETE FORM DATABSE UPLOAD SUCCESSFULL
		MyDataSource dataSource=new MyDataSource(this);
		dataSource.open();
		dataSource.deletePremiseItem(id);
		dataSource.close();
    }
    
    /**
     * DELETES reading item from database!
     * @param id
     */
    private void deleteUploadedWaterReading(int id){
    	//DELETE FORM DATABSE UPLOAD SUCCESSFULL
		MyDataSource dataSource=new MyDataSource(this);
		dataSource.open();
		dataSource.deleteWaterMeterReadingItem(id);
		dataSource.close();
    }
    
    
    /**
     * DELETES leak item from database!
     * @param id
     */
    private void deleteUploadedWaterLeak(int id){
    	//DELETE FORM DATABSE UPLOAD SUCCESSFULL
		MyDataSource dataSource=new MyDataSource(this);
		dataSource.open();
		dataSource.deleteWaterLeakItem(id);
		dataSource.close();
    }
    
    
    /**
     * DELETES customer item from database!
     * @param id
     */
    private void deleteUploadedCustomer(int id){
    	//DELETE FORM DATABSE UPLOAD SUCCESSFULL
		MyDataSource dataSource=new MyDataSource(this);
		dataSource.open();
		dataSource.deleteCustomerItem(id);
		dataSource.close();
    }
    
    private void setPremiseError(int id,String err){
		MyDataSource dataSource=new MyDataSource(this);
		dataSource.open();
		dataSource.setPremiseError(id, err);
		dataSource.close();
    }
    
    private void setCustomerError(int id,String err){
		MyDataSource dataSource=new MyDataSource(this);
		dataSource.open();
		dataSource.setCustomerError(id, err);
		dataSource.close();
    }

    private void setWaterReadingError(int id,String err){
		MyDataSource dataSource=new MyDataSource(this);
		dataSource.open();
		dataSource.setWaterMeterReadingError(id, err);
		dataSource.close();
    }

    private void setWaterLeakError(int id,String err){
		MyDataSource dataSource=new MyDataSource(this);
		dataSource.open();
		dataSource.setWaterLeakError(id, err);
		dataSource.close();
    }



}
