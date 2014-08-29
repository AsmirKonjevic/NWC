package ba.leftor.nwc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;

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
import android.graphics.Bitmap.CompressFormat;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import ba.leftor.nwc.ActivityPremise.SearchPremise;
import ba.leftor.nwc.CustomMultiPartEntity.ProgressListener;

public class ActivityCustomer extends Activity{
	
	//define variables for rotation aware task (so upload can resume if orientation chnages during upload)
	private SubmitCustomerForm task=null;
    private ProgressDialog dialog;
	
	//define variables needed for taking photos and selecting photo from gallery
	static final int REQUEST_TAKE_OWNER_PHOTO=1;
	static final int REQUEST_TAKE_ALTERNATE_CONTACT_PHOTO=2;
	static final int REQUEST_TAKE_INSTRUMENT_PHOTO=5;
	static final int REQUEST_SELECT_OWNER_GALLERY_PHOTO=98;
	static final int REQUEST_SELECT_ALTERNATE_CONTACT_GALLERY_PHOTO=99;
	static final int REQUEST_SELECT_INSTRUMENT_PHOTO=96;
	private String mCurrentPhotoPath;
	private AlbumStorageDirFactory mAlbumStorageDirFactory = null;
	File f=null;
	
	//define images first and private variables that will be used to store their paths after they have been taken
	private ImageView imgOwner;
	private ImageView imgAlternateContact;
	private ImageView imgInstrument;
	private String pathImgOwner;
	private String pathImgAlternateContact;
	private String pathImgInstrument;
	private Boolean canDeleteImgOwner=false;
	private Boolean canDeleteImgAlternateContact=false;
	private Boolean canDeleteImgInstrument=false;
	
	//define spinners
	private Spinner spnMonthExpirationDate;
	private Spinner spnGender;
	private Spinner spnNationality;
	private Spinner spnContactMonthExpirationDate;
	private Spinner spnInhabitantType;
	private Spinner spnTarsheedType;
	
	//define text fields
	private EditText txtHouseConnectionNumber;
	private EditText txtFullName;
	private EditText txtNationalId;
	private EditText txtDayExpirationDate;
	private EditText txtYearExpirationDate;
	private EditText txtEmail;
	private EditText txtPhoneLandLine;
	private EditText txtMobilePhone;
	private EditText txtPoBox;
	private EditText txtZipCode;
	private EditText txtLandId;
	private EditText txtAlternateContactName;
	private EditText txtContactNationalId;
	private EditText txtContactDayExpirationDate;
	private EditText txtContactYearExpirationDate;
	private EditText txtContactPhoneLandLine;
	private EditText txtContactMobilePhone;
	private EditText txtContactEmail;
	private EditText txtContactStreetName;
	private EditText txtContactZipCode;
	private EditText txtContactPoBox;
	
	//define checkbox
	private CheckBox chkTarsheedGiven;
	
	//search by hcn top form
	private Boolean didSearch=false; //USER MUST FIRST SEARCH BEFORE GETTING PERMISSION TO FILL FORM
	private SearchCustomer searchTask=null;
	private EditText txtSearchWaterMeterNumber;
	private EditText txtSearchHcn;
	private EditText txtSearchSEC;
	private Button btnSearch;
	private Button btnSearchWithLocation;
	private Button btnEnable;
	private Button btnSubmit;
	
	//define array lists
	ArrayList<JSONSpinnerItem> lstNationalities;
	ArrayList<JSONSpinnerItem> lstInhabitantTypes;
	ArrayList<JSONSpinnerItem> lstTarsheedTypes;
	private ArrayAdapter adptGender;
	private ArrayAdapter adptNationality;
	private ArrayAdapter adptInhabitantType;
	private ArrayAdapter adptTarsheedType;
	private ArrayAdapter adptContactMonthExpirationDate;
	private ArrayAdapter adptMonthExpirationDate;
	
	//variables for rejected premises
	private Boolean hasLoadedRejected=false;
	private String rejectedHcn=null;
	
	
	//my location
	private MyLocation myLocation;
	private MyLocation mySearchLocation;
	private Boolean gettingLocation=false; //to continue finding location on orientation change
	private Boolean gettingSearchLocation=false;
	
	//vars for new premise location!! (map pin location correction for previous bad inputs)
    static final int REQUEST_MAP_ACTIVITY=55;
    private Button btnGetLocation;
	private String newPremiseLocLat = null;
	private String newPremiseLocLng = null;
	private String newPremiseLocAlt = null;
	private Boolean locationCorrected = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.w("CREATING","CREATINGG!!!");
		setContentView(R.layout.customer_form);


		
		// display user name on top
		TextView txtWelcome = (TextView) findViewById(R.id.txtWelcome);
		JSONUSerItem user = GlobalFunctionsAndConstants.getUser(this);
		if (user.fullName.length() > 1)
			txtWelcome.setText(ActivityCustomer.this.getResources().getString(R.string.welcome) + user.fullName);
		else
			txtWelcome.setText(ActivityCustomer.this.getResources().getString(R.string.welcome) + user.username);
		
		//get album storage dir
		mAlbumStorageDirFactory = new BaseAlbumDirFactory();
		
		
		
		//find images in layout and register for context menu
		imgOwner=(ImageView)this.findViewById(R.id.imgOwner);
		imgAlternateContact=(ImageView)findViewById(R.id.imgAlternateContact);
		imgInstrument=(ImageView)this.findViewById(R.id.imgInstrument);	
		registerForContextMenu(imgOwner);
		registerForContextMenu(imgAlternateContact);
		registerForContextMenu(imgInstrument);
		imgOwner.setOnClickListener(photoListener);
		imgAlternateContact.setOnClickListener(photoListener);
		imgInstrument.setOnClickListener(photoListener);
		
		//find search form elements
		txtSearchWaterMeterNumber=(EditText)findViewById(R.id.txtSearchWaterMeterNumber);
		txtSearchHcn=(EditText)findViewById(R.id.txtSearchHcn);
		txtSearchSEC=(EditText)findViewById(R.id.txtSearchSEC);
		btnSearch=(Button)findViewById(R.id.btnSearch);
		btnSearchWithLocation=(Button)findViewById(R.id.btnSearchWithLocation);
		btnEnable=(Button)findViewById(R.id.btnEnable);
		
		//find edit text, spinners and checkboxes in layout
		txtHouseConnectionNumber=(EditText)findViewById(R.id.txtHouseConnectionNumber);
		txtFullName=(EditText)findViewById(R.id.txtFullName);
		txtNationalId=(EditText)findViewById(R.id.txtNationalId);
		txtDayExpirationDate=(EditText)findViewById(R.id.txtDayExpirationDate);
		txtYearExpirationDate=(EditText)findViewById(R.id.txtYearExpirationDate);
		txtEmail=(EditText)findViewById(R.id.txtEmail);
		txtPhoneLandLine=(EditText)findViewById(R.id.txtPhoneLandLine);
		txtMobilePhone=(EditText)findViewById(R.id.txtMobilePhone);
		txtPoBox=(EditText)findViewById(R.id.txtPoBox);
		txtZipCode=(EditText)findViewById(R.id.txtZipCode);
		txtLandId=(EditText)findViewById(R.id.txtLandId);
		txtAlternateContactName=(EditText)findViewById(R.id.txtAlternateContactName);
		txtContactNationalId=(EditText)findViewById(R.id.txtContactNationalId);
		txtContactDayExpirationDate=(EditText)findViewById(R.id.txtContactDayExpirationDate);
		txtContactYearExpirationDate=(EditText)findViewById(R.id.txtContactYearExpirationDate);
		txtContactPhoneLandLine=(EditText)findViewById(R.id.txtContactPhoneLandLine);
		txtContactMobilePhone=(EditText)findViewById(R.id.txtContactMobilePhone);
		txtContactEmail=(EditText)findViewById(R.id.txtContactEmail);
		txtContactStreetName=(EditText)findViewById(R.id.txtContactStreetName);
		txtContactZipCode=(EditText)findViewById(R.id.txtContactZipCode);
		txtContactPoBox=(EditText)findViewById(R.id.txtContactPoBox);
		
