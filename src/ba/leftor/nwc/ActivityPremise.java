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

public class ActivityPremise extends Activity{
	
	//define variables for rotation aware task (so upload can resume if orientation chnages during upload)
	private SubmitPremiseForm task=null;
	private SearchPremise searchTask=null;
	private ReserveNewHcn getHcnTask=null;
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
	
	//define spinners
	private Spinner spnPremiseType;
	
	//define array lists
	ArrayList<JSONSpinnerItem> lstPremiseTypes;
	ArrayList<JSONSpinnerItem> lstDistricts;
	ArrayList<JSONSpinnerItem> lstWmStatuses;
	
	//my location
	private String myLat;
	private String myLng;
	private String myAlt;
	private String premiseLat;
	private String premiseLng;
	private String premiseAlt;
	private TextView txtPremiseLat;
	private TextView txtPremiseLng;
	
	
	//define text fields
	private EditText txtHouseConnectionNumber;
	private EditText txtPremiseId;
	private EditText txtSTC;
	private EditText txtSEC;
	private EditText txtSEC2;
	private EditText txtSEC3;
	private EditText txtSEC4;
	private EditText txtSEC5;
	private EditText txtTotalElectricalMeters;
	private EditText txtFloorCount;
	private EditText txtPremiseName;
	private EditText txtUseOfBuilding;
	private EditText txtWaterMeterNumber;
	private EditText txtWaterMeterNumber2;
	private EditText txtWaterMeterNumber3;
	private EditText txtWaterMeterNumber4;
	private EditText txtWaterMeterNumber5;
	//Add more Watermeter button
	private Button addMoreWaterMeter;
	//Add more SEC numbers
	private Button addMoreSecNumber;
	//my location
	private MyLocation myLocation;
	private Boolean gettingLocation=false; //to continue finding location on orientation change
	
	//HOUSE CONNECTION KONWON-UNKNOWN, DISTRICT AND SEARCH FIELDS
	private Boolean didSearch=false; //USER MUST FIRST SEARCH BEFORE GETTING PERMISSION TO FILL FORM
	private EditText txtSearchHcn;
	private EditText txtSearchWaterMeterNumber;
	private Button btnSearch;
	private Button btnEnable;
	private CheckBox chkHcnKnown;
	private CheckBox chkHcnUnknown;
	private CheckBox chkSECKnown;
	private CheckBox chkSECUnknown;
	private Spinner spnDistrict;
	
	private Button btnSubmit;
	private Button btnGetLocation;
	private Button btnGetLocationNoMap;
	
	//water meter known i unknown
	private CheckBox chkWmnKnown;
	private CheckBox chkWmnUnknown;
	private Spinner spnWmStatus;
	private Boolean hcnAutoGenerated=false;
	
	//variables for rejected premises
	private Boolean hasLoadedRejected=false;
	private String rejectedHcn=null;
	
