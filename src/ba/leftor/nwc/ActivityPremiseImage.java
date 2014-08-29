package ba.leftor.nwc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;

import ba.leftor.nwc.CustomMultiPartEntity.ProgressListener;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityPremiseImage extends Activity{
	
	//define variables for rotation aware task (so upload can resume if orientation chnages during upload)
	private SubmitPremiseForm task=null;
	private SearchPremise searchTask=null;
    private ProgressDialog dialog;
	
    //REQUEST variable for map 
    static final int REQUEST_MAP_ACTIVITY=55;
    
	//define variables needed for taking photos and selecting photo from gallery
	static final int REQUEST_TAKE_HOUSE1_PHOTO=1;
	static final int REQUEST_TAKE_HOUSE2_PHOTO=2;
	static final int REQUEST_TAKE_HOUSE3_PHOTO=3;
	static final int REQUEST_TAKE_CONNECTION_PHOTO=4;
	static final int REQUEST_TAKE_STC_PHOTO=6;
	static final int REQUEST_TAKE_SEC_PHOTO=7;
	
	static final int REQUEST_SELECT_HOUSE1_PHOTO=92;
	static final int REQUEST_SELECT_HOUSE2_PHOTO=93;
	static final int REQUEST_SELECT_HOUSE3_PHOTO=94;
	static final int REQUEST_SELECT_CONNECTION_PHOTO=95;
	static final int REQUEST_SELECT_STC_PHOTO=97;
	static final int REQUEST_SELECT_SEC_PHOTO=98;
	
	private String mCurrentPhotoPath;
	private AlbumStorageDirFactory mAlbumStorageDirFactory = null;
	File f=null;

	//define images first and private variables that will be used to store their paths after they have been taken
	private ImageView imgHouse1;
	private ImageView imgHouse2;
	private ImageView imgHouse3;
	private ImageView imgConnection;
	private ImageView imgSTC;
	private ImageView imgSEC;
	
	private String pathImgHouse1;
	private String pathImgHouse2;
	private String pathImgHouse3;
	private String pathImgConnection;
	private String pathImgSTC;
	private String pathImgSEC;
	
	private Boolean canDeleteImgHouse1=false;
	private Boolean canDeleteImgHouse2=false;
	private Boolean canDeleteImgHouse3=false;
	private Boolean canDeleteImgConnection=false;
	private Boolean canDeleteImgSTC=false;
	private Boolean canDeleteImgSEC=false;
	
	//my location
	private String myLat;
	private String myLng;
	private String myAlt;
	private String hcn = null;

	private String premiseLat;
	private String premiseLng;
	private String premiseAlt;
	private TextView txtPremiseLat;
	private TextView txtPremiseLng;
	
	//my location
	private MyLocation myLocation;
	private Boolean gettingLocation=false; //to continue finding location on orientation change
	
	
	//HOUSE CONNECTION KONWON-UNKNOWN, DISTRICT AND SEARCH FIELDS
	private Boolean didSearch=false; //USER MUST FIRST SEARCH BEFORE GETTING PERMISSION TO FILL FORM
	private EditText txtSearchHcn;
	private EditText txtSearchWaterMeterNumber;
	private Button btnSearch;
	
	private Button btnSubmit;
	private Button btnGetLocation;
	private Button btnGetLocationNoMap;
	
	private JSONRejectedPremiseItem loadedPremise = null;
	// text views and their layouts
	private TextView txtFullName;
	private TextView txtDistrict;
	private TextView txtHcn;
	private TextView txtWmn;

	private LinearLayout lnDistrict;
	private LinearLayout lnHcn;
	private LinearLayout lnWmn;
	private LinearLayout lnFullName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.premise_image_form);
		
		
		// display user name on top
		TextView txtWelcome = (TextView) findViewById(R.id.txtWelcome);
		JSONUSerItem user = GlobalFunctionsAndConstants.getUser(this);
		if (user.fullName.length() > 1)
			txtWelcome.setText(ActivityPremiseImage.this.getResources().getString(R.string.welcome) + user.fullName);
		else
			txtWelcome.setText(ActivityPremiseImage.this.getResources().getString(R.string.welcome) + user.username);
		
		//get album storage dir
		mAlbumStorageDirFactory = new BaseAlbumDirFactory();
		
		//find images in layout and register for context menu
		imgHouse1=(ImageView)this.findViewById(R.id.imgHouse1);
		imgHouse2=(ImageView)this.findViewById(R.id.imgHouse2);
		imgHouse3=(ImageView)this.findViewById(R.id.imgHouse3);
		imgConnection=(ImageView)this.findViewById(R.id.imgConnection);
		imgSTC=(ImageView)this.findViewById(R.id.imgSTC);
		imgSEC=(ImageView)this.findViewById(R.id.imgSEC);
		registerForContextMenu(imgHouse1);
		registerForContextMenu(imgHouse2);
		registerForContextMenu(imgHouse3);
		registerForContextMenu(imgConnection);
		registerForContextMenu(imgSTC);
		registerForContextMenu(imgSEC);
		imgHouse1.setOnClickListener(photoListener);
		imgHouse2.setOnClickListener(photoListener);
		imgHouse3.setOnClickListener(photoListener);
		imgConnection.setOnClickListener(photoListener);
		imgSTC.setOnClickListener(photoListener);
		imgSEC.setOnClickListener(photoListener);
		txtPremiseLat=(TextView)findViewById(R.id.txtPremiseLat);
		txtPremiseLng=(TextView)findViewById(R.id.txtPremiseLng);
		
		
		//find search fields, and hcn-district control fields
		txtSearchHcn=(EditText)findViewById(R.id.txtSearchHcn);
		
		txtSearchWaterMeterNumber=(EditText)findViewById(R.id.txtSearchWaterMeterNumber);

		// text views
		txtFullName = (TextView) findViewById(R.id.txtFullName);
		txtDistrict = (TextView) findViewById(R.id.txtDistrict);
		txtHcn = (TextView) findViewById(R.id.txtHcn);
		txtFullName = (TextView) findViewById(R.id.txtFullName);
		txtWmn = (TextView) findViewById(R.id.txtWmn);
		lnDistrict = (LinearLayout) findViewById(R.id.lnDistrict);
		lnHcn = (LinearLayout) findViewById(R.id.lnHcn);
		lnWmn = (LinearLayout) findViewById(R.id.lnWmn);
		lnFullName = (LinearLayout) findViewById(R.id.lnFullName);
		
		//submit form button
		btnSubmit=(Button)findViewById(R.id.btnSubmit);
		btnSubmit.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				validateAndSubmit();
			}
		});
		
		
		//search form button
		btnSearch=(Button)findViewById(R.id.btnSearch);		
		btnSearch.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				String hcn=txtSearchHcn.getText().toString().trim();
				String wmn=txtSearchWaterMeterNumber.getText().toString().trim();
				
				Boolean error=false;
				String errText="";
				if(hcn.equals("") && wmn.equals("")){
					error=true;
					errText=getResources().getString(R.string.error_enter_search);
					txtSearchWaterMeterNumber.setError("You must first search for house connection number or water meter number!");
				} else {
					txtSearchHcn.setError(null);
					txtSearchWaterMeterNumber.setError(null);
				}
				
				if(error){
					new AlertDialog.Builder(ActivityPremiseImage.this)
				    .setTitle("ERROR")
				    .setMessage(errText)
				    .setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int which) { 
				            dialog.dismiss();
				        }
				     })
				     .show();
				} else {
					searchPremise();
				}
				
				
			}
		});
		
		//get location button
		btnGetLocation=(Button)findViewById(R.id.btnGetLocation);
		btnGetLocation.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				startActivityForResult(new Intent(ActivityPremiseImage.this,ActivityMap.class), REQUEST_MAP_ACTIVITY);
				
			}
		});
		
		//get location without map button
		btnGetLocationNoMap=(Button)findViewById(R.id.btnGetLocationNoMap);
		btnGetLocationNoMap.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				getGPSLocation();				
			}
		});
		
		//rotation aware task
		try {
			task=(SubmitPremiseForm)getLastNonConfigurationInstance();
		} catch (Exception e) {
		}
		if(task!=null){
			dialog = new ProgressDialog(this);
            dialog.setMessage(getResources().getString(R.string.sending));
            dialog.setIndeterminate(false);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setProgress(0);
            dialog.show();
			task.attach(this);
			
			updateProgress(task.getProgress());
		    
			if (task.getProgress()>=100) {
				markAsDone();
			}
		}
		
		//rotation aware task for searching
		try {
			searchTask=(SearchPremise)getLastNonConfigurationInstance();
		} catch (Exception e) {
		}
		if(searchTask!=null){
			dialog = new ProgressDialog(this);
            dialog.setMessage(getResources().getString(R.string.sending));
            dialog.setIndeterminate(false);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setProgress(0);
            dialog.show();
            searchTask.attach(this);
			
		    
			if (searchTask.response_str!=null) {
				markSearchAsDone();
			}
		}
		
	}
	
	//validate and submit form
	private void validateAndSubmit(){
		Boolean error=false;

		if((myLat==null || myLat.equalsIgnoreCase("0.000000")) && error==false){
			new AlertDialog.Builder(ActivityPremiseImage.this)
		    .setTitle(getResources().getString(R.string.error_location_not_set))
		    .setMessage(getResources().getString(R.string.please_click_get_location))
		    .setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) { 
		            dialog.dismiss();
		        }
		     })
		     .show();
			
			error=true;
		}
		
		
		if(pathImgHouse1==null && pathImgHouse2==null && pathImgHouse3==null && pathImgConnection==null && pathImgSTC==null && pathImgSEC==null){
			new AlertDialog.Builder(ActivityPremiseImage.this)
		    .setTitle(getResources().getString(R.string.errors))
		    .setMessage(getResources().getString(R.string.error_photos))
		    .setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) { 
		            dialog.dismiss();
		        }
		     })
		     .show();
			
			error=true;
		}
		
		if(!error){
			dialog = new ProgressDialog(this);
	        dialog.setMessage(getResources().getString(R.string.sending));
	        dialog.setIndeterminate(false);
	        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	        dialog.setProgress(0);
	        dialog.show();
			task=new SubmitPremiseForm(this);
			task.execute();
		}
	}
	
	
    static class SubmitPremiseForm extends AsyncTask<Void, Integer, Void> {

    	private ActivityPremiseImage activity=null;
        private int percent = 0;
        private long totalSize;
        private int  currentProgress;
        private String response_str=null;

        SubmitPremiseForm(ActivityPremiseImage activity) {
            attach(activity);
        }
        void detach() {
        	activity=null;
        }
          
        void attach(ActivityPremiseImage activity) {
        	this.activity=activity;
        }

        @Override
        protected Void doInBackground(Void... arg) {
        	
        	try {
            	response_str=null;
            	
            	//add premise item to database
            	JSONPremiseItem item=new JSONPremiseItem();
            	
            	if (activity.hcn != null) {
					item.house_connection_number = activity.hcn.trim();
				}
            	item.only_images = 1;//only images to send
        		item.gps_latitude_created=activity.myLat;
        		item.gps_longitude_created=activity.myLng;
        		item.gps_altitude_created=activity.myAlt;
        		item.gps_latitude=activity.premiseLat;
        		item.gps_longitude=activity.premiseLng;
        		item.gps_altitude=activity.premiseAlt;
        		
        		item.photo_house1=activity.pathImgHouse1;
        		item.photo_house2=activity.pathImgHouse2;
        		item.photo_house3=activity.pathImgHouse3;
        		item.photo_premise_connection=activity.pathImgConnection;
        		item.photo_stc=activity.pathImgSTC;
        		item.photo_sec=activity.pathImgSEC;
        		
            	Log.w("Snimam only images:","only_images:"+Integer.toString(item.only_images)+" --WMSTATUS:"+item.water_meter_status + " --hcn:"+item.house_connection_number+" --district:"+item.district);
            	MyDataSource dataSource=new MyDataSource(this.activity);
            	dataSource.open();
            	long insertStatus=dataSource.addPremiseItemToDb(item);
            	dataSource.close();
            	if(insertStatus!=-1)
            		response_str="OK";
            	else 
            		response_str="ERROR:ERROR WHILE SAVING ITEM TO DB.PLEASE TRY AGAIN. IF this error continues please CONTACT SUPPROT AND NOTIFY ABOUT THIS ERROR.";
            } catch (Exception e) {
                Log.w("GRE��KA U SNIMANJU U BAZU","ERR",e);
            }
            return null;
            
        }
        int getProgress() {
        	return(percent);
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            percent=progress[0];
            if(activity!=null)
            	activity.updateProgress(percent);
            
        }

        @Override
        protected void onPostExecute(Void result) {
            if(activity!=null){
	        	try {
	                activity.markAsDone();
	                
	            } catch(Exception e) {
	            	Log.w("GRE��KA U MARKIRANJU APLIKACIJE KAO DONE","ERR",e);
	            }
            }

        }

    }
    
    private void updateProgress(int percent){
    	dialog.setProgress(percent);
    }
    
    private void toggleMap(boolean state){
    	LinearLayout location=(LinearLayout)findViewById(R.id.linearLayout9);
    	if(!state){
    		location.setVisibility(View.GONE);
    	}else{
    		location.setVisibility(View.VISIBLE);
    	}
    }
	
    private void markAsDone(){
    	if(dialog!=null)
    		dialog.dismiss();
    	if(task.response_str!=null && task.response_str.contains("ERROR")){
			new AlertDialog.Builder(this)
		    .setTitle(getResources().getString(R.string.errors))
		    .setMessage(task.response_str.substring(6))
		    .setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) { 
		            dialog.dismiss();
		        }
		     })
		     .show();
			task.response_str=null;
			task=null;
    	} else if(task.response_str!=null && task.response_str.contains("OK")){
    		clearAll();
    		

    		//set alarm for service !!!
    		AlarmManager service = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
		    Intent i = new Intent(this, RecieverStartService.class);
		    PendingIntent pending = PendingIntent.getBroadcast(this, 0, i,PendingIntent.FLAG_CANCEL_CURRENT);
		    Calendar cal = Calendar.getInstance();
		    service.cancel(pending);
		    //
		    // Fetch every 1 min
		    // InexactRepeating allows Android to optimize the energy consumption
		    service.setRepeating(AlarmManager.RTC_WAKEUP,
		        cal.getTimeInMillis(), 60*15*1000, pending);
		    
    		new AlertDialog.Builder(this)
		    .setTitle(getResources().getString(R.string.success))
		    .setMessage(getResources().getString(R.string.form_succ_saved))
		    .setPositiveButton(getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) { 
		            dialog.dismiss();
		        }
		     })
		     .show();
    		task=null;
    		
    	}
    }
    
    
    /** SEARCH PREMISE ASYNC TASK **/
    static class SearchPremise extends AsyncTask<Void, Integer, Void> {

    	private ActivityPremiseImage activity=null;
        private String response_str=null;

        SearchPremise(ActivityPremiseImage activity) {
            attach(activity);
        }
        void detach() {
        	activity=null;
        }
          
        void attach(ActivityPremiseImage activity) {
        	this.activity=activity;
        }

        @Override
        protected Void doInBackground(Void... arg) {
        	
            try {
            	response_str=null;
            	//search premise item
            	String urlString = activity.getResources().getString(R.string.url_premise_images_search);
                HttpClient client = new DefaultHttpClient();
                HttpEntity resEntity;
                HttpPost post = new HttpPost(urlString);
                CustomMultiPartEntity reqEntity = new CustomMultiPartEntity(new ProgressListener()
    			{
    				public void transferred(long num)
    				{
    					
    				}
    			});
                
    			SharedPreferences prefs=activity.getSharedPreferences(GlobalFunctionsAndConstants.PREFS_NAME, 0);
            	JSONUSerItem user=new JSONUSerItem();
            	user.username=prefs.getString("serviceUsername", "");
            	user.password=prefs.getString("servicePass", "");
            	if(user.password.equals("")){
            		user.username=prefs.getString("username", "");
            		user.password=prefs.getString("pass", "");
            	}
            	reqEntity.addPart("username", new StringBody(user.username));
                reqEntity.addPart("pass", new StringBody(user.password));
                
                
                //get sarch fields and add to req entity
                String hcn="";
                String wmn="";
                hcn=this.activity.txtSearchHcn.getText().toString().trim();
				wmn=this.activity.txtSearchWaterMeterNumber.getText().toString().trim();
                
                if(hcn.length()>0){
                	reqEntity.addPart("house_connection_number", new StringBody(hcn));
                }
                if(wmn.length()>0){
                	reqEntity.addPart("water_meter_number", new StringBody(wmn));
                }
                
                post.setEntity(reqEntity);
                HttpResponse response = client.execute(post);
                resEntity = response.getEntity();
                response_str = EntityUtils.toString(resEntity);
                
            } catch (Exception e) {
                Log.w("PREMISE SARCH CONN ERROR","ERROR",e);
                response_str="ERROR: " + activity.getResources().getString(R.string.require_connection);
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void result) {
            if(activity!=null){
	        	try {
	                activity.markSearchAsDone();
	                
	            } catch(Exception e) {
	            }
            }

        }

    }
    
    private void markSearchAsDone(){
    	if(dialog!=null)
    		dialog.dismiss();
    	if(searchTask==null) return;
    	String response_str=searchTask.response_str;
    	if(response_str != null) {
        	if(response_str.contains("ERROR")) {
        		Log.w("PREMISE SEARCH ERROR","PREMISE ERROR:"+response_str);
        		Toast.makeText(this, response_str, Toast.LENGTH_LONG).show();
        	} else if(response_str.contains("NOT FOUND")){
        		
    			Log.w("PREMISE SEARCH","NOT FOUND"+response_str);
    			
    			clearAll();
    			didSearch=true;
    			new AlertDialog.Builder(this)
    			.setTitle(getResources().getString(R.string.errors))
				.setMessage(getResources().getString(R.string.premise_not_found))
    		    .setPositiveButton(getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
    		        public void onClick(DialogInterface dialog, int which) { 
    		            dialog.dismiss();
    		        }
    		     })
    		     .show();
    			toggleForm(true);
        	} else if (response_str.contains("DUPLICATE")) {
				new AlertDialog.Builder(this)
				.setTitle(getResources().getString(R.string.errors))
				.setMessage(
						getResources().getString(
								R.string.error_duplicate_premise_images))
				.setPositiveButton(
						getResources().getString(R.string.OK),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						}).show();
        	} else {
        		Gson gson=new Gson();
        		JSONRejectedPremiseItem premise;
        		try{
        			premise=gson.fromJson(response_str, JSONRejectedPremiseItem.class);
        			Log.w("PREMISE SEARCH","parsiran PREMISE!!!");
        			//fill the fields with values :)
        			
        			clearAll();
        			didSearch=true;
        			fillFieldsWithData(premise);
					this.hcn = premise.house_connection_number;
        			toggleForm(true);
        			txtSearchHcn.setText("");
        			txtSearchWaterMeterNumber.setText("");
        			loadedPremise=premise;
        			

        	    	new AlertDialog.Builder(this)
        		    .setTitle(getResources().getString(R.string.premise_found))
        		    .setMessage(getResources().getString(R.string.premise_found))
        		    .setPositiveButton(getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
        		        public void onClick(DialogInterface dialog, int which) { 
        		            dialog.dismiss();
        		        }
        		     })
        		     .show();
        	    	
        		} catch (Exception e) {
					Log.w("GRE��KA","NE MOGU PARSIRATI NA��ENI PREMISE!!!"+response_str,e);
				}
        	}
        }
    	
    	searchTask=null;
    }
    
    
    private void fillFieldsWithData(JSONRejectedPremiseItem premise){
    	
    	if (premise.house_connection_number == null || premise.house_connection_number.equals(""))
			lnHcn.setVisibility(View.GONE);
		else {
			txtHcn.setText(premise.house_connection_number);
			lnHcn.setVisibility(View.VISIBLE);
		}

		if (premise.water_meter_number == null || premise.water_meter_number.equals(""))
			lnWmn.setVisibility(View.GONE);
		else {
			txtWmn.setText(premise.water_meter_number);
			lnWmn.setVisibility(View.VISIBLE);
		}

		if (premise.full_name == null || premise.full_name.equals(""))
			lnFullName.setVisibility(View.GONE);
		else {
			txtFullName.setText(premise.full_name);
			lnFullName.setVisibility(View.VISIBLE);
		}

		if (premise.district == null || premise.district.equals(""))
			lnDistrict.setVisibility(View.GONE);
		else {
			txtDistrict.setText(premise.district);
			lnDistrict.setVisibility(View.VISIBLE);
		}
		
    	if(!premise.gps_latitude.equalsIgnoreCase("0.000000")){
    		toggleMap(false);
    		txtPremiseLat.setText(premise.gps_latitude);
    		txtPremiseLng.setText(premise.gps_longitude);
    		
    		myLat=premise.gps_latitude;
    		myLng=premise.gps_longitude;
    		myAlt=premise.gps_altitude;
    	}else{
    		txtPremiseLat.setText(premise.gps_latitude);
    		txtPremiseLng.setText(premise.gps_longitude);
    		myLat=premise.gps_latitude;
    		myLng=premise.gps_longitude;
    		myAlt=premise.gps_altitude;
    		toggleMap(true);
    	}
    	

    }
    
    @Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString("mCurrentPhotoPath", mCurrentPhotoPath);
		if(pathImgHouse1!=null){
			outState.putString("pathImgHouse1", pathImgHouse1);
			Log.w("SAVING HOUSE1","JPG");
		}
		if(pathImgHouse2!=null){
			outState.putString("pathImgHouse2", pathImgHouse2);
			Log.w("SAVING HOUSE2","JPG");
		}
		if(pathImgHouse3!=null){
			outState.putString("pathImgHouse3", pathImgHouse3);
			Log.w("SAVING HOUSE3","JPG");
		}
		if(pathImgConnection!=null){
			outState.putString("pathImgConnection", pathImgConnection);
			Log.w("SAVING CONNIMG","JPG");
		}
		if(pathImgSTC!=null){
			outState.putString("pathImgSTC", pathImgSTC);
			Log.w("SAVING pathImgSTC","JPG");
		}
		if(pathImgSEC!=null){
			outState.putString("pathImgSEC", pathImgSEC);
			Log.w("SAVING pathImgSEC","JPG");
		}

		if(myLat!=null && myLng!=null && myAlt!=null && premiseAlt!=null && premiseLat!=null && premiseLng!=null){
			outState.putString("myLat", myLat);
			outState.putString("myLng", myLng);
			outState.putString("myAlt", myAlt);
			outState.putString("premiseAlt", premiseAlt);
			outState.putString("premiseLat", premiseLat);
			outState.putString("premiseLng", premiseLng);
			
		}
		if(hcn!=null){
			outState.putString("hcn", hcn);
			Log.w("SAVING hcn",hcn);
		}

		
		outState.putBoolean("canDeleteImgHouse1", canDeleteImgHouse1);
		outState.putBoolean("canDeleteImgHouse2", canDeleteImgHouse2);
		outState.putBoolean("canDeleteImgHouse3", canDeleteImgHouse3);
		outState.putBoolean("canDeleteImgConnection", canDeleteImgConnection);
		outState.putBoolean("canDeleteImgSTC", canDeleteImgSTC);
		outState.putBoolean("canDeleteImgSEC", canDeleteImgSEC);
		
		
		outState.putBoolean("gettingLocation", gettingLocation);
		
		outState.putString("txtSearchHcn", txtSearchHcn.getText().toString());
		
		outState.putString("txtSearchWaterMeterNumber", txtSearchWaterMeterNumber.getText().toString());
		outState.putBoolean("didSearch", didSearch);
		outState.putParcelable("loadedPremise", loadedPremise);
		
		super.onSaveInstanceState(outState);
		Log.w("SAVING INSTANCE","SAVING!!!");
	}

	



	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		
		if(savedInstanceState!=null){
			Log.w("RESTORING STATE","RESTORING!!!");
			pathImgHouse1=savedInstanceState.getString("pathImgHouse1");
			pathImgHouse2=savedInstanceState.getString("pathImgHouse2");
			pathImgHouse3=savedInstanceState.getString("pathImgHouse3");
			pathImgConnection=savedInstanceState.getString("pathImgConnection");
			pathImgSTC=savedInstanceState.getString("pathImgSTC");
			pathImgSEC=savedInstanceState.getString("pathImgSEC");
			
			myLat=savedInstanceState.getString("myLat");
			myLng=savedInstanceState.getString("myLng");
			myAlt=savedInstanceState.getString("myAlt");
			premiseLat=savedInstanceState.getString("premiseLat");
			premiseLng=savedInstanceState.getString("premiseLng");
			premiseAlt=savedInstanceState.getString("premiseAlt");
			if(premiseLat!=null && premiseLng!=null && txtPremiseLat!=null && txtPremiseLng!=null){
				txtPremiseLat.setText(premiseLat);
				txtPremiseLng.setText(premiseLng);
			}
			
			hcn=savedInstanceState.getString("hcn");
			
			//invalidate imageviews to force images from camera to show if activity gets destroyed and instance state is saved!!!
			if(pathImgHouse1!=null) {
				setPic(imgHouse1, pathImgHouse1);
			}
			
			if(pathImgHouse2!=null) {
				setPic(imgHouse2, pathImgHouse2);
			}
			
			if(pathImgHouse3!=null) {
				setPic(imgHouse3, pathImgHouse3);
			}
			
			if(pathImgConnection!=null) {
				setPic(imgConnection, pathImgConnection);
			}
			
			if(pathImgSTC!=null) {
				setPic(imgSTC, pathImgSTC);
			}
			
			if(pathImgSEC!=null) {
				setPic(imgSEC, pathImgSEC);
			}
			
			
			canDeleteImgHouse1=savedInstanceState.getBoolean("canDeleteImgHouse1");
			canDeleteImgHouse2=savedInstanceState.getBoolean("canDeleteImgHouse2");
			canDeleteImgHouse3=savedInstanceState.getBoolean("canDeleteImgHouse3");
			canDeleteImgConnection=savedInstanceState.getBoolean("canDeleteImgConnection");
			canDeleteImgSTC=savedInstanceState.getBoolean("canDeleteImgSTC");
			canDeleteImgSEC=savedInstanceState.getBoolean("canDeleteImgSEC");
			
			gettingLocation=savedInstanceState.getBoolean("gettingLocation");
			
			mCurrentPhotoPath=savedInstanceState.getString("mCurrentPhotoPath");
			if(mCurrentPhotoPath!=null)
				f=new File(mCurrentPhotoPath);
			
			txtSearchWaterMeterNumber.setText(savedInstanceState.getString("txtSearchWaterMeterNumber",""));
			didSearch=savedInstanceState.getBoolean("didSearch",false);
			loadedPremise = savedInstanceState.getParcelable("loadedPremise");

			// get text views
			if (loadedPremise != null)
				fillFieldsWithData(loadedPremise);
		}
	}

	
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.w("RESUME","RESUMING!!!");
		if(gettingLocation == true) {
			Log.w("GETTING LOC CREATE","TRUE");
			getGPSLocation();
		}
		setGpsLocation();
		
		//enable disable form
		toggleForm(didSearch);
		
		checkHcn();
		checkWmn();
		
	}


	@Override
	protected void onRestart() {
		super.onRestart();
		Log.w("RESTARTING","RESTARTING!!!");
	}
	@Override
	protected void onStop() {
		super.onStop();
		Log.w("STOPING","STOPING AND SAVING USER!!!");
		GlobalFunctionsAndConstants.savePrefs(this);
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
	    if(task!=null){
	    	task.detach();
	    	return(task);
	    }
	    if(searchTask!=null){
	    	searchTask.detach();
	    	return(searchTask);
	    }
	    return null;
	}
	
	/**
	 * postavi context menu kad se klikne na sliku
	 * ako je slika vec postavljena onda ce imat opciju brisi, a ako nije onda samo capture i choose
	 */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {
    	super.onCreateContextMenu(menu, v, menuInfo);
    	
    	//context menu can be opened only if user has searched (enable /disable)
    	if(didSearch){
    	
	    	switch(v.getId()){
	    	case R.id.imgHouse1:
	    		createImageContextMenu(menu,R.id.takeHouse1Photo,R.id.selectHouse1Photo,R.id.deleteHouse1Photo,"house1");
	    		break;
	    	case R.id.imgHouse2:
	    		createImageContextMenu(menu,R.id.takeHouse2Photo,R.id.selectHouse2Photo,R.id.deleteHouse2Photo,"house2");
	    		break;
	    	case R.id.imgHouse3:
	    		createImageContextMenu(menu,R.id.takeHouse3Photo,R.id.selectHouse3Photo,R.id.deleteHouse3Photo,"house3");
	    		break;
	    	case R.id.imgConnection:
	    		createImageContextMenu(menu,R.id.takeConnectionPhoto,R.id.selectConnectionPhoto,R.id.deleteConnectionPhoto,"connection");
	    		break;
	    	case R.id.imgSTC:
	    		createImageContextMenu(menu,R.id.takeSTCPhoto,R.id.selectSTCPhoto,R.id.deleteSTCPhoto,"STC");
	    		break;
	    	case R.id.imgSEC:
	    		createImageContextMenu(menu,R.id.takeSECPhoto,R.id.selectSECPhoto,R.id.deleteSECPhoto,"SEC");
	    		break;
	    	}
    	}
    	
	}
    
    private void createImageContextMenu(ContextMenu menu,int idTake,int idSelect, int idDelete,String img){
    	String path=null;
    	if(img.equals("house1")) path=pathImgHouse1;
    	if(img.equals("house2")) path=pathImgHouse2;
    	if(img.equals("house3")) path=pathImgHouse3;
    	if(img.equals("connection")) path=pathImgConnection;
    	if(img.equals("STC")) path=pathImgSTC;
    	if(img.equals("SEC")) path=pathImgSEC;
    	
    	
    	menu.add(0, idTake, 0, "Take photo with camera");
		menu.add(0, idSelect, 0, "Select photo from gallery");
		if(path==null)
			menu.setHeaderTitle("Select photo");
		else {
			menu.setHeaderTitle("Edit photo");
			menu.add(0, idDelete, 0, "Delete photo");
		}
    }
	

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		int id=item.getItemId();
		
		switch (id){
		case R.id.takeHouse1Photo:
			startImageCapture(REQUEST_TAKE_HOUSE1_PHOTO);
			break;
		case R.id.deleteHouse1Photo:
			deleteImageFile(canDeleteImgHouse1, pathImgHouse1, imgHouse1);
			pathImgHouse1=null;
			break;
		case R.id.selectHouse1Photo:
			selectImageFromGal(REQUEST_SELECT_HOUSE1_PHOTO);
			break;
		case R.id.takeHouse2Photo:
			startImageCapture(REQUEST_TAKE_HOUSE2_PHOTO);
			break;
		case R.id.deleteHouse2Photo:
			deleteImageFile(canDeleteImgHouse2, pathImgHouse2, imgHouse2);
			pathImgHouse2=null;
			break;
		case R.id.selectHouse2Photo:
			selectImageFromGal(REQUEST_SELECT_HOUSE2_PHOTO);
			break;
		case R.id.takeHouse3Photo:
			startImageCapture(REQUEST_TAKE_HOUSE3_PHOTO);
			break;
		case R.id.deleteHouse3Photo:
			deleteImageFile(canDeleteImgHouse3, pathImgHouse3, imgHouse3);
			pathImgHouse3=null;
			break;
		case R.id.selectHouse3Photo:
			selectImageFromGal(REQUEST_SELECT_HOUSE3_PHOTO);
			break;
		case R.id.takeConnectionPhoto:
			startImageCapture(REQUEST_TAKE_CONNECTION_PHOTO);
			break;
		case R.id.deleteConnectionPhoto:
			deleteImageFile(canDeleteImgConnection, pathImgConnection, imgConnection);
			pathImgConnection=null;
			break;
		case R.id.selectConnectionPhoto:
			selectImageFromGal(REQUEST_SELECT_CONNECTION_PHOTO);
			break;
		case R.id.takeSTCPhoto:
			startImageCapture(REQUEST_TAKE_STC_PHOTO);
			break;
		case R.id.deleteSTCPhoto:
			deleteImageFile(canDeleteImgSTC, pathImgSTC, imgSTC);
			pathImgSTC=null;
			break;
		case R.id.selectSTCPhoto:
			selectImageFromGal(REQUEST_SELECT_STC_PHOTO);
			break;
		case R.id.takeSECPhoto:
			startImageCapture(REQUEST_TAKE_SEC_PHOTO);
			break;
		case R.id.deleteSECPhoto:
			deleteImageFile(canDeleteImgSEC, pathImgSEC, imgSEC);
			pathImgSEC=null;
			break;
		case R.id.selectSECPhoto:
			selectImageFromGal(REQUEST_SELECT_SEC_PHOTO);
			break;
		}
		
		return super.onContextItemSelected(item);
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case REQUEST_MAP_ACTIVITY:
				Bundle b=data.getExtras();
				myLat=b.getString("myLat", null);
				myLng=b.getString("myLng", null);
				myAlt=b.getString("myAlt", null);
				premiseLat=b.getString("premiseLat", null);
				premiseLng=b.getString("premiseLng", null);
				premiseAlt=b.getString("premiseAlt", null);
				if(premiseLat!=null && premiseLng!=null && txtPremiseLat!=null && txtPremiseLng!=null){
					txtPremiseLat.setText(premiseLat);
					txtPremiseLng.setText(premiseLng);
				}
				break;
			case REQUEST_TAKE_HOUSE1_PHOTO:
				if(imgHouse1!=null){
					setPic(imgHouse1,mCurrentPhotoPath);
					canDeleteImgHouse1=true;
					galleryAddPic();
				}
				pathImgHouse1=mCurrentPhotoPath;
				clearImageFileHandlers();
			break;
			case REQUEST_TAKE_HOUSE2_PHOTO:
				if(imgHouse2!=null){
					setPic(imgHouse2,mCurrentPhotoPath);
					canDeleteImgHouse2=true;
					galleryAddPic();
				}
				pathImgHouse2=mCurrentPhotoPath;
				clearImageFileHandlers();
			break;
			case REQUEST_TAKE_HOUSE3_PHOTO:
				if(imgHouse3!=null){
					setPic(imgHouse3,mCurrentPhotoPath);
					canDeleteImgHouse3=true;
					galleryAddPic();
				}
				pathImgHouse3=mCurrentPhotoPath;
				clearImageFileHandlers();
			break;
			case REQUEST_TAKE_CONNECTION_PHOTO:
				if(imgConnection!=null){
					setPic(imgConnection,mCurrentPhotoPath);
					canDeleteImgConnection=true;
					galleryAddPic();
				}
				pathImgConnection=mCurrentPhotoPath;
				clearImageFileHandlers();
			break;
			case REQUEST_TAKE_STC_PHOTO:
				if(imgSTC!=null){
					setPic(imgSTC,mCurrentPhotoPath);
					canDeleteImgSTC=true;
					galleryAddPic();
				}
				pathImgSTC=mCurrentPhotoPath;
				clearImageFileHandlers();
			break;
			case REQUEST_TAKE_SEC_PHOTO:
				if(imgSEC!=null){
					setPic(imgSEC,mCurrentPhotoPath);
					canDeleteImgSEC=true;
					galleryAddPic();
				}
				pathImgSEC=mCurrentPhotoPath;
				clearImageFileHandlers();
			break;
					
			case REQUEST_SELECT_HOUSE1_PHOTO:
				setImageFromGalleryResult(data,"house1");
				canDeleteImgHouse1=false;
			break;
			case REQUEST_SELECT_HOUSE2_PHOTO:
				setImageFromGalleryResult(data,"house2");
				canDeleteImgHouse2=false;
			break;
			case REQUEST_SELECT_HOUSE3_PHOTO:
				setImageFromGalleryResult(data,"house3");
				canDeleteImgHouse3=false;
			break;
			case REQUEST_SELECT_CONNECTION_PHOTO:
				setImageFromGalleryResult(data,"connection");
				canDeleteImgConnection=false;
			break;
			case REQUEST_SELECT_STC_PHOTO:
				setImageFromGalleryResult(data,"STC");
				canDeleteImgSTC=false;
			break;
			case REQUEST_SELECT_SEC_PHOTO:
				setImageFromGalleryResult(data,"SEC");
				canDeleteImgSEC=false;
			break;
			}
		}
    }

	private void setImageFromGalleryResult(Intent data,String imageView){
		Uri selectedImage = data.getData();
        String[] filePathColumn = {MediaStore.Images.Media.DATA};

        Cursor cursor = getContentResolver().query(
                           selectedImage, filePathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        if(imageView.equals("house1")){
        	pathImgHouse1=cursor.getString(columnIndex);
        	setPic(imgHouse1, pathImgHouse1);
        } else if(imageView.equals("house2")){
        	pathImgHouse2 = cursor.getString(columnIndex);
        	setPic(imgHouse2, pathImgHouse2);
        }  else if(imageView.equals("house3")){
        	pathImgHouse3 = cursor.getString(columnIndex);
        	setPic(imgHouse3, pathImgHouse3);
        }  else if(imageView.equals("connection")){
        	pathImgConnection = cursor.getString(columnIndex);
        	setPic(imgConnection, pathImgConnection);
        }  else if(imageView.equals("STC")){
        	pathImgSTC = cursor.getString(columnIndex);
        	setPic(imgSTC, pathImgSTC);
        }  else if(imageView.equals("SEC")){
        	pathImgSEC = cursor.getString(columnIndex);
        	setPic(imgSEC, pathImgSEC);
        } 
        
        cursor.close();
	}
	
	
	private void startImageCapture(int requestCode){
		try {
			f = setUpPhotoFile();
			mCurrentPhotoPath = f.getAbsolutePath();
			Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
			startActivityForResult(i, requestCode);
		} catch (IOException e) {
		}
	}
	
	private void selectImageFromGal(int requestCode){
		Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(i, requestCode); 
	}
	private void deleteImageFile(Boolean canDelete,String path,ImageView img){
		if(canDelete){
			File f1=new File(path);
			if(f1.delete()){
				sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
			}
		}
		img.setImageDrawable(getResources().getDrawable(R.drawable.capture));
	}
    
    
	private OnClickListener photoListener=new OnClickListener() {
		
		public void onClick(View v) {
			v.showContextMenu();
			
		}
	};
	
	private void clearImageFileHandlers(){
		mCurrentPhotoPath=null;
		f=null;
	}
	
	
	private File setUpPhotoFile() throws IOException {

		File f = createImageFile();
		mCurrentPhotoPath = f.getAbsolutePath();

		return f;
	}	

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp =  new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "NWC_premise_" + timeStamp + "";
        File image = File.createTempFile(
            imageFileName.substring(0,27), 
            ".jpg",
            getAlbumDir()
        );
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
    
    private File getAlbumDir() {
		File storageDir = null;

		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			
			storageDir = mAlbumStorageDirFactory.getAlbumStorageDir("NWC");

			if (storageDir != null) {
				if (! storageDir.mkdirs()) {
					if (! storageDir.exists()){
						Log.d("CameraSample", "failed to create directory");
						return null;
					}
				}
			}
			
		} else {
			Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
		}
		
		return storageDir;
	}
    
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Log.w("Adding gallery","path:"+mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }	

    /**
     * Compress picture and set it to imageview
     */
    private void setPic(ImageView v,String path) {
        // Get the dimensions of the View
        int targetW = 100;
        int targetH = 100;
        
        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;
        
        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);
      
        // Decode the image file into a Bitmap sized to fill the View
       	bmOptions.inSampleSize = scaleFactor;
       	bmOptions.inPurgeable = true;
	    bmOptions.inJustDecodeBounds = false;
      
        Bitmap bitmap = BitmapFactory.decodeFile(path, bmOptions);
        v.setImageBitmap(bitmap);
        
        //decode original file and save it compressed
        int dimW=1600;
        int dimH=1200;
        
        //set width to be always the larger size of image
        if(photoH > photoW) {
        	dimW=1200;
        	dimH=1600;
        }
        
        //if larger size > 1600 we will compress
        if((photoW>1600 && photoW>photoH) || (photoH>1600 && photoH>photoW)) {
        	try {
                bmOptions.inDither = false;
                bmOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
                bmOptions.inScaled=false;
                bmOptions.inSampleSize = photoW/dimW;
                bitmap = BitmapFactory.decodeFile(path, bmOptions);
                
        		// Resize
        		Matrix matrix = new Matrix();
        		matrix.postScale((float)dimW/photoW, (float)dimW/photoW);
        		Bitmap compressedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        		bitmap = null;
        		//Bitmap compressedBitmap=Bitmap.createScaledBitmap(bitmap, dimW, dimH, false);
				
				FileOutputStream os=new FileOutputStream(path);
				compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, os);
				compressedBitmap=null;
				Log.w("COMPRESS","KOMPRESUJEM");
			} catch (Exception e) {
				Log.w("CANT COMPRESS","ERROR",e);
			}
        	
        }
    }
    
    private void clearAll(){
    	mCurrentPhotoPath=null;
    	imgHouse1.setImageDrawable(getResources().getDrawable(R.drawable.capture));
    	imgHouse2.setImageDrawable(getResources().getDrawable(R.drawable.capture));
    	imgHouse3.setImageDrawable(getResources().getDrawable(R.drawable.capture));
    	imgConnection.setImageDrawable(getResources().getDrawable(R.drawable.capture));
    	imgSTC.setImageDrawable(getResources().getDrawable(R.drawable.capture));
    	imgSEC.setImageDrawable(getResources().getDrawable(R.drawable.capture));
    	
    	pathImgHouse1=null;
    	pathImgHouse2=null;
    	pathImgHouse3=null;
    	pathImgConnection=null;
    	pathImgSTC=null;
    	pathImgSEC=null;
    	
    	canDeleteImgHouse1=false;
    	canDeleteImgHouse2=false;
    	canDeleteImgHouse3=false;
    	canDeleteImgConnection=false;
    	canDeleteImgSTC=false;
    	canDeleteImgSEC=false;
    	
    	//my location
    	//my location
    	myLat=null;
    	myLng=null;
    	myAlt=null;
    	premiseLat=null;
    	premiseLng=null;
    	premiseAlt=null;
    	txtPremiseLat.setText("");
    	txtPremiseLng.setText("");
    	
    	hcn=null;
    	
    	txtSearchHcn.setText("");
    	txtWmn.setText("");
    	txtHcn.setText("");
    	txtFullName.setText("");
    	txtDistrict.setText("");
    	lnWmn.setVisibility(View.GONE);
    	lnHcn.setVisibility(View.GONE);
    	lnFullName.setVisibility(View.GONE);
    	lnDistrict.setVisibility(View.GONE);
    	
    	txtSearchWaterMeterNumber.setText("");
    	didSearch=false;
		loadedPremise = null;
    	toggleForm(false);
    }
    
    
  //FUNCTION FOR ACQUIRING GPS LOCATION
  	private void getGPSLocation(){
  		MyLocation.LocationResult locationResult = new MyLocation.LocationResult(){
  			@Override
  			public void gotLocation(android.location.Location location,int provider) {
  				GlobalFunctionsAndConstants.endLoading();
  				gettingLocation=false;
  				Log.w("LOCATION LISTENER","GOT LOCATION RESPONSE!!!!");
  				View windowRoot=getWindow().getDecorView().findViewById(android.R.id.content);
  				if(location==null){
  					
  					windowRoot.post(new Runnable() {
  						public void run() {
  							Toast.makeText(getApplicationContext(), getResources().getString(R.string.location_service_unavailable), Toast.LENGTH_LONG).show();
  						}
  					});
  					return;
  				}
  				//GPS toast location
  				final android.location.Location loc=location;
  				windowRoot.post(new Runnable() {
  					public void run() {
  						Toast.makeText(getApplicationContext(), getResources().getString(R.string.location_acquired)+loc.getProvider(), Toast.LENGTH_SHORT).show();
  						//String[] args={String.valueOf(loc.getLatitude()),String.valueOf(loc.getLongitude()),String.valueOf(loc.getAltitude())};
  						
  						myLat=String.valueOf(loc.getLatitude());
  						myLng=String.valueOf(loc.getLongitude());
  						myAlt=String.valueOf(loc.getAltitude());
  						ActivityPremiseImage.this.setGpsLocation();
  					}
  				});
  				
  			}
  		};
  		myLocation=new MyLocation();
  		gettingLocation=true;
  		if(myLocation.getLocation(this, locationResult)==false){
  			Toast.makeText(this, getResources().getString(R.string.location_service_unavailable), Toast.LENGTH_LONG).show();
  			gettingLocation=false;
  		} else {
  			GlobalFunctionsAndConstants.startLoading(this, null, this.getResources().getString(R.string.getting_gps));
  		}
  		
  		
  	}	
  	
  	private void setGpsLocation(){
		if(premiseLat!=null && premiseLng!=null && txtPremiseLat!=null && txtPremiseLng!=null){
			txtPremiseLat.setText(premiseLat);
			txtPremiseLng.setText(premiseLng);
		}
  	}
  	
  	/**
  	 * Enable or disable input form
  	 * @param Boolean enable
  	 */
  	private void toggleForm(Boolean enable){
  		btnSubmit.setEnabled(enable);
  		btnGetLocation.setEnabled(enable);
  		btnGetLocationNoMap.setEnabled(enable);
  	}
  	
  	private void checkHcn(){
  		LinearLayout llDistrict=(LinearLayout)findViewById(R.id.llDistrict);
  	}

  	private void checkWmn(){
  		LinearLayout llWmStatus=(LinearLayout)findViewById(R.id.llWmStatus);
  	}

	@Override
	protected void onPause() {
		super.onPause();
		if(myLocation!=null)
			myLocation.cancelUpdates();
		
		GlobalFunctionsAndConstants.endLoading();
	}
	
	//start preise search task
	private void searchPremise(){
		dialog = new ProgressDialog(this);
        dialog.setMessage(getResources().getString(R.string.sending));
        dialog.setIndeterminate(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setProgress(0);
        dialog.show();
		searchTask=new SearchPremise(this);
		searchTask.execute();
	}
	
}