		spnMonthExpirationDate=(Spinner)findViewById(R.id.spnMonthExpirationDate);
		spnGender=(Spinner)findViewById(R.id.spnGender);
		spnNationality=(Spinner)findViewById(R.id.spnNationality);
		spnContactMonthExpirationDate=(Spinner)findViewById(R.id.spnContactMonthExpirationDate);
		spnInhabitantType=(Spinner)findViewById(R.id.spnInhabitantType);
		spnTarsheedType=(Spinner)findViewById(R.id.spnTarsheedType);
		
		chkTarsheedGiven=(CheckBox)findViewById(R.id.chkTarsheedGiven);
		
		
		//create adapters
		loadCatalogListsFromDb();
		
		//months adapter
		Integer[] array_monthExpirationDate={1,2,3,4,5,6,7,8,9,10,11,12};
		adptMonthExpirationDate = new ArrayAdapter(this,android.R.layout.simple_spinner_item, array_monthExpirationDate);
		adptMonthExpirationDate.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnMonthExpirationDate.setAdapter(adptMonthExpirationDate);
		
		//months adapter
		Integer[] array_contactMonthExpirationDate={1,2,3,4,5,6,7,8,9,10,11,12};
		adptContactMonthExpirationDate = new ArrayAdapter(this,android.R.layout.simple_spinner_item, array_contactMonthExpirationDate);
		adptContactMonthExpirationDate.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnContactMonthExpirationDate.setAdapter(adptContactMonthExpirationDate);
		
		//gender adapter
		String [] array_gender={"Male / ذكر","Female / أنثى"};
		adptGender = new ArrayAdapter(this,android.R.layout.simple_spinner_item, array_gender);
		adptGender.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnGender.setAdapter(adptGender);	
		
		//nationality adapter
		adptNationality = new ArrayAdapter(this,android.R.layout.simple_spinner_item, lstNationalities);
		adptNationality.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnNationality.setAdapter(adptNationality);	
		
		//nationality adapter
		adptInhabitantType = new ArrayAdapter(this,android.R.layout.simple_spinner_item, lstInhabitantTypes);
		adptInhabitantType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnInhabitantType.setAdapter(adptInhabitantType);	
		
		//nationality adapter
		adptTarsheedType = new ArrayAdapter(this,android.R.layout.simple_spinner_item, lstTarsheedTypes);
		adptTarsheedType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnTarsheedType.setAdapter(adptTarsheedType);	
		