	//adapteri za spnnere
	ArrayAdapter<JSONSpinnerItem> adptDistrict;
	ArrayAdapter<JSONSpinnerItem> adptPremiseType;
	ArrayAdapter<JSONSpinnerItem> adptWmStatus;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.premise_form);
		
		
		// display user name on top
		TextView txtWelcome = (TextView) findViewById(R.id.txtWelcome);
		JSONUSerItem user = GlobalFunctionsAndConstants.getUser(this);
		if (user.fullName.length() > 1)
			txtWelcome.setText(ActivityPremise.this.getResources().getString(R.string.welcome) + user.fullName);
		else
			txtWelcome.setText(ActivityPremise.this.getResources().getString(R.string.welcome) + user.username);
		
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
		
		
		//find edit text, spinners and checkboxes in layout
		txtHouseConnectionNumber=(EditText)findViewById(R.id.txtHouseConnectionNumber);
		txtPremiseId=(EditText)findViewById(R.id.txtPremiseId);
		txtSTC=(EditText)findViewById(R.id.txtSTC);
		txtSEC=(EditText)findViewById(R.id.txtSEC);
		txtSEC2=(EditText)findViewById(R.id.txtSEC2);
		txtSEC3=(EditText)findViewById(R.id.txtSEC3);
		txtSEC4=(EditText)findViewById(R.id.txtSEC4);
		txtSEC5=(EditText)findViewById(R.id.txtSEC5);
		txtTotalElectricalMeters=(EditText)findViewById(R.id.txtTotalElectricalMeters);
		txtFloorCount=(EditText)findViewById(R.id.txtFloorCount);
		txtPremiseName=(EditText)findViewById(R.id.txtPremiseName);
		txtUseOfBuilding=(EditText)findViewById(R.id.txtUseOfBuilding);
		txtWaterMeterNumber=(EditText)findViewById(R.id.txtWaterMeterNumber);
		txtWaterMeterNumber2=(EditText)findViewById(R.id.txtWaterMeterNumber2);
		txtWaterMeterNumber3=(EditText)findViewById(R.id.txtWaterMeterNumber3);
		txtWaterMeterNumber4=(EditText)findViewById(R.id.txtWaterMeterNumber4);
		txtWaterMeterNumber5=(EditText)findViewById(R.id.txtWaterMeterNumber5);
		addMoreWaterMeter=(Button)findViewById(R.id.addMoreWaterMeter);
		addMoreWaterMeter.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				addMoreWaterMeterNumbers();
			}
		});
		addMoreSecNumber=(Button)findViewById(R.id.addMoreSecNumber);
		addMoreSecNumber.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				addMoreSecNumbers();
			}
		});
		//find textviews
		txtPremiseLat=(TextView)findViewById(R.id.txtPremiseLat);
		txtPremiseLng=(TextView)findViewById(R.id.txtPremiseLng);
		
		spnPremiseType=(Spinner) findViewById(R.id.spnPremiseType);
		
		//find search fields, and hcn-district control fields
		txtSearchHcn=(EditText)findViewById(R.id.txtSearchHcn);
		
		txtSearchWaterMeterNumber=(EditText)findViewById(R.id.txtSearchWaterMeterNumber);
		chkHcnKnown=(CheckBox)findViewById(R.id.chkHcnKnown);
		chkHcnUnknown=(CheckBox)findViewById(R.id.chkHcnUnknown);
		chkSECKnown=(CheckBox)findViewById(R.id.chkSECKnown);
		chkSECUnknown=(CheckBox)findViewById(R.id.chkSECUnknown);
		spnDistrict=(Spinner)findViewById(R.id.spnDistrict);
		
		spnWmStatus=(Spinner)findViewById(R.id.spnWmStatus);
		chkWmnKnown=(CheckBox)findViewById(R.id.chkWmnKnown);
		chkWmnUnknown=(CheckBox)findViewById(R.id.chkWmnUnknown);
		
		//load premise types, districts and water meter statuses from db
		MyDataSource dataSource=new MyDataSource(this);
		dataSource.open();
		lstPremiseTypes=dataSource.getCatalogItems("premise_type");
		lstDistricts=dataSource.getCatalogItems("district");
		lstWmStatuses=dataSource.getCatalogItems("water_meter_status");
		//select district item
		JSONSpinnerItem noDistrict=new JSONSpinnerItem();
		noDistrict.id=-1;
		noDistrict.text="Please select";
		lstDistricts.add(0, noDistrict);
		//select water meter status item
		JSONSpinnerItem noWaterMeterStatus=new JSONSpinnerItem();
		noWaterMeterStatus.id=-1;
		noWaterMeterStatus.text="Please select";
		lstWmStatuses.add(0, noWaterMeterStatus);
		dataSource.close();
		
		//premiseType adapter
		adptPremiseType = new ArrayAdapter(this,android.R.layout.simple_spinner_item, lstPremiseTypes);
		adptPremiseType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnPremiseType.setAdapter(adptPremiseType);
		
		//district adapter
		adptDistrict = new ArrayAdapter(this,android.R.layout.simple_spinner_item, lstDistricts);
		adptDistrict.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnDistrict.setAdapter(adptDistrict);
		
		//wmstatus adapter
		adptWmStatus = new ArrayAdapter(this,android.R.layout.simple_spinner_item, lstWmStatuses);
		adptWmStatus.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnWmStatus.setAdapter(adptWmStatus);
		
		
		//submit form button
		btnSubmit=(Button)findViewById(R.id.btnSubmit);
		btnSubmit.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				validateAndSubmit();
			}
		});
		
		//get location button
		btnGetLocation=(Button)findViewById(R.id.btnGetLocation);
		btnGetLocation.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				startActivityForResult(new Intent(ActivityPremise.this,ActivityMap.class), REQUEST_MAP_ACTIVITY);
				
			}
		});
		
		//get location without map button
		btnGetLocationNoMap=(Button)findViewById(R.id.btnGetLocationNoMap);
		btnGetLocationNoMap.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				getGPSLocation();				
			}
		});
		
		//checkBox known and unknown
		chkHcnKnown.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				LinearLayout llDistrict=(LinearLayout)findViewById(R.id.llDistrict);
				if(isChecked){
					chkHcnUnknown.setChecked(false);
					hcnAutoGenerated=false;
					txtHouseConnectionNumber.setVisibility(View.VISIBLE);
					txtHouseConnectionNumber.setEnabled(true);
					if(llDistrict!=null) llDistrict.setVisibility(View.GONE);
				} else {
					txtHouseConnectionNumber.setVisibility(View.GONE);
				}
				
			}
		});
		chkHcnUnknown.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				LinearLayout llDistrict=(LinearLayout)findViewById(R.id.llDistrict);
				if(isChecked) {
					chkHcnKnown.setChecked(false);
					txtHouseConnectionNumber.setVisibility(View.VISIBLE);
					txtHouseConnectionNumber.setEnabled(false);
					if(llDistrict!=null) llDistrict.setVisibility(View.VISIBLE);
				} else {
					if(llDistrict!=null) llDistrict.setVisibility(View.GONE);
				}
				
			}
		});
		
		//checkBox known and unknown for water meter number
		chkWmnKnown.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				LinearLayout llWmStatus=(LinearLayout)findViewById(R.id.llWmStatus);
				if(isChecked){
					chkWmnUnknown.setChecked(false);
					txtWaterMeterNumber.setVisibility(View.VISIBLE);
					addMoreWaterMeter.setVisibility(View.VISIBLE);
					if(llWmStatus!=null) llWmStatus.setVisibility(View.VISIBLE);	
				} else {
					if(llWmStatus!=null) llWmStatus.setVisibility(View.GONE);
					txtWaterMeterNumber.setVisibility(View.GONE);
					txtWaterMeterNumber2.setVisibility(View.GONE);
					txtWaterMeterNumber3.setVisibility(View.GONE);
					txtWaterMeterNumber4.setVisibility(View.GONE);
					txtWaterMeterNumber5.setVisibility(View.GONE);
					addMoreWaterMeter.setVisibility(View.GONE);
				}
				
			}
		});
		
		chkWmnUnknown.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				LinearLayout llWmStatus=(LinearLayout)findViewById(R.id.llWmStatus);
				if(isChecked) {
					chkWmnKnown.setChecked(false);
					txtWaterMeterNumber.setVisibility(View.GONE);
					txtWaterMeterNumber2.setVisibility(View.GONE);
					txtWaterMeterNumber3.setVisibility(View.GONE);
					txtWaterMeterNumber4.setVisibility(View.GONE);
					txtWaterMeterNumber5.setVisibility(View.GONE);
					addMoreWaterMeter.setVisibility(View.GONE);
					//addMoreWaterMeter.setVisibility(View.GONE);

					if(llWmStatus!=null) llWmStatus.setVisibility(View.VISIBLE);
				} else {
					if(llWmStatus!=null) llWmStatus.setVisibility(View.GONE);
				}
				
			}
		});
		
		
		chkSECKnown.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					chkSECUnknown.setChecked(false);
					txtSEC.setVisibility(View.VISIBLE);
					addMoreSecNumber.setVisibility(View.VISIBLE);
				} else {
					txtSEC.setVisibility(View.GONE);
					txtSEC2.setVisibility(View.GONE);
					txtSEC3.setVisibility(View.GONE);
					txtSEC4.setVisibility(View.GONE);
					txtSEC5.setVisibility(View.GONE);
					addMoreSecNumber.setVisibility(View.GONE);
				}
				
			}
		});
		chkSECUnknown.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked) {
					chkSECKnown.setChecked(false);
					txtSEC.setVisibility(View.GONE);
					txtSEC2.setVisibility(View.GONE);
					txtSEC3.setVisibility(View.GONE);
					txtSEC4.setVisibility(View.GONE);
					txtSEC5.setVisibility(View.GONE);
					addMoreSecNumber.setVisibility(View.GONE);
				} else {
					
				}
				
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
					new AlertDialog.Builder(ActivityPremise.this)
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
		
		// enable form button
		btnEnable=(Button)findViewById(R.id.btnEnable);
		btnEnable.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				didSearch=true;
				toggleForm(didSearch);
				chkHcnUnknown.setChecked(true);
				chkWmnUnknown.setChecked(true);
				txtHouseConnectionNumber.setEnabled(false);
				txtWaterMeterNumber.requestFocus();
				
			}
		});
		
		//generate new hcn button
		Button btnReserveHcn=(Button)findViewById(R.id.btnReserveHcn);	
		btnReserveHcn.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				reserveNewHcn();
				
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
		
		//rotation aware task for reserving house connection number
		try {
			getHcnTask=(ReserveNewHcn)getLastNonConfigurationInstance();
		} catch (Exception e) {
		}
		if(getHcnTask!=null){
			dialog = new ProgressDialog(this);
            dialog.setMessage(getResources().getString(R.string.sending));
            dialog.setIndeterminate(false);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setProgress(0);
            dialog.show();
            getHcnTask.attach(this);
			
		    
			if (getHcnTask.response_str!=null) {
				markReservationAsDone();
			}
		}
		
		
		
		
	}
	
	//addmorewaternumbers
	private void addMoreWaterMeterNumbers() {
		if(chkWmnKnown.isChecked())
		{
		txtWaterMeterNumber2.setVisibility(View.VISIBLE);
		txtWaterMeterNumber3.setVisibility(View.VISIBLE);
		txtWaterMeterNumber4.setVisibility(View.VISIBLE);
		txtWaterMeterNumber5.setVisibility(View.VISIBLE);
		}
		else if(chkWmnUnknown.isChecked())
		{
		txtWaterMeterNumber2.setVisibility(View.GONE);
		txtWaterMeterNumber3.setVisibility(View.GONE);
		txtWaterMeterNumber4.setVisibility(View.GONE);
		txtWaterMeterNumber5.setVisibility(View.GONE);
		}
	}
	//add more sec numbers
	private void addMoreSecNumbers(){
		if(chkSECKnown.isChecked()){
			txtSEC2.setVisibility(View.VISIBLE);
			txtSEC3.setVisibility(View.VISIBLE);
			txtSEC4.setVisibility(View.VISIBLE);
			txtSEC5.setVisibility(View.VISIBLE);
		}
		else if(chkSECUnknown.isChecked()){
			txtSEC2.setVisibility(View.GONE);
			txtSEC3.setVisibility(View.GONE);
			txtSEC4.setVisibility(View.GONE);
			txtSEC5.setVisibility(View.GONE);
		}
	}
	//validate and submit form
	private void validateAndSubmit(){
		Boolean error=false;

		
		if(txtPremiseId.getText().toString().length()<1) {
			txtPremiseId.setError(getResources().getString(R.string.field_required));
			if(!error) txtPremiseId.requestFocus();
			error=true;
		}
		else txtPremiseId.setError(null);
		
		if(txtSTC.getText().toString().length()<1) {
			txtSTC.setError(getResources().getString(R.string.field_required));
			if(!error) txtSTC.requestFocus();
			error=true;
		}
		else txtSTC.setError(null);
		
		
		
		if(txtTotalElectricalMeters.getText().toString().length()<1) {
			txtTotalElectricalMeters.setError(getResources().getString(R.string.field_required));
			if(!error) txtTotalElectricalMeters.requestFocus();
			error=true;
		}
		else txtTotalElectricalMeters.setError(null);
		
		if(txtFloorCount.getText().toString().length()<1) {
			txtFloorCount.setError(getResources().getString(R.string.field_required));
			if(!error) txtFloorCount.requestFocus();
			error=true;
		}
		else txtFloorCount.setError(null);
		
		if(txtUseOfBuilding.getText().toString().length()<1) {
			txtUseOfBuilding.setError(getResources().getString(R.string.field_required));
			if(!error) txtUseOfBuilding.requestFocus();
			error=true;
		}
		else txtUseOfBuilding.setError(null);
		
		
		//check water meter number
		if(chkWmnKnown.isChecked())
		{
			
			if(txtWaterMeterNumber.getText().toString().trim().length()<1) {
				txtWaterMeterNumber.setError(getResources().getString(R.string.wmn_known_error));
			
				if(!error) txtWaterMeterNumber.requestFocus();
				error=true;
			} else txtWaterMeterNumber.setError(null);
		}
		if(chkWmnUnknown.isChecked())
		{
			if(spnWmStatus.getSelectedItemPosition()==0 && !error) {
				new AlertDialog.Builder(ActivityPremise.this)
			    .setTitle(getResources().getString(R.string.errors))
			    .setMessage(getResources().getString(R.string.wmn_unknown_error))
			    .setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int which) { 
			            dialog.dismiss();
			        }
			     })
			     .show();
				
				error=true;
			}
		}
		if(!chkWmnKnown.isChecked() && !chkWmnUnknown.isChecked() && !error){
			new AlertDialog.Builder(ActivityPremise.this)
		    .setTitle(getResources().getString(R.string.errors))
		    .setMessage(getResources().getString(R.string.wmn_error))
		    .setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) { 
		            dialog.dismiss();
		        }
		     })
		     .show();
			
			error=true;
		}

		
		//check SEC number
		if(chkSECKnown.isChecked())
		{
			if(txtSEC.getText().toString().trim().length()<1) {
				txtSEC.setError(getResources().getString(R.string.field_required));
				if(!error) txtSEC.requestFocus();
				error=true;
			}
			else if(!GlobalFunctionsAndConstants.isSECValid(txtSEC.getText().toString().trim())){
				txtSEC.setError(getResources().getString(R.string.error_sec_wrong_format));
				if(!error) txtSEC.requestFocus();
				new AlertDialog.Builder(ActivityPremise.this)
			    .setTitle(getResources().getString(R.string.errors))
			    .setMessage(getResources().getString(R.string.error_sec_wrong_format))
			    .setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int which) { 
			            dialog.dismiss();
			        }
			     })
			     .show();
				error=true;
			}
			else txtSEC.setError(null);
		}
		if(!chkSECKnown.isChecked() && !chkSECUnknown.isChecked() && !error){
			new AlertDialog.Builder(ActivityPremise.this)
		    .setTitle(getResources().getString(R.string.errors))
		    .setMessage(getResources().getString(R.string.sec_error))
		    .setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) { 
		            dialog.dismiss();
		        }
		     })
		     .show();
			
			error=true;
		}

		//check house connection number
		if(chkHcnKnown.isChecked())
		{
			if(txtHouseConnectionNumber.getText().toString().trim().length()<1) {
				txtHouseConnectionNumber.setError(getResources().getString(R.string.hcn_known_error));
				if(!error) txtHouseConnectionNumber.requestFocus();
				error=true;
			}else if(!GlobalFunctionsAndConstants.isHCNValid(txtHouseConnectionNumber.getText().toString().trim()))
			{
				txtHouseConnectionNumber.setError(getResources().getString(R.string.error_hcn_invalid_characters));
				if(!error) txtHouseConnectionNumber.requestFocus();
				error=true;
			} else txtHouseConnectionNumber.setError(null);
		}
		if(chkHcnUnknown.isChecked())
		{
			if(txtHouseConnectionNumber.getText().toString().length() < 10) {
				new AlertDialog.Builder(ActivityPremise.this)
			    .setTitle(getResources().getString(R.string.errors))
			    .setMessage(getResources().getString(R.string.error_hcn_reservation_validate))
			    .setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int which) { 
			            dialog.dismiss();
			        }
			     })
			     .show();
				
				error=true;
			}
		}
		if(!chkHcnKnown.isChecked() && !chkHcnUnknown.isChecked()){
			new AlertDialog.Builder(ActivityPremise.this)
		    .setTitle(getResources().getString(R.string.errors))
		    .setMessage(getResources().getString(R.string.hcn_error))
		    .setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) { 
		            dialog.dismiss();
		        }
		     })
		     .show();
			
			error=true;
		}
		
		
		if((myLat==null || myLng==null || myAlt==null || premiseAlt==null || premiseLat==null || premiseLng==null) && error==false){
			new AlertDialog.Builder(ActivityPremise.this)
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

    	private ActivityPremise activity=null;
        private int percent = 0;
        private long totalSize;
        private int  currentProgress;
        private String response_str=null;

        SubmitPremiseForm(ActivityPremise activity) {
            attach(activity);
        }
        void detach() {
        	activity=null;
        }
          
        void attach(ActivityPremise activity) {
        	this.activity=activity;
        }

        @Override
        protected Void doInBackground(Void... arg) {
        	
            	
            	//check water meter number first!
            	String checkResponse="";
            	
            	try {
                	//search premise item
            		
                	String urlString = activity.getResources().getString(R.string.url_check_wmn);
                    HttpClient client = new DefaultHttpClient();
                    HttpEntity resEntity;
                    HttpPost post = new HttpPost(urlString);
                    CustomMultiPartEntity reqEntity = new CustomMultiPartEntity(new ProgressListener()
        			{
        				public void transferred(long num)
        				{
        					
        				}
        			});
                    
                    if(activity.chkWmnKnown.isChecked()){
        //            	String wmn2=this.activity.txtWaterMeterNumber2.getText().toString().trim();
                    	String wmn=this.activity.txtWaterMeterNumber.getText().toString().trim();
                    	if(wmn.length()>0){
                        	reqEntity.addPart("wmn", new StringBody(wmn));
                        	reqEntity.addPart("checkWmn", new StringBody("yes"));
                        }
                    	
                    }else {
                    	reqEntity.addPart("checkWmn", new StringBody("no"));
                    }
                    
                    
                    //get sarch fields and add to req entity
                    String hcn=this.activity.txtHouseConnectionNumber.getText().toString().trim();

                    
                    if(hcn.length()>0){
                    	reqEntity.addPart("hcn", new StringBody(hcn));
                    }
                    
                    
                    
                    post.setEntity(reqEntity);
                    HttpResponse response = client.execute(post);
                    resEntity = response.getEntity();
                    checkResponse = EntityUtils.toString(resEntity);
                } catch (Exception e) {
                    Log.w("PREMISE SUBMIT CONN ERROR","ERROR",e);
                    response_str="ERROR: " + activity.getResources().getString(R.string.require_connection);
                    return null;
                }
	            
            	Log.w("Check response",checkResponse);
            	if(checkResponse.equalsIgnoreCase("ok")){
            	
                    try {
                    	response_str=null;
                    	
		            	//add prmise item to database
		            	JSONPremiseItem item=new JSONPremiseItem();
		            	
		        		item.gps_latitude_created=activity.myLat;
		        		item.gps_longitude_created=activity.myLng;
		        		item.gps_altitude_created=activity.myAlt;
		        		item.gps_latitude=activity.premiseLat;
		        		item.gps_longitude=activity.premiseLng;
		        		item.gps_altitude=activity.premiseAlt;
		        		item.house_connection_number=activity.txtHouseConnectionNumber.getText().toString().trim();
		        		item.premise_id=activity.txtPremiseId.getText().toString().trim();
		        		item.stc_db_number=activity.txtSTC.getText().toString().trim();
		        		item.sec_db_number=activity.txtSEC.getText().toString().trim();
		        		item.sec_db_number2=activity.txtSEC2.getText().toString().trim();
		        		item.sec_db_number3=activity.txtSEC3.getText().toString().trim();
		        		item.sec_db_number4=activity.txtSEC4.getText().toString().trim();
		        		item.sec_db_number5=activity.txtSEC5.getText().toString().trim();
		        		item.total_electrical_meters=activity.txtTotalElectricalMeters.getText().toString().trim();
		        		item.floor_count=activity.txtFloorCount.getText().toString().trim();
		        		item.premise_name=activity.txtPremiseName.getText().toString().trim();
		        		item.use_of_building=activity.txtUseOfBuilding.getText().toString().trim();
		        		item.premise_type_id=String.valueOf(((JSONSpinnerItem)activity.lstPremiseTypes.get(activity.spnPremiseType.getSelectedItemPosition())).id);
		        		item.photo_house1=activity.pathImgHouse1;
		        		item.photo_house2=activity.pathImgHouse2;
		        		item.photo_house3=activity.pathImgHouse3;
		        		item.photo_premise_connection=activity.pathImgConnection;
		        		item.photo_stc=activity.pathImgSTC;
		        		item.photo_sec=activity.pathImgSEC;         
		        		item.water_meter_number=activity.txtWaterMeterNumber.getText().toString().trim();
		        		item.water_meter_number2=activity.txtWaterMeterNumber2.getText().toString().trim();
		        		item.water_meter_number3=activity.txtWaterMeterNumber3.getText().toString().trim();
		        		item.water_meter_number4=activity.txtWaterMeterNumber4.getText().toString().trim();
		        		item.water_meter_number5=activity.txtWaterMeterNumber5.getText().toString().trim();
		        		
		        		if(activity.hcnAutoGenerated)
		        			item.hcnAutoGenerated="1";
		        		else
		        			item.hcnAutoGenerated="0";
		        		
		        		if(activity.chkHcnUnknown.isChecked()){
		        			item.district=String.valueOf(((JSONSpinnerItem)activity.lstDistricts.get(activity.spnDistrict.getSelectedItemPosition())).id);
		        		} else if(activity.chkHcnKnown.isChecked()){
		        			item.district="-1";
		        		}
		        		if(activity.chkWmnUnknown.isChecked() && !activity.chkWmnKnown.isChecked()){
		        			item.water_meter_status=String.valueOf(((JSONSpinnerItem)activity.lstWmStatuses.get(activity.spnWmStatus.getSelectedItemPosition())).id);
		        			item.water_meter_number="-1";
		        			item.water_meter_number2="-1";
		        			item.water_meter_number3="-1";
		        			item.water_meter_number4="-1";
		        			item.water_meter_number5="-1";
		        		} else if(activity.chkWmnKnown.isChecked()){
		        			item.water_meter_number=activity.txtWaterMeterNumber.getText().toString().trim();
		        			if(activity.txtWaterMeterNumber2.length() > 0){
		        				item.water_meter_number2=activity.txtWaterMeterNumber2.getText().toString().trim();
		        			}else item.water_meter_number2="-1";	
		        			
		        			if(activity.txtWaterMeterNumber3.length() > 0){
		        				item.water_meter_number3=activity.txtWaterMeterNumber3.getText().toString().trim();
		        			}else item.water_meter_number3="-1"; 
		        				
		        			if(activity.txtWaterMeterNumber4.length() > 0){
		        				item.water_meter_number4=activity.txtWaterMeterNumber4.getText().toString().trim();
		        			}else item.water_meter_number4="-1";
		        			
		        			if(activity.txtWaterMeterNumber5.length() > 0){
		        				item.water_meter_number5=activity.txtWaterMeterNumber5.getText().toString().trim();
		        			}else item.water_meter_number5="-1";
		        		}
		        		if(activity.chkSECUnknown.isChecked()){
		        			item.sec_db_number="NA";
		        			item.sec_db_number2="NA";
		        			item.sec_db_number3="NA";
		        			item.sec_db_number4="NA";
		        			item.sec_db_number5="NA";
		        		} else if(activity.chkSECKnown.isChecked()){
		        			item.sec_db_number=activity.txtSEC.getText().toString().trim();
		        			if(activity.txtSEC2.length() > 0){
		        				item.sec_db_number2=activity.txtSEC2.getText().toString().trim();
		        			}else item.sec_db_number2="NA";
		        			
		        			if(activity.txtSEC3.length() > 0){
		        				item.sec_db_number3=activity.txtSEC3.getText().toString().trim();
		        			}else item.sec_db_number3="NA";
		        			
		        			if(activity.txtSEC4.length() > 0){
		        				item.sec_db_number4=activity.txtSEC4.getText().toString().trim();
		        			}else item.sec_db_number4="NA";
		        			
		        			if(activity.txtSEC5.length() > 0){
		        				item.sec_db_number5=activity.txtSEC5.getText().toString().trim();
		        			}else item.sec_db_number5="NA";
		        			
		        		}
		            	Log.w("Snimam wmn:","WMN:" +item.water_meter_number + " --WMSTATUS:" +item.water_meter_status + " --hcn:"+item.house_connection_number+" --district:"+item.district);
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
            } else {
            	//check scenario for duplicate hcn and wmn
            	if(checkResponse.equalsIgnoreCase("wmn_busy_hcn_ok"))
            		response_str="ERROR:" + this.activity.getString(R.string.error_wmn_duplicate);
            	else if(checkResponse.equalsIgnoreCase("wmn_ok_hcn_busy"))
            		response_str="ERROR:"+this.activity.getString(R.string.error_hcn_duplicate);
            	else if(checkResponse.equalsIgnoreCase("wmn_busy_hcn_busy"))
            		response_str="ERROR:" + this.activity.getString(R.string.error_wmn_duplicate) + this.activity.getString(R.string.error_hcn_duplicate);
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
		    
		    
		    if(hasLoadedRejected==true) {
		    	Intent intent = new Intent();
		    	Intent ci=getIntent();
		    	intent.putExtra("rejectedId", ci.getIntExtra("rejectedId", 0));
		    	setResult(RESULT_OK, intent);
		    	finish();
		    	return;
		    }
    		
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

    	private ActivityPremise activity=null;
        private String response_str=null;

        SearchPremise(ActivityPremise activity) {
            attach(activity);
        }
        void detach() {
        	activity=null;
        }
          
        void attach(ActivityPremise activity) {
        	this.activity=activity;
        }

        @Override
        protected Void doInBackground(Void... arg) {
        	
            try {
            	response_str=null;
            	//search premise item
            	String urlString = activity.getResources().getString(R.string.url_search_premise);
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
                if(activity.rejectedHcn!=null){
                	hcn=activity.rejectedHcn;
                }else {
	                hcn=this.activity.txtSearchHcn.getText().toString().trim();
					wmn=this.activity.txtSearchWaterMeterNumber.getText().toString().trim();
                }
                
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
    		    .setTitle(getResources().getString(R.string.premise_not_found))
    		    .setMessage(getResources().getString(R.string.premise_not_found_big))
    		    .setPositiveButton(getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
    		        public void onClick(DialogInterface dialog, int which) { 
    		            dialog.dismiss();
    		        }
    		     })
    		     .show();
    			toggleForm(true);
    			chkHcnKnown.setChecked(true);
    			chkWmnKnown.setChecked(true);
    			chkSECKnown.setChecked(true);
    			txtWaterMeterNumber.requestFocus();
        	} else if(response_str.contains("no_permission")) {
        		Log.w("premise SEARCH","NO permission "+response_str);
    			
    			clearAll();
    			didSearch=true;
    			new AlertDialog.Builder(this)
    		    .setTitle(getResources().getString(R.string.errors))
    		    .setMessage(getResources().getString(R.string.error_no_permission))
    		    .setPositiveButton(getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
    		        public void onClick(DialogInterface dialog, int which) { 
    		            dialog.dismiss();
    		        }
    		     })
    		     .show();
    			toggleForm(true);
    			txtWaterMeterNumber.requestFocus();
        	} else if(response_str.contains("waiting_validation")) {
        		Log.w("premise SEARCH","NOT validated "+response_str);
    			
    			clearAll();
    			didSearch=true;
    			new AlertDialog.Builder(this)
    		    .setTitle(getResources().getString(R.string.premise_not_validated))
    		    .setMessage(getResources().getString(R.string.premise_not_validated_big))
    		    .setPositiveButton(getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
    		        public void onClick(DialogInterface dialog, int which) { 
    		            dialog.dismiss();
    		        }
    		     })
    		     .show();
    			toggleForm(false);
    			txtWaterMeterNumber.requestFocus();
    		} else {
        		Gson gson=new Gson();
        		JSONPremiseItem premise;
        		try{
        			premise=gson.fromJson(response_str, JSONPremiseItem.class);
        			Log.w("PREMISE SEARCH","parsiran PREMISE!!!");
        			//fill the fields with values :)
        			
        			clearAll();
        			didSearch=true;
        			fillFieldsWithData(premise);
        			toggleForm(true);
        			txtWaterMeterNumber.requestFocus();
        			txtSearchHcn.setText("");
        			txtSearchWaterMeterNumber.setText("");
        			
        			//ako je rejected 
        			if(rejectedHcn != null && rejectedHcn.length()>0) {
        				hasLoadedRejected=true;
        				btnSearch.setEnabled(false);
        				btnEnable.setEnabled(false);
        				txtHouseConnectionNumber.setEnabled(false);
        				chkHcnKnown.setEnabled(false);
        				chkHcnUnknown.setEnabled(false);
        				txtPremiseId.requestFocus();
        			}
        		} catch (Exception e) {
					Log.w("GRE��KA","NE MOGU PARSIRATI NA��ENI PREMISE!!!"+response_str,e);
				}
        	}
        }
    	
    	searchTask=null;
    }
    
    
    
    
    /** RESERRVE HCN PREMISE ASYNC TASK **/
    static class ReserveNewHcn extends AsyncTask<Void, Integer, Void> {

    	private ActivityPremise activity=null;
        private String response_str=null;

        ReserveNewHcn(ActivityPremise activity) {
            attach(activity);
        }
        void detach() {
        	activity=null;
        }
          
        void attach(ActivityPremise activity) {
        	this.activity=activity;
        }

        @Override
        protected Void doInBackground(Void... arg) {
        	
            try {
            	response_str=null;
            	//search premise item
            	String urlString = activity.getResources().getString(R.string.url_reserve_hcn);
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
                
                String district=String.valueOf(((JSONSpinnerItem)activity.lstDistricts.get(activity.spnDistrict.getSelectedItemPosition())).id);
                
                if(district.length()>0){
                	reqEntity.addPart("district", new StringBody(district));
                }
                
                post.setEntity(reqEntity);
                HttpResponse response = client.execute(post);
                resEntity = response.getEntity();
                response_str = EntityUtils.toString(resEntity);
                
            } catch (Exception e) {
                Log.w("HCN RESERVETAION CONN ERROR","ERROR",e);
                response_str="ERROR: " + activity.getResources().getString(R.string.require_connection);
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void result) {
            if(activity!=null){
	        	try {
	                activity.markReservationAsDone();
	                
	            } catch(Exception e) {
	            }
            }

        }

    }
    
    private void markReservationAsDone(){
    	if(dialog!=null)
    		dialog.dismiss();
    	if(getHcnTask==null) return;
    	String response_str=getHcnTask.response_str;
    	if(response_str != null) {
        	if(response_str.contains("ERROR")) {
        		Log.w("HCN RESERVATION SEARCH ERROR","PREMISE ERROR:"+response_str);
        		Toast.makeText(this, response_str, Toast.LENGTH_LONG).show();
        	} else {
        		Gson gson=new Gson();
        		String reservedHcn=null;
        		try{
        			reservedHcn=gson.fromJson(response_str, String.class);
        			Log.w("HCN RESERVATION","parsiran HCN!!!");
        			if(reservedHcn.length()==10){
        				txtHouseConnectionNumber.setText(reservedHcn);
        				hcnAutoGenerated=true;
        				Toast.makeText(this, getResources().getString(R.string.hcn_generated), Toast.LENGTH_LONG).show();
        			}
        		} catch (Exception e) {
					Log.w("GRE��KA","NE MOGU PARSIRATI GENERISANI HCN!!!"+response_str,e);
				}
        	}
        }
    	
    	getHcnTask=null;
    }
    
    private void fillFieldsWithData(JSONPremiseItem premise){
    	
    	if(premise.district!=null){
    		JSONSpinnerItem tmp=new JSONSpinnerItem();
    		tmp.id=Integer.valueOf(premise.district);
    		spnDistrict.setSelection(adptDistrict.getPosition(tmp));
    	}
    	if(premise.house_connection_number!=null){
    		if(premise.house_connection_number.length()>0){
    			txtHouseConnectionNumber.setText(premise.house_connection_number);
    			chkHcnKnown.setChecked(true);
    		} else {
    			chkHcnUnknown.setChecked(true);
    		}
    	} else {
    		chkHcnUnknown.setChecked(true);
    	}
    	
    	//water meter number
       	if(premise.water_meter_status!=null){
    		JSONSpinnerItem tmp3=new JSONSpinnerItem();
    		tmp3.id=Integer.valueOf(premise.water_meter_status);
    		spnWmStatus.setSelection(adptWmStatus.getPosition(tmp3));
    	}
    	if(premise.water_meter_number!=null){
    		if(premise.water_meter_number.length()>0){
    			txtWaterMeterNumber.setText(premise.water_meter_number);
    			chkWmnKnown.setChecked(true);
    		} else {
    			chkWmnUnknown.setChecked(true);
    		}
    	} else {
    		chkWmnUnknown.setChecked(true);
    	}

    	
    	//SEC number
    	if(premise.sec_db_number!=null){
    		
    		if(premise.sec_db_number.length()>0 && !premise.sec_db_number.equalsIgnoreCase("NA")){
    			chkSECKnown.setChecked(true);
    			txtSEC.setText(premise.sec_db_number);
    		} else {
    			chkSECUnknown.setChecked(true);
    			txtSEC.setText("");
    		}
    	} else {
    		txtSEC.setText("");
    		chkSECUnknown.setChecked(true);
    	}

    	
    	if(premise.premise_type_id!=null){
    		JSONSpinnerItem tmp=new JSONSpinnerItem();
    		tmp.id=Integer.valueOf(premise.premise_type_id);
    		spnPremiseType.setSelection(adptPremiseType.getPosition(tmp));
    	}
    	
    	if(premise.premise_id!=null)
    		txtPremiseId.setText(premise.premise_id);
    	
    	if(premise.stc_db_number!=null)
    		txtSTC.setText(premise.stc_db_number);
    	
    	if(premise.sec_db_number!=null)
    		txtSEC.setText(premise.sec_db_number);
    	
    	if(premise.total_electrical_meters!=null)
    		txtTotalElectricalMeters.setText(premise.total_electrical_meters);
    	
    	if(premise.floor_count!=null)
    		txtFloorCount.setText(premise.floor_count);

    	if(premise.premise_name!=null)
    		txtPremiseName.setText(premise.premise_name);
    	    	
    	if(premise.use_of_building!=null)
    		txtUseOfBuilding.setText(premise.use_of_building);
    	
    	new AlertDialog.Builder(this)
	    .setTitle(getResources().getString(R.string.premise_found))
	    .setMessage(getResources().getString(R.string.premise_found_big))
	    .setPositiveButton(getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	            dialog.dismiss();
	        }
	     })
	     .show();
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

		
		outState.putBoolean("canDeleteImgHouse1", canDeleteImgHouse1);
		outState.putBoolean("canDeleteImgHouse2", canDeleteImgHouse2);
		outState.putBoolean("canDeleteImgHouse3", canDeleteImgHouse3);
		outState.putBoolean("canDeleteImgConnection", canDeleteImgConnection);
		outState.putBoolean("canDeleteImgSTC", canDeleteImgSTC);
		outState.putBoolean("canDeleteImgSEC", canDeleteImgSEC);
		
		
		//save states of text fields, spinners and checkboxes
		outState.putString("txtHouseConnectionNumber", txtHouseConnectionNumber.getText().toString());
		outState.putString("txtPremiseId", txtPremiseId.getText().toString());
		outState.putString("txtSTC", txtSTC.getText().toString());
		outState.putString("txtSEC", txtSEC.getText().toString());
		outState.putString("txtTotalElectricalMeters", txtTotalElectricalMeters.getText().toString());
		outState.putString("txtFloorCount", txtFloorCount.getText().toString());
		outState.putString("txtPremiseName", txtPremiseName.getText().toString());
		outState.putString("txtUseOfBuilding", txtUseOfBuilding.getText().toString());
		outState.putString("txtWaterMeterNumber", txtWaterMeterNumber.getText().toString());
		
		
		outState.putInt("spnPremiseType", spnPremiseType.getSelectedItemPosition());
		
		outState.putBoolean("gettingLocation", gettingLocation);
		
		//new fields for search and known/unknown location
		outState.putInt("spnDistrict", spnDistrict.getSelectedItemPosition());
		outState.putString("txtSearchHcn", txtSearchHcn.getText().toString());
		
		outState.putString("txtSearchWaterMeterNumber", txtSearchWaterMeterNumber.getText().toString());
		outState.putBoolean("chkHcnKnown", chkHcnKnown.isChecked());
		outState.putBoolean("chkHcnUnknown", chkHcnUnknown.isChecked());
		outState.putBoolean("chkSECKnown", chkSECKnown.isChecked());
		outState.putBoolean("chkSECUnknown", chkSECUnknown.isChecked());
		outState.putBoolean("didSearch", didSearch);
		
		//new fields for water meter known/unknown
		outState.putInt("spnWmStatus", spnWmStatus.getSelectedItemPosition());
		outState.putBoolean("chkWmnKnown", chkWmnKnown.isChecked());
		outState.putBoolean("chkWmnUnknown", chkWmnUnknown.isChecked());
		
		outState.putBoolean("hasLoadedRejected", hasLoadedRejected);
		outState.putBoolean("hcnAutoGenerated", hcnAutoGenerated);
		

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
			
			//now load edit text, spinners and check boxes from saved state
			txtHouseConnectionNumber.setText(savedInstanceState.getString("txtHouseConnectionNumber",""));
			txtPremiseId.setText(savedInstanceState.getString("txtPremiseId",""));
			txtSTC.setText(savedInstanceState.getString("txtSTC",""));
			txtSEC.setText(savedInstanceState.getString("txtSEC",""));
			txtFloorCount.setText(savedInstanceState.getString("txtFloorCount",""));
			txtPremiseName.setText(savedInstanceState.getString("txtPremiseName",""));
			txtTotalElectricalMeters.setText(savedInstanceState.getString("txtTotalElectricalMeters",""));
			txtUseOfBuilding.setText(savedInstanceState.getString("txtUseOfBuilding",""));
			txtWaterMeterNumber.setText(savedInstanceState.getString("txtWaterMeterNumber",""));

			spnPremiseType.setSelection(savedInstanceState.getInt("spnPremiseType",0));
			
			gettingLocation=savedInstanceState.getBoolean("gettingLocation");
			
			mCurrentPhotoPath=savedInstanceState.getString("mCurrentPhotoPath");
			if(mCurrentPhotoPath!=null)
				f=new File(mCurrentPhotoPath);
			
			//new fields
			spnDistrict.setSelection(savedInstanceState.getInt("spnDistrict",0));
			
			txtSearchWaterMeterNumber.setText(savedInstanceState.getString("txtSearchWaterMeterNumber",""));
			chkHcnKnown.setChecked(savedInstanceState.getBoolean("chkHcnKnown",false));
			chkHcnUnknown.setChecked(savedInstanceState.getBoolean("chkHcnUnknown",false));
			chkSECKnown.setChecked(savedInstanceState.getBoolean("chkSECKnown",false));
			chkSECUnknown.setChecked(savedInstanceState.getBoolean("chkSECUnknown",false));
			didSearch=savedInstanceState.getBoolean("didSearch",false);
			
			//new fields for water meter known/unknown
			spnWmStatus.setSelection(savedInstanceState.getInt("spnWmStatus",0));
			chkWmnKnown.setChecked(savedInstanceState.getBoolean("chkWmnKnown",false));
			chkWmnUnknown.setChecked(savedInstanceState.getBoolean("chkWmnUnknown",false));
			
			hasLoadedRejected=savedInstanceState.getBoolean("hasLoadedRejected");
			hcnAutoGenerated=savedInstanceState.getBoolean("hcnAutoGenerated",false);
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
		
		//provjeri rejected
		//provjeri da li je proslije��en rejected premise id
		Bundle b=this.getIntent().getExtras();
		int id=0;
		if(b != null){
			id = b.getInt("rejectedId", 0);
			rejectedHcn=b.getString("rejectedHcn", null);
			Log.w("BUNDLE","REJECTED POSLAN");
		} else Log.w("BUNDLE","PRAZAN");
		if(id>0 && rejectedHcn != null && rejectedHcn.length()>0 && hasLoadedRejected==false && searchTask==null){
			//startaj search taks proslije��en je HCN od rejected-a
			dialog = new ProgressDialog(this);
	        dialog.setMessage(getResources().getString(R.string.sending));
	        dialog.setIndeterminate(false);
	        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	        dialog.setProgress(0);
	        dialog.show();
			searchTask=new SearchPremise(this);
			searchTask.execute();
			btnSearch.setEnabled(false);
			btnEnable.setEnabled(false);
		} else if(hasLoadedRejected == true) {
			txtPremiseId.requestFocus();
			btnSearch.setEnabled(false);
			btnEnable.setEnabled(false);
			txtHouseConnectionNumber.setEnabled(false);
			chkHcnKnown.setEnabled(false);
			chkHcnUnknown.setEnabled(false);
		}
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
	    if(getHcnTask!=null){
	    	getHcnTask.detach();
	    	return(getHcnTask);
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
    	
    	spnPremiseType.setSelection(0);
    	
    	
    	//my location
    	myLat=null;
    	myLng=null;
    	myAlt=null;
    	premiseLat=null;
    	premiseLng=null;
    	premiseAlt=null;
    	txtPremiseLat.setText("");
    	txtPremiseLng.setText("");
    	
    	
    	//define text fields
    	txtHouseConnectionNumber.setText("");
    	txtHouseConnectionNumber.setError(null);
    	txtPremiseId.setText("");
    	txtPremiseId.setError(null);
    	txtSTC.setText("");
    	txtSTC.setError(null);
    	txtSEC.setText("");
    	txtSEC.setError(null);
    	txtTotalElectricalMeters.setText("");
    	txtTotalElectricalMeters.setError(null);
    	txtFloorCount.setText("");
    	txtFloorCount.setError(null);
    	txtPremiseName.setText("");
    	txtPremiseName.setError(null);
    	txtUseOfBuilding.setText("");
    	txtUseOfBuilding.setError(null);
    	txtWaterMeterNumber.setText("");
    	txtWaterMeterNumber.setError(null);
    	
    	txtSearchHcn.setText("");
    	hcnAutoGenerated=false;
    	
    	txtSearchWaterMeterNumber.setText("");
    	didSearch=false;
    	spnDistrict.setSelection(0);
    	spnWmStatus.setSelection(0);
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
  						premiseLat=String.valueOf(loc.getLatitude());
  						premiseLng=String.valueOf(loc.getLongitude());
  						premiseAlt=String.valueOf(loc.getAltitude());
  						ActivityPremise.this.setGpsLocation();
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
  		txtWaterMeterNumber.setEnabled(enable);
  		txtHouseConnectionNumber.setEnabled(enable);
  		chkHcnKnown.setEnabled(enable);
  		chkSECKnown.setEnabled(enable);
  		chkSECUnknown.setEnabled(enable);
  		chkHcnUnknown.setEnabled(enable);
  		chkWmnKnown.setEnabled(enable);
  		chkWmnUnknown.setEnabled(enable);
  		spnWmStatus.setEnabled(enable);
  		spnDistrict.setEnabled(enable);
  		spnPremiseType.setEnabled(enable);
  		txtPremiseId.setEnabled(enable);
  		txtSTC.setEnabled(enable);
  		txtSEC.setEnabled(enable);
  		txtTotalElectricalMeters.setEnabled(enable);
  		txtFloorCount.setEnabled(enable);
  		txtPremiseName.setEnabled(enable);
  		txtUseOfBuilding.setEnabled(enable);
  		btnGetLocation.setEnabled(enable);
  		btnGetLocationNoMap.setEnabled(enable);
  		btnSubmit.setEnabled(enable);
  	}
  	
  	private void checkHcn(){
  		LinearLayout llDistrict=(LinearLayout)findViewById(R.id.llDistrict);
  		if(chkHcnKnown.isChecked()){
			chkHcnUnknown.setChecked(false);
			txtHouseConnectionNumber.setVisibility(View.VISIBLE);
			txtHouseConnectionNumber.setEnabled(true);
			if(llDistrict!=null) llDistrict.setVisibility(View.GONE);
		} else if(chkHcnUnknown.isChecked()) {
			chkHcnKnown.setChecked(false);
			txtHouseConnectionNumber.setVisibility(View.VISIBLE);
			txtHouseConnectionNumber.setEnabled(false);
			if(llDistrict!=null) llDistrict.setVisibility(View.VISIBLE);
		} else {
			if(llDistrict!=null) llDistrict.setVisibility(View.GONE);
			txtHouseConnectionNumber.setVisibility(View.GONE);
		}
  	}

  	private void checkWmn(){
  		LinearLayout llWmStatus=(LinearLayout)findViewById(R.id.llWmStatus);
  		if(chkWmnKnown.isChecked()){
  			chkWmnUnknown.setChecked(false);
  			txtWaterMeterNumber.setVisibility(View.VISIBLE);
			if(llWmStatus!=null) llWmStatus.setVisibility(View.GONE);
		} else if(chkWmnUnknown.isChecked()) {
			chkWmnKnown.setChecked(false);
			txtWaterMeterNumber.setVisibility(View.GONE);
			if(llWmStatus!=null) llWmStatus.setVisibility(View.VISIBLE);
		} else {
			if(llWmStatus!=null) llWmStatus.setVisibility(View.GONE);
			txtWaterMeterNumber.setVisibility(View.GONE);
		}
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
	
	
	private void reserveNewHcn(){
		Boolean error=false;
		if(chkHcnUnknown.isChecked())
		{
			if(spnDistrict.getSelectedItemPosition()==0) {
				new AlertDialog.Builder(ActivityPremise.this)
			    .setTitle(getResources().getString(R.string.errors))
			    .setMessage(getResources().getString(R.string.hcn_unknown_error))
			    .setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int which) { 
			            dialog.dismiss();
			        }
			     })
			     .show();
				
				error=true;
			}
		}
		
		if(!error){
			dialog = new ProgressDialog(this);
	        dialog.setMessage(getResources().getString(R.string.sending));
	        dialog.setIndeterminate(false);
	        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	        dialog.setProgress(0);
	        dialog.show();
			getHcnTask=new ReserveNewHcn(this);
			getHcnTask.execute();
		}
	}
  	
  	
	
}