		btnSubmit=(Button)findViewById(R.id.btnSumbit);
		btnSubmit.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				validateAndSubmit();
				
			}
		});
		
		//search form button
		btnSearch.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				String hcn=txtSearchHcn.getText().toString().trim();
				String wmn=txtSearchWaterMeterNumber.getText().toString().trim();
				String sec=txtSearchSEC.getText().toString().trim();
				
				
				Boolean error=false;
				String errText="";
				if(hcn.equals("") && wmn.equals("") && sec.equals("")){
					error=true;
					errText=getResources().getString(R.string.error_enter_search);
					txtSearchWaterMeterNumber.setError("You must first search for house connection number or water meter number!");
				} else {
					txtSearchHcn.setError(null);
					txtSearchWaterMeterNumber.setError(null);
				}
				
				if(!error && sec.length()>0 && !GlobalFunctionsAndConstants.isSECValid(sec)){
					error=true;
					errText=getResources().getString(R.string.error_sec_wrong_format);
					
				}
				
				if(error){
					new AlertDialog.Builder(ActivityCustomer.this)
				    .setTitle("ERROR")
				    .setMessage(errText)
				    .setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int which) { 
				            dialog.dismiss();
				        }
				     })
				     .show();
				} else {
					searchCustomer(null);
				}
				
				
			}
		});
		
		btnSearchWithLocation.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				String wmn=txtSearchWaterMeterNumber.getText().toString().trim();
				
				
				Boolean error=false;
				String errText="";
				if(wmn.equals("")){
					error=true;
					errText=getResources().getString(R.string.error_gps_search_not_availbale);
					txtSearchWaterMeterNumber.setError(errText);
				} else {
					txtSearchWaterMeterNumber.setError(null);
				}
				
				
				if(error){
					new AlertDialog.Builder(ActivityCustomer.this)
				    .setTitle("ERROR")
				    .setMessage(errText)
				    .setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int which) { 
				            dialog.dismiss();
				        }
				     })
				     .show();
				} else {
					getSearchGPSLocation();
				}
				
				
			}
		});
		
		// enable form button
		btnEnable.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				didSearch=true;
				toggleForm(didSearch);
				txtHouseConnectionNumber.requestFocus();
				
			}
		});
		
		//get location button
		btnGetLocation=(Button)findViewById(R.id.btnGetLocation);
		btnGetLocation.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				Intent i = new Intent(ActivityCustomer.this,ActivityMap.class);
				if(newPremiseLocAlt != null && newPremiseLocLat!= null && newPremiseLocLng != null){
					i.putExtra("premiseLat", newPremiseLocLat);
					i.putExtra("premiseLng", newPremiseLocLng);
					i.putExtra("premiseAlt", newPremiseLocAlt);
				}
				startActivityForResult(i, REQUEST_MAP_ACTIVITY);
				
			}
		});
		
		
		//rotation aware task
		try {
			task=(SubmitCustomerForm)getLastNonConfigurationInstance();
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
			searchTask=(SearchCustomer)getLastNonConfigurationInstance();
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
	
	
	//start preise search task
	private void searchCustomer(String[] args){
		dialog = new ProgressDialog(this);
        dialog.setMessage(getResources().getString(R.string.sending));
        dialog.setIndeterminate(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setProgress(0);
        dialog.show();
		searchTask=new SearchCustomer(this);
		searchTask.execute(args);
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
	
	private void loadCatalogListsFromDb(){
		MyDataSource dataSource=new MyDataSource(this);
		dataSource.open();
		lstNationalities=dataSource.getCatalogItems("nationality");
		lstInhabitantTypes=dataSource.getCatalogItems("inhabitant_type");
		lstTarsheedTypes=dataSource.getCatalogItems("tarsheed_type");
		dataSource.close();
		Log.w("CATALOG","NATIONALITIES:"+lstNationalities.size());
		Log.w("CATALOG","INHABITANT_T:"+lstNationalities.size());
		Log.w("CATALOG","TARSHEED_T:"+lstNationalities.size());
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
						String[] args={String.valueOf(loc.getLatitude()),String.valueOf(loc.getLongitude()),String.valueOf(loc.getAltitude())};
						ActivityCustomer.this.submitForm(args);
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
	//FUNCTION FOR ACQUIRING GPS LOCATION
	private void getSearchGPSLocation(){
		MyLocation.LocationResult locationResult = new MyLocation.LocationResult(){
			@Override
			public void gotLocation(android.location.Location location,int provider) {
				GlobalFunctionsAndConstants.endLoading();
				gettingSearchLocation=false;
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
						String[] args={String.valueOf(loc.getLatitude()),String.valueOf(loc.getLongitude()),String.valueOf(loc.getAltitude())};
						ActivityCustomer.this.searchCustomer(args);
					}
				});
				
			}
		};
		mySearchLocation=new MyLocation();
		gettingSearchLocation=true;
		if(mySearchLocation.getLocation(this, locationResult)==false){
			Toast.makeText(this, getResources().getString(R.string.location_service_unavailable), Toast.LENGTH_LONG).show();
			gettingSearchLocation=false;
		} else {
			GlobalFunctionsAndConstants.startLoading(this, null, this.getResources().getString(R.string.getting_gps));
		}
		
		
	}	
	
	//validate and submit form
	private void validateAndSubmit(){
		Boolean error=false;
		
		if(txtHouseConnectionNumber.getText().toString().trim().length()<1){
			txtHouseConnectionNumber.setError(getResources().getString(R.string.field_required));
			txtHouseConnectionNumber.requestFocus();
			error=true;
		}
		else txtHouseConnectionNumber.setError(null);
		
		if(txtFullName.getText().toString().trim().length()<1){
			txtFullName.setError(getResources().getString(R.string.field_required));
			if(!error) txtFullName.requestFocus();
			error=true;
		}
		else txtFullName.setError(null);
		
		if(txtNationalId.getText().toString().trim().length()<1){
			txtNationalId.setError(getResources().getString(R.string.field_required));
			if(!error) txtNationalId.requestFocus();
			error=true;
		}
		else txtNationalId.setError(null);
		
		
		if(txtMobilePhone.getText().toString().trim().length()<1){
			txtMobilePhone.setError(getResources().getString(R.string.field_required));
			if(!error) txtMobilePhone.requestFocus();
			error=true;
		}
		else txtMobilePhone.setError(null);
		
		if(txtEmail.getText().toString().trim().length()>0 && !GlobalFunctionsAndConstants.isEmailValid(txtEmail.getText().toString())){
			txtEmail.setError(getResources().getString(R.string.error_email_invalid));
			if(!error) txtEmail.requestFocus();
			error=true;
		}
		else txtEmail.setError(null);

		if(txtContactEmail.getText().toString().trim().length()>0 && !GlobalFunctionsAndConstants.isEmailValid(txtContactEmail.getText().toString())){
			txtContactEmail.setError(getResources().getString(R.string.error_email_invalid));
			if(!error) txtContactEmail.requestFocus();
			error=true;
		}
		else txtContactEmail.setError(null);

		if(txtDayExpirationDate.getText().toString().trim().length()>0){
			int day=0;
			try {
				day=Integer.valueOf(txtDayExpirationDate.getText().toString().trim());
			} catch (Exception e) {
				day=0;
			}
			if(day<1 || day>31){
				txtDayExpirationDate.setError(getResources().getString(R.string.error_date_day));
				if(!error) txtDayExpirationDate.requestFocus();
				error=true;
			}
			
		}
		else txtDayExpirationDate.setError(null);

		if(txtContactDayExpirationDate.getText().toString().trim().length()>0){
			int day=0;
			try {
				day=Integer.valueOf(txtContactDayExpirationDate.getText().toString().trim());
			} catch (Exception e) {
				day=0;
			}
			if(day<1 || day>31){
				txtContactDayExpirationDate.setError(getResources().getString(R.string.error_date_day));
				if(!error) txtContactDayExpirationDate.requestFocus();
				error=true;
			}
			
		}
		else txtContactDayExpirationDate.setError(null);

		if(txtYearExpirationDate.getText().toString().trim().length()>0){
			int year=0;
			try {
				year=Integer.valueOf(txtYearExpirationDate.getText().toString().trim());
			} catch (Exception e) {
				year=0;
			}
			if(year<1400){
				txtYearExpirationDate.setError(getResources().getString(R.string.error_date_year));
				if(!error) txtYearExpirationDate.requestFocus();
				error=true;
			}
			
		}
		else txtYearExpirationDate.setError(null);
		
		if(txtContactYearExpirationDate.getText().toString().trim().length()>0){
			int year=0;
			try {
				year=Integer.valueOf(txtContactYearExpirationDate.getText().toString().trim());
			} catch (Exception e) {
				year=0;
			}
			if(year<1300){
				txtContactYearExpirationDate.setError(getResources().getString(R.string.error_date_year));
				if(!error) txtContactYearExpirationDate.requestFocus();
				error=true;
			}
			
		}
		else txtContactYearExpirationDate.setError(null);
		
		
		
		if(!error && ( newPremiseLocLat == null || newPremiseLocLng==null || newPremiseLocAlt == null || locationCorrected==false) ){
			error=true;
			new AlertDialog.Builder(ActivityCustomer.this)
		    .setTitle(getResources().getString(R.string.error_location_not_set))
		    .setMessage(getResources().getString(R.string.error_correct_premise_location))
		    .setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) { 
		            dialog.dismiss();
		        }
		     })
		     .show();
		}
		
		if(!error)
			getGPSLocation();
	}
	
	private void submitForm(String[] args){
		dialog = new ProgressDialog(this);
        dialog.setMessage(getResources().getString(R.string.sending));
        dialog.setIndeterminate(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setProgress(0);
        dialog.show();
		task=new SubmitCustomerForm(this);
		task.execute(args);
	}
	
	
	
    static class SubmitCustomerForm extends AsyncTask<String, Integer, Void> {

    	private ActivityCustomer activity=null;
        private int percent = 0;
        private long totalSize;
        private int  currentProgress;
        private String response_str=null;

        SubmitCustomerForm(ActivityCustomer activity) {
            attach(activity);
        }
        void detach() {
        	activity=null;
        }
          
        void attach(ActivityCustomer activity) {
        	this.activity=activity;
        }

        @Override
        protected Void doInBackground(String... arg) {
        	
            try {
            	           	
                JSONCustomerItem item=new JSONCustomerItem();
                /** parse variables + gps **/
            	item.gps_latitude_created=arg[0];
            	item.gps_longitude_created=arg[1];
            	item.gps_altitude_created=arg[2];
            	
            	item.house_connection_number=activity.txtHouseConnectionNumber.getText().toString().trim();
            	item.full_name=activity.txtFullName.getText().toString().trim();
            	item.national_id=activity.txtNationalId.getText().toString().trim();
            	item.day_expiration_date=activity.txtDayExpirationDate.getText().toString().trim();
            	item.year_expiration_date=activity.txtYearExpirationDate.getText().toString().trim();
            	item.email=activity.txtEmail.getText().toString().trim();
            	item.phone_land_line=activity.txtPhoneLandLine.getText().toString().trim();
            	item.mobile_phone=activity.txtMobilePhone.getText().toString().trim();
            	item.po_box=activity.txtPoBox.getText().toString().trim();
            	item.zip_code=activity.txtZipCode.getText().toString().trim();
            	item.land_id=activity.txtLandId.getText().toString().trim();
            	item.alternate_contact_name=activity.txtAlternateContactName.getText().toString().trim();
            	item.contact_national_id=activity.txtContactNationalId.getText().toString().trim();
            	item.contact_phone_land_line=activity.txtContactPhoneLandLine.getText().toString().trim();
            	item.contact_mobile_phone=activity.txtContactMobilePhone.getText().toString().trim();
            	item.contact_day_expiration_date=activity.txtContactDayExpirationDate.getText().toString().trim();
            	item.contact_year_expiration_date=activity.txtContactYearExpirationDate.getText().toString().trim();
            	item.contact_email=activity.txtContactEmail.getText().toString().trim();
            	item.contact_street_name=activity.txtContactStreetName.getText().toString().trim();
            	item.contact_zip_code=activity.txtContactZipCode.getText().toString().trim();
            	item.contact_po_box=activity.txtContactPoBox.getText().toString().trim();
            	
            	item.month_expiration_date=String.valueOf(activity.spnMonthExpirationDate.getSelectedItemPosition()+1);
            	item.contact_month_expiration_date=String.valueOf(activity.spnContactMonthExpirationDate.getSelectedItemPosition()+1);
            	item.gender=String.valueOf(activity.spnGender.getSelectedItemPosition()+1);
            	item.nationality_id=String.valueOf(((JSONSpinnerItem)activity.lstNationalities.get(activity.spnNationality.getSelectedItemPosition())).id);
            	item.inhabitant_type_id=String.valueOf(((JSONSpinnerItem)activity.lstInhabitantTypes.get(activity.spnInhabitantType.getSelectedItemPosition())).id);
            	item.tarsheed_type_id=String.valueOf(((JSONSpinnerItem)activity.lstTarsheedTypes.get(activity.spnTarsheedType.getSelectedItemPosition())).id);
            	
            	String tmpTarsheed="0";
                if(activity.chkTarsheedGiven.isChecked()) tmpTarsheed="1"; else tmpTarsheed="0";
            	item.tarsheed_given=tmpTarsheed;
            	
            	item.photo_owner=activity.pathImgOwner;
            	item.photo_alternate=activity.pathImgAlternateContact;
        		item.photo_instrument=activity.pathImgInstrument;

        		item.newPremiseLat=activity.newPremiseLocLat;
        		item.newPremiseLng=activity.newPremiseLocLng;
        		item.newPremiseAlt=activity.newPremiseLocAlt;
        		
            	MyDataSource dataSource=new MyDataSource(this.activity);
            	dataSource.open();
            	
            	long insertStatus=dataSource.addCustomerItemToDb(item);
            	dataSource.close();
            	if(insertStatus!=-1)
            		response_str="OK";
            	else 
            		response_str="ERROR:ERROR WHILE SAVING ITEM TO DB.PLEASE TRY AGAIN. IF this error continues please CONTACT SUPPROT AND NOTIFY ABOUT THIS ERROR.";

                
                

            } catch (Exception e) {
                e.printStackTrace();
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
	            }
            }

        }

    }
    
    private void updateProgress(int percent){
    	dialog.setProgress(percent);
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
    	}else if(task.response_str!=null && task.response_str.contains("OK")){
    		clearAll();
    		didSearch=false;
	    	toggleForm(false);
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
		    
		    
		    if(hasLoadedRejected==true) {
		    	Intent intent = new Intent();
		    	Intent ci=getIntent();
		    	intent.putExtra("rejectedId", ci.getIntExtra("rejectedId", 0));
		    	setResult(RESULT_OK, intent);
		    	finish();
		    	return;
		    }
    	}
    }
	
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.w("RESUME","RESUMING!!!");
		//if user was in process of obtaining user location then submit immedatelly!
		if(gettingLocation == true) {
			Log.w("GETTING LOC CREATE","TRUE");
			validateAndSubmit();
		}
		if(gettingSearchLocation==true){
			getSearchGPSLocation();
		}
		
		toggleForm(didSearch);
		
		//provjeri rejected
		//provjeri da li je proslijeđen rejected premise id
		Bundle b=this.getIntent().getExtras();
		int id=0;
		if(b != null){
			id = b.getInt("rejectedId", 0);
			rejectedHcn=b.getString("rejectedHcn", null);
			Log.w("BUNDLE","REJECTED POSLAN");
		} else Log.w("BUNDLE","PRAZAN");
		if(id>0 && rejectedHcn != null && rejectedHcn.length()>0 && hasLoadedRejected==false && searchTask==null){
			//startaj search taks proslijeđen je HCN od rejected-a
			dialog = new ProgressDialog(this);
	        dialog.setMessage(getResources().getString(R.string.sending));
	        dialog.setIndeterminate(false);
	        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	        dialog.setProgress(0);
	        dialog.show();
			searchTask=new SearchCustomer(this);
			searchTask.execute();
			btnSearch.setEnabled(false);
			btnEnable.setEnabled(false);
		} else if(hasLoadedRejected == true) {
			txtFullName.requestFocus();
			btnSearch.setEnabled(false);
			btnEnable.setEnabled(false);
			txtHouseConnectionNumber.setEnabled(false);
		}
		
			
	}


	@Override
	protected void onPause() {
		super.onPause();
		GlobalFunctionsAndConstants.endLoading();
		
		if(myLocation!=null)
			myLocation.cancelUpdates();
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
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString("mCurrentPhotoPath", mCurrentPhotoPath);
		if(pathImgOwner!=null){
			outState.putString("pathImgOwner", pathImgOwner);
			Log.w("SAVING OWNER","BMP");
		}
		if(pathImgAlternateContact!=null){
			outState.putString("pathImgAlternateContact", pathImgAlternateContact);
			Log.w("SAVING ALTERNATE","BMP");
		}
		if(pathImgInstrument!=null){
			outState.putString("pathImgInstrument", pathImgInstrument);
			Log.w("SAVING pathImgInstrument","JPG");
		}
		
		outState.putBoolean("canDeleteImgOwner", canDeleteImgOwner);
		outState.putBoolean("canDeleteImgAlternateContact", canDeleteImgAlternateContact);
		outState.putBoolean("canDeleteImgInstrument", canDeleteImgInstrument);
		
		
		//save states of text fields, spinners and checkboxes
		outState.putString("txtHouseConnectionNumber", txtHouseConnectionNumber.getText().toString());
		outState.putString("txtFullName", txtFullName.getText().toString());
		outState.putString("txtNationalId", txtNationalId.getText().toString());
		outState.putString("txtDayExpirationDate", txtDayExpirationDate.getText().toString());
		outState.putString("txtYearExpirationDate", txtYearExpirationDate.getText().toString());
		outState.putString("txtEmail", txtEmail.getText().toString());
		outState.putString("txtPhoneLandLine", txtPhoneLandLine.getText().toString());
		outState.putString("txtMobilePhone", txtMobilePhone.getText().toString());
		outState.putString("txtPoBox", txtPoBox.getText().toString());
		outState.putString("txtZipCode", txtZipCode.getText().toString());
		outState.putString("txtLandId", txtLandId.getText().toString());
		outState.putString("txtAlternateContactName", txtAlternateContactName.getText().toString());
		outState.putString("txtContactNationalId", txtContactNationalId.getText().toString());
		outState.putString("txtContactDayExpirationDate", txtContactDayExpirationDate.getText().toString());
		outState.putString("txtContactYearExpirationDate", txtContactYearExpirationDate.getText().toString());
		outState.putString("txtContactPhoneLandLine", txtContactPhoneLandLine.getText().toString());
		outState.putString("txtContactMobilePhone", txtContactMobilePhone.getText().toString());
		outState.putString("txtContactEmail", txtContactEmail.getText().toString());
		outState.putString("txtContactStreetName", txtContactStreetName.getText().toString());
		outState.putString("txtContactZipCode", txtContactZipCode.getText().toString());
		outState.putString("txtContactPoBox", txtContactPoBox.getText().toString());
		
		if(newPremiseLocLat != null)
			outState.putString("newPremiseLocLat", newPremiseLocLat);
		if(newPremiseLocLng != null)
			outState.putString("newPremiseLocLng", newPremiseLocLng);
		if(newPremiseLocAlt != null)
			outState.putString("newPremiseLocAlt", newPremiseLocAlt);
		
		outState.putBoolean("locationCorrected", locationCorrected);
		
		
		outState.putInt("spnMonthExpirationDate", spnMonthExpirationDate.getSelectedItemPosition());
		outState.putInt("spnGender", spnGender.getSelectedItemPosition());
		outState.putInt("spnNationality", spnNationality.getSelectedItemPosition());
		outState.putInt("spnContactMonthExpirationDate", spnContactMonthExpirationDate.getSelectedItemPosition());
		outState.putInt("spnInhabitantType", spnInhabitantType.getSelectedItemPosition());
		outState.putInt("spnTarsheedType", spnTarsheedType.getSelectedItemPosition());
		
		outState.putBoolean("chkTarsheedGiven", chkTarsheedGiven.isChecked());

		outState.putBoolean("gettingLocation", gettingLocation);
		outState.putBoolean("gettingSearchLocation", gettingSearchLocation);
		if(gettingLocation)
			Log.w("SAVE GETTING LOC","TRUE");
		
		//new fields for search and known/unknown location
		outState.putString("txtSearchHcn", txtSearchHcn.getText().toString());
		outState.putString("txtSearchWaterMeterNumber", txtSearchWaterMeterNumber.getText().toString());
		outState.putString("txtSearchSEC", txtSearchSEC.getText().toString());
		outState.putBoolean("didSearch", didSearch);
		
		outState.putBoolean("hasLoadedRejected", hasLoadedRejected);

		super.onSaveInstanceState(outState);
		Log.w("SAVING","SAVING!!!");
	}

	



	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		
		if(savedInstanceState!=null){
			Log.w("RESTORING STATE","RESTORING!!!");
			pathImgOwner=savedInstanceState.getString("pathImgOwner");
			pathImgAlternateContact=savedInstanceState.getString("pathImgAlternateContact");
			pathImgInstrument=savedInstanceState.getString("pathImgInstrument");
			
			//invalidate imageviews to force images from camera to show if activity gets destroyed and instance state is saved!!!
			if(pathImgOwner!=null) {
				setPic(imgOwner, pathImgOwner);
			}
			if(pathImgAlternateContact!=null) {
				setPic(imgAlternateContact,pathImgAlternateContact);
			}
			if(pathImgInstrument!=null) {
				setPic(imgInstrument, pathImgInstrument);
			}
			
			
			canDeleteImgOwner=savedInstanceState.getBoolean("canDeleteImgOwner");
			canDeleteImgAlternateContact=savedInstanceState.getBoolean("canDeleteImgAlternateContact");
			canDeleteImgInstrument=savedInstanceState.getBoolean("canDeleteImgInstrument");
			
			//now load edit text, spinners and check boxes from saved state
			txtHouseConnectionNumber.setText(savedInstanceState.getString("txtHouseConnectionNumber",""));
			txtFullName.setText(savedInstanceState.getString("txtFullName",""));
			txtNationalId.setText(savedInstanceState.getString("txtNationalId",""));
			txtDayExpirationDate.setText(savedInstanceState.getString("txtDayExpirationDate",""));
			txtYearExpirationDate.setText(savedInstanceState.getString("txtYearExpirationDate",""));
			txtEmail.setText(savedInstanceState.getString("txtEmail",""));
			txtPhoneLandLine.setText(savedInstanceState.getString("txtPhoneLandLine",""));
			txtMobilePhone.setText(savedInstanceState.getString("txtMobilePhone",""));
			txtPoBox.setText(savedInstanceState.getString("txtPoBox",""));
			txtZipCode.setText(savedInstanceState.getString("txtZipCode",""));
			txtLandId.setText(savedInstanceState.getString("txtLandId",""));
			txtAlternateContactName.setText(savedInstanceState.getString("txtAlternateContactName",""));
			txtContactNationalId.setText(savedInstanceState.getString("txtContactNationalId",""));
			txtContactDayExpirationDate.setText(savedInstanceState.getString("txtContactDayExpirationDate",""));
			txtContactYearExpirationDate.setText(savedInstanceState.getString("txtContactYearExpirationDate",""));
			txtContactPhoneLandLine.setText(savedInstanceState.getString("txtContactPhoneLandLine",""));
			txtContactMobilePhone.setText(savedInstanceState.getString("txtContactMobilePhone",""));
			txtContactEmail.setText(savedInstanceState.getString("txtContactEmail",""));
			txtContactStreetName.setText(savedInstanceState.getString("txtContactStreetName",""));
			txtContactZipCode.setText(savedInstanceState.getString("txtContactZipCode",""));
			txtContactPoBox.setText(savedInstanceState.getString("txtContactPoBox",""));
			
			newPremiseLocLat = savedInstanceState.getString("newPremiseLocLat", null);
			newPremiseLocLng = savedInstanceState.getString("newPremiseLocLng", null);
			newPremiseLocAlt = savedInstanceState.getString("newPremiseLocAlt", null);

			spnMonthExpirationDate.setSelection(savedInstanceState.getInt("spnMonthExpirationDate",0));
			spnGender.setSelection(savedInstanceState.getInt("spnGender",0));
			spnNationality.setSelection(savedInstanceState.getInt("spnNationality",0));
			spnContactMonthExpirationDate.setSelection(savedInstanceState.getInt("spnContactMonthExpirationDate",0));
			spnInhabitantType.setSelection(savedInstanceState.getInt("spnInhabitantType",0));
			spnTarsheedType.setSelection(savedInstanceState.getInt("spnTarsheedType",0));
			
			chkTarsheedGiven.setChecked(savedInstanceState.getBoolean("chkTarsheedGiven",false));

			gettingLocation=savedInstanceState.getBoolean("gettingLocation");
			gettingSearchLocation=savedInstanceState.getBoolean("gettingSearchLocation");
			
			mCurrentPhotoPath=savedInstanceState.getString("mCurrentPhotoPath");
			if(gettingLocation)
				Log.w("RESTORE GETTING LOC","TRUE");
			
			if(mCurrentPhotoPath!=null)
				f=new File(mCurrentPhotoPath);
			
			//new fields
			txtSearchHcn.setText(savedInstanceState.getString("txtSearchHcn",""));
			txtSearchWaterMeterNumber.setText(savedInstanceState.getString("txtSearchWaterMeterNumber",""));
			txtSearchSEC.setText(savedInstanceState.getString("txtSearchSEC",""));
			didSearch=savedInstanceState.getBoolean("didSearch",false);
			
			hasLoadedRejected=savedInstanceState.getBoolean("hasLoadedRejected");
			
			locationCorrected=savedInstanceState.getBoolean("locationCorrected");
		}
	}





	private OnClickListener photoListener=new OnClickListener() {
		
		public void onClick(View v) {
			v.showContextMenu();
			
		}
	};
	
	/**
	 * postavi context menu kad se klikne na sliku
	 * ako je slika vec psotavljena onda ce imat opciju brisi, a ako nije onda samo capture i choose
	 */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {
    	super.onCreateContextMenu(menu, v, menuInfo);
		if(v.getId()==R.id.imgOwner) {
			menu.add(0, R.id.takeOwnerPhoto, 0, "Take photo with camera");
			menu.add(0, R.id.selectOwnerPhoto, 0, "Select photo from gallery");
			if(pathImgOwner==null)
				menu.setHeaderTitle("Select photo");
			else {
				menu.setHeaderTitle("Edit photo");
				menu.add(0, R.id.deleteOwnerPhoto, 0, "Delete photo");
			}
		}
		if(v.getId()==R.id.imgAlternateContact) {
			menu.add(0, R.id.takeAlternateContactPhoto, 0, "Take photo with camera");
			menu.add(0, R.id.selectAlternateContactPhoto, 0, "Select photo from gallery");
			if(pathImgAlternateContact==null)
				menu.setHeaderTitle("Select photo");
			else {
				menu.setHeaderTitle("Edit photo");
				menu.add(0, R.id.deleteAlternateContactPhoto, 0, "Delete photo");
			}
		}
		if(v.getId()==R.id.imgInstrument){
	    	menu.add(0, R.id.takeInstrumentPhoto, 0, "Take photo with camera");
			menu.add(0, R.id.selectInstrumentPhoto, 0, "Select photo from gallery");
			if(pathImgInstrument==null)
				menu.setHeaderTitle("Select photo");
			else {
				menu.setHeaderTitle("Edit photo");
				menu.add(0, R.id.deleteInstrumentPhoto, 0, "Delete photo");
			}
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		int id=item.getItemId();
		
		switch (id){
		case R.id.takeOwnerPhoto:
			try {
				f = setUpPhotoFile();
				mCurrentPhotoPath = f.getAbsolutePath();
				Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
				startActivityForResult(i, REQUEST_TAKE_OWNER_PHOTO);
			} catch (IOException e) {
			}	
			break;
		case R.id.takeAlternateContactPhoto:
			try {
				f = setUpPhotoFile();
				mCurrentPhotoPath = f.getAbsolutePath();
				Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
				startActivityForResult(i, REQUEST_TAKE_ALTERNATE_CONTACT_PHOTO);
			} catch (IOException e) {
			}
			break;
		case R.id.deleteOwnerPhoto:
			if(canDeleteImgOwner){
				File f=new File(pathImgOwner);
				if(f.delete()){
					sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
				}
			}
			imgOwner.setImageDrawable(getResources().getDrawable(R.drawable.capture));
			pathImgOwner=null;
			break;
		case R.id.deleteAlternateContactPhoto:
			if(canDeleteImgAlternateContact){
				File f1=new File(pathImgAlternateContact);
				if(f1.delete()){
					sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
				}
			}
			imgAlternateContact.setImageDrawable(getResources().getDrawable(R.drawable.capture));
			pathImgAlternateContact=null;
			break;
		case R.id.selectOwnerPhoto:
			Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			startActivityForResult(i, REQUEST_SELECT_OWNER_GALLERY_PHOTO); 
			break;
		case R.id.selectAlternateContactPhoto:
			Intent i1 = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			startActivityForResult(i1, REQUEST_SELECT_ALTERNATE_CONTACT_GALLERY_PHOTO); 
			break;
		case R.id.takeInstrumentPhoto:
			try {
				f = setUpPhotoFile();
				mCurrentPhotoPath = f.getAbsolutePath();
				Intent i5 = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				i5.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
				startActivityForResult(i5, REQUEST_TAKE_INSTRUMENT_PHOTO);
			} catch (IOException e) {
			}
			break;
		case R.id.deleteInstrumentPhoto:
			if(canDeleteImgInstrument){
				File f1=new File(pathImgInstrument);
				if(f1.delete()){
					sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
				}
			}
			imgInstrument.setImageDrawable(getResources().getDrawable(R.drawable.capture));
			pathImgInstrument=null;
			break;
		case R.id.selectInstrumentPhoto:
			Intent i6 = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			startActivityForResult(i6, REQUEST_SELECT_INSTRUMENT_PHOTO); 
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
				newPremiseLocLat=b.getString("premiseLat", null);
				newPremiseLocLng=b.getString("premiseLng", null);
				newPremiseLocAlt=b.getString("premiseAlt", null);
				locationCorrected=true;
				Toast.makeText(this, ""+newPremiseLocLat, Toast.LENGTH_LONG).show();
				break;
			case REQUEST_TAKE_OWNER_PHOTO:
					if(imgOwner!=null){
						setPic(imgOwner,mCurrentPhotoPath);
						canDeleteImgOwner=true;
						galleryAddPic();
					}
					pathImgOwner=mCurrentPhotoPath;
					clearImageFileHandlers();
				break;
			case REQUEST_TAKE_ALTERNATE_CONTACT_PHOTO:
					if(imgAlternateContact!=null){
						setPic(imgAlternateContact,mCurrentPhotoPath);
						canDeleteImgAlternateContact=true;
						galleryAddPic();
					}
					pathImgAlternateContact=mCurrentPhotoPath;
					clearImageFileHandlers();
				break;
			case REQUEST_SELECT_OWNER_GALLERY_PHOTO:
					setImageFromGalleryResult(data,"owner");
					canDeleteImgOwner=false;
				break;
			case REQUEST_SELECT_ALTERNATE_CONTACT_GALLERY_PHOTO:
					setImageFromGalleryResult(data,"alternate");
					canDeleteImgAlternateContact=false;
				break;
			case REQUEST_TAKE_INSTRUMENT_PHOTO:
				if(imgInstrument!=null){
					setPic(imgInstrument,mCurrentPhotoPath);
					canDeleteImgInstrument=true;
					galleryAddPic();
				}
				pathImgInstrument=mCurrentPhotoPath;
				clearImageFileHandlers();
			break;
			case REQUEST_SELECT_INSTRUMENT_PHOTO:
				setImageFromGalleryResult(data,"instrument");
				canDeleteImgInstrument=false;
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
        if(imageView.equals("owner")){
        	pathImgOwner=cursor.getString(columnIndex);
        	setPic(imgOwner, pathImgOwner);
        } else if(imageView.equals("alternate")){
        	pathImgAlternateContact = cursor.getString(columnIndex);
        	setPic(imgAlternateContact, pathImgAlternateContact);
        } else if(imageView.equals("instrument")){
        	pathImgInstrument = cursor.getString(columnIndex);
        	setPic(imgInstrument, pathImgInstrument);
        }  
        cursor.close();
	}
	
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
        String imageFileName = "NWC_customer_" + timeStamp + "";
        File image = File.createTempFile(
            imageFileName.substring(0, 28), 
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
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;
      
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
    
    /**
  	 * Enable or disable input form
  	 * @param Boolean enable
  	 */
  	private void toggleForm(Boolean enable){
  		btnSubmit.setEnabled(enable);
  		txtHouseConnectionNumber.setEnabled(false);
  		txtFullName.setEnabled(enable);
  		txtNationalId.setEnabled(enable);
  		txtDayExpirationDate.setEnabled(enable);
  		spnMonthExpirationDate.setEnabled(enable);
  		txtYearExpirationDate.setEnabled(enable);
  		spnGender.setEnabled(enable);
  		spnNationality.setEnabled(enable);
  		txtEmail.setEnabled(enable);
  		txtPhoneLandLine.setEnabled(enable);
  		txtMobilePhone.setEnabled(enable);
  		txtPoBox.setEnabled(enable);
  		txtZipCode.setEnabled(enable);
  		txtLandId.setEnabled(enable);
  		txtAlternateContactName.setEnabled(enable);
  		txtContactNationalId.setEnabled(enable);
  		txtContactDayExpirationDate.setEnabled(enable);
  		spnContactMonthExpirationDate.setEnabled(enable);
  		txtContactYearExpirationDate.setEnabled(enable);
  		spnInhabitantType.setEnabled(enable);
  		txtContactPhoneLandLine.setEnabled(enable);
  		txtContactMobilePhone.setEnabled(enable);
  		txtContactEmail.setEnabled(enable);
  		txtContactStreetName.setEnabled(enable);
    	txtContactZipCode.setEnabled(enable);
    	txtContactPoBox.setEnabled(enable);
  		spnTarsheedType.setEnabled(enable);
  		chkTarsheedGiven.setEnabled(enable);
  		btnGetLocation.setEnabled(enable);
  		
  	}
  	
  	
    /** SEARCH CUSTOMER ASYNC TASK **/
    static class SearchCustomer extends AsyncTask<String, Integer, Void> {

    	private ActivityCustomer activity=null;
        private String response_str=null;

        SearchCustomer(ActivityCustomer activity) {
            attach(activity);
        }
        void detach() {
        	activity=null;
        }
          
        void attach(ActivityCustomer activity) {
        	this.activity=activity;
        }

        @Override
        protected Void doInBackground(String... args) {
        	
            try {
            	response_str=null;
            	//search premise item
            	String urlString = activity.getResources().getString(R.string.url_search_customer);
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
                String sec="";
                if(activity.rejectedHcn!=null){
                	hcn=activity.rejectedHcn;
                }else if(args != null) {
                	reqEntity.addPart("gps_lat", new StringBody(args[0]));
                	reqEntity.addPart("gps_long", new StringBody(args[1]));
                	Log.w("GPS","lat:"+args[0]+ " -- lng:"+args[1]);
                	wmn=this.activity.txtSearchWaterMeterNumber.getText().toString().trim();
                } else {
	                hcn=this.activity.txtSearchHcn.getText().toString().trim();
					wmn=this.activity.txtSearchWaterMeterNumber.getText().toString().trim();
					sec=this.activity.txtSearchSEC.getText().toString().trim();
                }
				 
                if(hcn.length()>0){
                	reqEntity.addPart("house_connection_number", new StringBody(hcn));
                }
                if(wmn.length()>0){
                	reqEntity.addPart("water_meter_number", new StringBody(wmn));
                }
                if(sec.length()>0){
                	reqEntity.addPart("sec_db_number", new StringBody(sec));
                }
                
                post.setEntity(reqEntity);
                HttpResponse response = client.execute(post);
                resEntity = response.getEntity();
                response_str = EntityUtils.toString(resEntity);
                
            } catch (Exception e) {
                Log.w("CUSTOMER SARCH CONN ERROR","ERROR",e);
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
    		String dialogMessage=null;
        	if(response_str.contains("ERROR")) {
        		//nema konekcije
        		Log.w("Customer SEARCH ERROR","PREMISE ERROR:"+response_str);
        		new AlertDialog.Builder(this)
    		    .setTitle(getResources().getString(R.string.errors))
    		    .setMessage(response_str)
    		    .setPositiveButton(getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
    		        public void onClick(DialogInterface dialog, int which) { 
    		            dialog.dismiss();
    		        }
    		     })
    		     .show();
        	} else if(response_str.contains("NO_NEAREST_WMN_DETECTED")){
        		new AlertDialog.Builder(this)
    		    .setTitle(getResources().getString(R.string.errors))
    		    .setMessage(getResources().getString(R.string.error_multiple_wmn_not_near))
    		    .setPositiveButton(getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
    		        public void onClick(DialogInterface dialog, int which) { 
    		            dialog.dismiss();
    		        }
    		     })
    		     .show();
        		
        	
        	} else if(response_str.contains("DOUBLE_WMN_DETECTED")){
        		new AlertDialog.Builder(this)
    		    .setTitle(getResources().getString(R.string.errors))
    		    .setMessage(getResources().getString(R.string.error_multiple_wmn))
    		    .setPositiveButton(getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
    		        public void onClick(DialogInterface dialog, int which) { 
    		            dialog.dismiss();
    		        }
    		     })
    		     .show();
        		
        	
    		}else if(response_str.contains("NOT FOUND")){
        		//nije nadjen
    			Log.w("Customer SEARCH","NOT FOUND"+response_str);
    			
    			dialogMessage=getResources().getString(R.string.customer_not_found_big_fail);
    			//pustit cemo korisnika dalje AKO I SAMO AKO premise za taj HCN postoji i ako nije rejected!!
    			if(response_str.contains("PREMISE_NOT_EXISTS"))
    				dialogMessage+=getResources().getString(R.string.warning_premise_not_exists);
    			else if(response_str.contains("PREMISE_REJECTED"))
    				dialogMessage+=getResources().getString(R.string.warning_premise_rejected);
    			else{
    				dialogMessage=getResources().getString(R.string.customer_not_found_big);
    				clearAll();
        			didSearch=true;
        			
        			toggleForm(true);
        			txtFullName.requestFocus();
        			//parsiraj i nadji HCN iz response-a
        			String tmp="";
        			int pos=response_str.indexOf("=>");
        			if(pos!=-1 && response_str.length()>(pos+2)){
        				int end=0;
        				end=response_str.indexOf("\"", pos+2);
        				tmp=response_str.substring(pos+2,end);
        				if(response_str.length() > (end+1)){
        					String tmp2 = response_str.substring(end+1);
        					Gson gson=new Gson();
        	        		JSONCustomerItem tmpCust;
        	        		try{
        	        			tmpCust=gson.fromJson(tmp2, JSONCustomerItem.class);
        	        			newPremiseLocLat=tmpCust.newPremiseLat;
        	        			newPremiseLocLng=tmpCust.newPremiseLng;
        	        			newPremiseLocAlt=tmpCust.newPremiseAlt;
        	        			locationCorrected=false;
        	        		} catch (Exception e) {
								Log.w("PARSE ERROR",""+tmp2);
							}
        				}
        				txtHouseConnectionNumber.setText(tmp.trim());
        			}
    			}
    			
    			new AlertDialog.Builder(this)
    		    .setTitle(getResources().getString(R.string.customer_not_found))
    		    .setMessage(dialogMessage)
    		    .setPositiveButton(getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
    		        public void onClick(DialogInterface dialog, int which) { 
    		            dialog.dismiss();
    		        }
    		     })
    		     .show();
    			
        	} else if(response_str.contains("waiting_validation")) {
        		//unesen i nije jos validiran ili je odobren skroz disable forma i dalje!
        		Log.w("Customer SEARCH","NOT validated"+response_str);
    			
        		dialogMessage=getResources().getString(R.string.customer_not_validated_big);
    			if(response_str.contains("PREMISE_NOT_EXISTS"))
    				dialogMessage+=getResources().getString(R.string.warning_premise_not_exists);
    			else if(response_str.contains("PREMISE_REJECTED"))
    				dialogMessage+=getResources().getString(R.string.warning_premise_rejected);
    			else {
//    				clearAll();
//        			didSearch=true;
//        			toggleForm(true);
//        			txtHouseConnectionNumber.requestFocus();
    			}
    			new AlertDialog.Builder(this)
    		    .setTitle(getResources().getString(R.string.customer_not_validated))
    		    .setMessage(dialogMessage)
    		    .setPositiveButton(getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
    		        public void onClick(DialogInterface dialog, int which) { 
    		            dialog.dismiss();
    		        }
    		     })
    		     .show();
   			
        	} else if(response_str.contains("no_permission")) {
        		//nema dozvole da se trazi customer iz ovog grada disable forma i dalje!
        		Log.w("premise SEARCH","NO permission"+response_str);
    			
//    			clearAll();
//    			didSearch=true;
//    			toggleForm(true);
//    			txtHouseConnectionNumber.requestFocus();
    			new AlertDialog.Builder(this)
    		    .setTitle(getResources().getString(R.string.errors))
    		    .setMessage(getResources().getString(R.string.error_no_permission))
    		    .setPositiveButton(getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
    		        public void onClick(DialogInterface dialog, int which) { 
    		            dialog.dismiss();
    		        }
    		     })
    		     .show();
    		}else {
    			//nadjen i ima dozvola za edit
        		Gson gson=new Gson();
        		JSONCustomerItem customer;
        		try{
        			customer=gson.fromJson(response_str, JSONCustomerItem.class);
        			Log.w("CUSTOMER SEARCH","parsiran PREMISE!!!");
        			if(customer.premise_exists!=null && customer.premise_exists.equals("yes")){
        				clearAll();
            			didSearch=true;
            			locationCorrected=false;
            			fillFieldsWithData(customer);
            			toggleForm(true);
            			txtHouseConnectionNumber.setEnabled(false);
        				txtFullName.requestFocus();
            			txtSearchHcn.setText("");
            			txtSearchWaterMeterNumber.setText("");
            			txtSearchSEC.setText("");
            			
            			newPremiseLocLat=customer.newPremiseLat;
	        			newPremiseLocLng=customer.newPremiseLng;
	        			newPremiseLocAlt=customer.newPremiseAlt;
        			} else if(customer.premise_exists!=null && customer.premise_exists.equals("no")){
        				new AlertDialog.Builder(this)
            		    .setTitle(getResources().getString(R.string.customer_not_validated))
            		    .setMessage(getResources().getString(R.string.warning_premise_not_exists))
            		    .setPositiveButton(getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
            		        public void onClick(DialogInterface dialog, int which) { 
            		            dialog.dismiss();
            		        }
            		     })
            		     .show();
        			} else if(customer.premise_exists!=null && customer.premise_exists.equals("rejected")){
        				new AlertDialog.Builder(this)
            		    .setTitle(getResources().getString(R.string.customer_not_validated))
            		    .setMessage(getResources().getString(R.string.warning_premise_rejected))
            		    .setPositiveButton(getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
            		        public void onClick(DialogInterface dialog, int which) { 
            		            dialog.dismiss();
            		        }
            		     })
            		     .show();
        			}
        			
        			
        			
        			//ako je rejected 
        			if(rejectedHcn != null && rejectedHcn.length()>0) {
        				clearAll();
            			didSearch=true;
            			fillFieldsWithData(customer);
            			toggleForm(true);
            			txtFullName.requestFocus();
            			txtSearchHcn.setText("");
            			txtSearchWaterMeterNumber.setText("");
            			txtSearchSEC.setText("");
        				hasLoadedRejected=true;
        				btnSearch.setEnabled(false);
        				btnEnable.setEnabled(false);
        				txtHouseConnectionNumber.setEnabled(false);
        				txtFullName.requestFocus();
        				
        				newPremiseLocLat=customer.newPremiseLat;
	        			newPremiseLocLng=customer.newPremiseLng;
	        			newPremiseLocAlt=customer.newPremiseAlt;
	        			locationCorrected=false;
        			}
        		} catch (Exception e) {
					Log.w("GREsKA","NE MOGU PARSIRATI NADJENI CUSTOMER!!!"+response_str,e);
				}
        	}
        }
    	
    	searchTask=null;
    }
  	
  	
    private void fillFieldsWithData(JSONCustomerItem customer){
    	

    	
    	if(customer.house_connection_number!=null)
			txtHouseConnectionNumber.setText(customer.house_connection_number);
    	
    	if(customer.gender!=null){
    		Integer tmp=Integer.valueOf(customer.gender);
    		if(tmp==2)
    			spnGender.setSelection(1);
    		else 
    			spnGender.setSelection(0);
    	}
    	
    	if(customer.full_name!=null)
    		txtFullName.setText(customer.full_name);
    	
    	if(customer.nationality_id!=null){
    		JSONSpinnerItem tmp=new JSONSpinnerItem();
    		tmp.id=Integer.valueOf(customer.nationality_id);
    		spnNationality.setSelection(adptNationality.getPosition(tmp));
    	}
    	
    	if(customer.national_id!=null)
    		txtNationalId.setText(customer.national_id);
    	
    	if(customer.email!=null)
    		txtEmail.setText(customer.email);
    	
    	if(customer.phone_land_line!=null)
    		txtPhoneLandLine.setText(customer.phone_land_line);
    	
    	if(customer.mobile_phone!=null)
    		txtMobilePhone.setText(customer.mobile_phone);
  		
    	if(customer.po_box!=null)
    		txtPoBox.setText(customer.po_box);
  		
    	if(customer.zip_code!=null)
    		txtZipCode.setText(customer.zip_code);
  		
    	if(customer.land_id!=null)
    		txtLandId.setText(customer.land_id);
  		
    	if(customer.alternate_contact_name!=null)
    		txtAlternateContactName.setText(customer.alternate_contact_name);
  		
    	if(customer.contact_national_id!=null)
    		txtContactNationalId.setText(customer.contact_national_id);
  		
    	
    	if(customer.contact_phone_land_line!=null)
    		txtContactPhoneLandLine.setText(customer.contact_phone_land_line);
  		
    	if(customer.contact_mobile_phone!=null)
    		txtContactMobilePhone.setText(customer.contact_mobile_phone);
  		
    	if(customer.contact_email!=null)
    		txtContactEmail.setText(customer.contact_email);
  		
    	if(customer.inhabitant_type_id!=null){
    		JSONSpinnerItem tmp=new JSONSpinnerItem();
    		tmp.id=Integer.valueOf(customer.inhabitant_type_id);
    		spnInhabitantType.setSelection(adptInhabitantType.getPosition(tmp));
    	}
  		
    	if(customer.tarsheed_type_id!=null){
    		JSONSpinnerItem tmp=new JSONSpinnerItem();
    		tmp.id=Integer.valueOf(customer.tarsheed_type_id);
    		spnTarsheedType.setSelection(adptTarsheedType.getPosition(tmp));
    	}
    	
    	if(customer.tarsheed_given!=null){
    		Integer tmpTg=Integer.valueOf(customer.tarsheed_given);
    		if(tmpTg == 0) chkTarsheedGiven.setChecked(false);
    		else chkTarsheedGiven.setChecked(true);
    	}
    	
    	if(customer.day_expiration_date!=null)
    		txtDayExpirationDate.setText(customer.day_expiration_date);	
    	if(customer.year_expiration_date!=null)
    		txtYearExpirationDate.setText(customer.year_expiration_date);
    	
    	if(customer.contact_day_expiration_date!=null)
    		txtContactDayExpirationDate.setText(customer.contact_day_expiration_date);
    	if(customer.contact_year_expiration_date!=null)
    		txtContactYearExpirationDate.setText(customer.contact_year_expiration_date);
  		
    	if(customer.month_expiration_date!=null){
    		Integer tmp =Integer.valueOf(customer.month_expiration_date);
    		spnMonthExpirationDate.setSelection(adptMonthExpirationDate.getPosition(tmp));
    	}
    	
    	if(customer.contact_month_expiration_date!=null){
    		Integer tmp =Integer.valueOf(customer.contact_month_expiration_date);
    		spnContactMonthExpirationDate.setSelection(adptContactMonthExpirationDate.getPosition(tmp));
    	}
    	
    	
    	if(customer.contact_street_name!=null)
    		txtContactStreetName.setText(customer.contact_street_name);
  		
    	if(customer.contact_zip_code!=null)
    		txtContactZipCode.setText(customer.contact_zip_code);
  		
    	if(customer.contact_po_box!=null)
    		txtContactPoBox.setText(customer.contact_po_box);
  		
    	
    	
    	String dialogMessage=null;
    	dialogMessage=getResources().getString(R.string.customer_found_big);
    	
    	if(customer.premise_exists.equals("no"))
			dialogMessage+=getResources().getString(R.string.warning_premise_not_exists);
		if(customer.premise_exists.equals("rejected"))
			dialogMessage+=getResources().getString(R.string.warning_premise_rejected);
		
		
    	new AlertDialog.Builder(this)
	    .setTitle(getResources().getString(R.string.customer_found))
	    .setMessage(dialogMessage)
	    .setPositiveButton(getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	            dialog.dismiss();
	        }
	     })
	     .show();
    	locationCorrected=false;
    }
    
    private void clearAll(){
    	//images
    	imgOwner.setImageDrawable(getResources().getDrawable(R.drawable.capture));
    	imgAlternateContact.setImageDrawable(getResources().getDrawable(R.drawable.capture));
    	imgInstrument.setImageDrawable(getResources().getDrawable(R.drawable.capture));

    	pathImgOwner=null;
    	pathImgAlternateContact=null;
    	canDeleteImgOwner=false;
    	canDeleteImgAlternateContact=false;
    	pathImgInstrument=null;
    	canDeleteImgInstrument=false;
   	
    	//spinners
    	spnMonthExpirationDate.setSelection(0);
    	spnGender.setSelection(0);
    	spnNationality.setSelection(0);
    	spnContactMonthExpirationDate.setSelection(0);
    	spnInhabitantType.setSelection(0);
    	spnTarsheedType.setSelection(0);
    	
    	//text fields
    	txtHouseConnectionNumber.setText("");
    	txtFullName.setText("");
    	txtNationalId.setText("");
    	txtDayExpirationDate.setText("");
    	txtYearExpirationDate.setText("");
    	txtEmail.setText("");
    	txtPhoneLandLine.setText("");
    	txtMobilePhone.setText("");
    	txtPoBox.setText("");
    	txtZipCode.setText("");
    	txtLandId.setText("");
    	txtAlternateContactName.setText("");
    	txtContactNationalId.setText("");
    	txtContactDayExpirationDate.setText("");
    	txtContactYearExpirationDate.setText("");
    	txtContactPhoneLandLine.setText("");
    	txtContactMobilePhone.setText("");
    	txtContactEmail.setText("");
    	txtContactStreetName.setText("");
    	txtContactZipCode.setText("");
    	txtContactPoBox.setText("");
    	
    	//checkbox
    	chkTarsheedGiven.setChecked(false);
    	
    	
    	txtSearchHcn.setText("");
    	
    	txtSearchWaterMeterNumber.setText("");
    	didSearch=false;
    	
    	
    	//my location
    	myLocation=null;
    	locationCorrected=false;

    }
    

	
	
	
}