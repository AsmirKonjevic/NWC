package ba.leftor.nwc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
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
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityWaterMeterStatus extends Activity {

	private SubmitWaterMeterForm task = null;
	private SearchPremise searchTask = null;
	private checkData checkTask=null;
	private ProgressDialog dialog;
	   
    /** FOR currentProgress & totalSize for setting progress while uploading*/
	private int currentProgress=0;
	private long totalSize;

	// define variables needed for taking photos and selecting photo from
	// gallery
	static final int REQUEST_TAKE_WATER_METER_PHOTO = 1;
	static final int REQUEST_SELECT_WATER_METER = 92;
	private String mCurrentPhotoPath;
	private AlbumStorageDirFactory mAlbumStorageDirFactory = null;
	File f = null;

	private ImageView imgWaterMeter;
	private String pathImgWaterMeter;
	private Boolean canDeleteImgWaterMeter = false;

	private ArrayList<JSONPictureItem> lstPictures;
	ArrayList<JSONSpinnerItem> lstDistricts;
	private Spinner spnDistrict;
	private LinearLayout llPictures = null;
	static final int REQUEST_TAKE_PHOTO = 300;
	static final int REQUEST_SELECT_PHOTO = 301;
	private String currentDynamicTag = null;
	private int dynamicImageIndexCounter = 0;
	private boolean manualEnabled=false;


	private String myLat;
	private String myLng;
	private String myAlt;
	private String hcn = null;
	

	// define text fields
	private EditText txtWaterMeterReading;

	// my location
	private MyLocation myLocation;
	private Boolean gettingLocation = false; // to continue finding location on
												// orientation change

	// HOUSE CONNECTION KONWON-UNKNOWN, DISTRICT AND SEARCH FIELDS
	private Boolean didSearch = false; // USER MUST FIRST SEARCH BEFORE GETTING
										// PERMISSION TO FILL FORM
	private EditText txtSearchHcn;
	private EditText txtSearchWaterMeterNumber;
	private EditText txtSearchSec;
	private EditText txtPremiseName;
	private EditText txtHouseConnectionNumber;
	private EditText txtSecDbNumber;
	private EditText txtWaterMeterNumber;
	private EditText txtAreaName;
	private Button btnSearch;
	private Button btnEnableForm;

	private JSONRejectedPremiseItem loadedPremise = null;

	private Button btnSubmit;
	private Button btnPhoto1;
	private Button btnPhoto2;
	private Button btnPhoto3;

	// text views and their layouts
	private TextView txtFullName;
	private TextView txtDistrict;
	private TextView txtHcn;
	private TextView txtType;
	
	private TextView txtWmn;
	private LinearLayout lnHouseConnectionNumber;
	private LinearLayout lnSecDbNumber;
	private LinearLayout lnWaterMeterNumber;
	private LinearLayout lnFullName;
	private LinearLayout lnDistrict;
	private LinearLayout tvPremiseName;
	private LinearLayout lnHcn;
	private LinearLayout lnPremiseName;
	private LinearLayout lnWmn;
	private LinearLayout lnType;
	ArrayAdapter<JSONSpinnerItem> adptDistrict;
	public int district_id;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.meter_status_form);

		// display user name on top
		TextView txtWelcome = (TextView) findViewById(R.id.txtWelcome);
		JSONUSerItem user = GlobalFunctionsAndConstants.getUser(this);
		if (user.fullName.length() > 1)
			txtWelcome.setText(this.getResources().getString(R.string.welcome)
					+ user.fullName);
		else
			txtWelcome.setText(this.getResources().getString(R.string.welcome)
					+ user.username);

		// get album storage dir
		mAlbumStorageDirFactory = new BaseAlbumDirFactory();

		// dynamic image layout
		llPictures = (LinearLayout) findViewById(R.id.llPictures);
		lstPictures = new ArrayList<JSONPictureItem>();

		// find images in layout and register for context menu
		imgWaterMeter = (ImageView) this.findViewById(R.id.imgWaterMeter);
		registerForContextMenu(imgWaterMeter);
		imgWaterMeter.setOnClickListener(photoListener);

		// find edit texts
		txtSearchHcn = (EditText) findViewById(R.id.txtSearchHcn);
		txtHouseConnectionNumber = (EditText) findViewById(R.id.txtHouseConnectionNumber);
		txtSecDbNumber = (EditText) findViewById(R.id.txtSecDbNumber);
		txtWaterMeterNumber = (EditText) findViewById(R.id.txtWaterMeterNumber);
		txtAreaName = (EditText) findViewById(R.id.txtAreaName);
		txtSearchWaterMeterNumber = (EditText) findViewById(R.id.txtSearchWaterMeterNumber);
		txtSearchSec = (EditText) findViewById(R.id.txtSearchSec);
		txtWaterMeterReading = (EditText) findViewById(R.id.txtWaterMeterReading);

		// text views
		txtFullName = (TextView) findViewById(R.id.txtFullName);
		txtDistrict = (TextView) findViewById(R.id.txtDistrict);
		txtHcn = (TextView) findViewById(R.id.txtHcn);
		txtType = (TextView) findViewById(R.id.txtType);
		txtWmn = (TextView) findViewById(R.id.txtWmn);
		txtPremiseName=(EditText)findViewById(R.id.txtPremiseName);
		lnFullName = (LinearLayout) findViewById(R.id.lnFullName);
		lnDistrict = (LinearLayout) findViewById(R.id.lnDistrict);
		lnHcn = (LinearLayout) findViewById(R.id.lnHcn);
		lnWmn = (LinearLayout) findViewById(R.id.lnWmn);
		lnType = (LinearLayout) findViewById(R.id.lnType);
		lnHouseConnectionNumber= (LinearLayout) findViewById(R.id.lnHouseConnectionNumber);
		lnSecDbNumber= (LinearLayout) findViewById(R.id.lnSecDbNumber);
		lnWaterMeterNumber= (LinearLayout) findViewById(R.id.lnWaterMeterNumber);
		lnPremiseName= (LinearLayout) findViewById(R.id.lnPremiseName);
		spnDistrict=(Spinner)findViewById(R.id.spnDistrict);
	
		// dugmad za slike
		btnPhoto1 = (Button) findViewById(R.id.btnPhoto1);
		btnPhoto2 = (Button) findViewById(R.id.btnPhoto2);
		btnPhoto3 = (Button) findViewById(R.id.btnPhoto3);

		//load premise types, districts and water meter statuses from db
		MyDataSource dataSource=new MyDataSource(this);
		dataSource.open();
		lstDistricts=dataSource.getCatalogItems("district");
		//select district item
		JSONSpinnerItem noDistrict=new JSONSpinnerItem();
		noDistrict.id=-1;
		noDistrict.text="Please select";
		lstDistricts.add(0, noDistrict);
		dataSource.close();
		//district adapter
		adptDistrict = new ArrayAdapter(this,android.R.layout.simple_spinner_item, lstDistricts);
		adptDistrict.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnDistrict.setAdapter(adptDistrict);
		
		btnPhoto1.setOnClickListener(new OnClickListener() {

			
			public void onClick(View arg0) {
				if (loadedPremise != null) {
					if (loadedPremise.photo_house != null) {
						String tmp = getString(R.string.url_home) + "/upload/"
								+ loadedPremise.user_id + "/"
								+ loadedPremise.photo_house;
						Intent i = new Intent(ActivityWaterMeterStatus.this,
								ActivityShowPhoto.class);
						i.putExtra("photo", tmp);
						startActivity(i);
					}
				}

			}
		});
		btnPhoto2.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				if (loadedPremise != null) {
					if (loadedPremise.photo_house_2 != null) {
						String tmp = getString(R.string.url_home) + "/upload/"
								+ loadedPremise.user_id + "/"
								+ loadedPremise.photo_house_2;
						Intent i = new Intent(ActivityWaterMeterStatus.this,
								ActivityShowPhoto.class);
						i.putExtra("photo", tmp);
						startActivity(i);
					}
				}

			}
		});
		btnPhoto3.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				if (loadedPremise != null) {
					if (loadedPremise.photo_house_3 != null) {
						String tmp = getString(R.string.url_home) + "/upload/"
								+ loadedPremise.user_id + "/"
								+ loadedPremise.photo_house_3;
						Intent i = new Intent(ActivityWaterMeterStatus.this,
								ActivityShowPhoto.class);
						i.putExtra("photo", tmp);
						startActivity(i);
					}
				}

			}
		});

		// submit form button
		btnSubmit = (Button) findViewById(R.id.btnSubmit);
		btnSubmit.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				try {
					validateAndSubmit();
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		btnSearch = (Button) findViewById(R.id.btnSearch);
		btnSearch.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				String hcn = txtSearchHcn.getText().toString().trim();
				String wmn = txtSearchWaterMeterNumber.getText().toString()
						.trim();
				String sec = txtSearchSec.getText().toString().trim();


				Boolean error = false;
				String errText = "";
				if (hcn.equals("") && wmn.equals("") && sec.equals("")) {
					error = true;
					errText = getResources().getString(
							R.string.error_enter_search);
					txtSearchWaterMeterNumber
							.setError("You must first search for house connection number or water meter number!");
				} else {
					txtSearchHcn.setError(null);
					txtSearchWaterMeterNumber.setError(null);
				}

				if (error) {
					new AlertDialog.Builder(ActivityWaterMeterStatus.this)
							.setTitle("ERROR")
							.setMessage(errText)
							.setPositiveButton("Dismiss",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int which) {
											dialog.dismiss();
										}
									}).show();
				} else {
					searchPremise();
				}

			}
		});
		// enable form without search
		btnEnableForm = (Button) findViewById(R.id.btnEnableForm);
		btnEnableForm.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				
					clearAll();
					enableForm();
					toggleForm(true);

			}
		});

		// rotation aware task
		try {
			task = (SubmitWaterMeterForm) getLastNonConfigurationInstance();
		} catch (Exception e) {
		}
		if (task != null) {
			dialog = new ProgressDialog(this);
			dialog.setMessage(getResources().getString(R.string.sending));
			dialog.setIndeterminate(false);
			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			dialog.setProgress(0);
			dialog.show();
			task.attach(this);

			updateProgress(task.getProgress());

			if (task.getProgress() >= 100) {
				markAsDone();
			}
		}

		// rotation aware task for searching
		try {
			searchTask = (SearchPremise) getLastNonConfigurationInstance();
		} catch (Exception e) {
		}
		if (searchTask != null) {
			dialog = new ProgressDialog(this);
			dialog.setMessage(getResources().getString(R.string.sending));
			dialog.setIndeterminate(false);
			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			dialog.setProgress(0);
			dialog.show();
			searchTask.attach(this);

			if (searchTask.response_str != null) {
				markSearchAsDone();
			}
		}

	}

	/**
	 * Kreira novi layout u llPictures u kojem se nalazi prazan img i postavlja
	 * listener na njega za context menu da se moze uzeti slika :)
	 */
	private void createNewPictureBox(JSONPictureItem item) {
		if (item == null) {
			item = new JSONPictureItem();
			lstPictures.add(item);
			item.setId(dynamicImageIndexCounter);
			item.setTag("img" + item.getId());
			dynamicImageIndexCounter++;
		}

		ImageView tmpImg = (ImageView) this.getLayoutInflater().inflate(
				R.layout.picture_item, null);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				100, 100);
		layoutParams.setMargins(0, 0, 20, 0);
		tmpImg.setLayoutParams(layoutParams);
		tmpImg.setTag(item.getTag());
		llPictures.addView(tmpImg);
		Log.w("DYNAMIC CREATE", "KREIRAM SLIKU");

		registerForContextMenu(tmpImg);
		tmpImg.setOnClickListener(photoListener);
		// ako je restore instance state postavi sliku
		if (item.path != null) {
			Log.w("DYNAMIC CREATE", "Postavljam putanju");
			setPic(tmpImg, item.path);
		}
	}

	// validate and submit form
	private void validateAndSubmit() throws ParseException, IOException {
		Boolean error = false;
		// hcn required
		if(manualEnabled){
		if (txtHouseConnectionNumber.getText().toString().length() < 1) {
			txtHouseConnectionNumber.setError(getResources().getString(
					R.string.field_required));
			if (!error)
				txtHouseConnectionNumber.requestFocus();
			error = true;
			return;
		} else
			txtHouseConnectionNumber.setError(null);
		}
		// wmr required
		if (txtWaterMeterReading.getText().toString().length() < 1) {
			txtWaterMeterReading.setError(getResources().getString(
					R.string.field_required));
			if (!error)
				txtWaterMeterReading.requestFocus();
			error = true;
			return;
		} else
			txtWaterMeterReading.setError(null);

		try {
			String readingString = txtWaterMeterReading.getText().toString();
			Float reading = Float.parseFloat(txtWaterMeterReading.getText()
					.toString());
			if (reading < 0.1f && readingString.isEmpty())
				error = true;
		} catch (Exception e) {
			error = true;
		}
		if (error)
			txtWaterMeterReading.setError(getResources().getString(
					R.string.field_required));
		else
			txtWaterMeterReading.setError(null);

		if (pathImgWaterMeter == null) {
			new AlertDialog.Builder(ActivityWaterMeterStatus.this)
					.setTitle(getResources().getString(R.string.errors))
					.setMessage(
							getResources().getString(
									R.string.error_water_meter_picture))
					.setPositiveButton("Dismiss",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							}).show();
			error = true;
		}

		if (!error) {
			checkData();
		}
	}

	static class SubmitWaterMeterForm extends AsyncTask<Void, Integer, Void> {

		private ActivityWaterMeterStatus activity = null;
		private int percent = 0;
		private long totalSize;
		private int currentProgress;
		private String response_str = null;

		SubmitWaterMeterForm(ActivityWaterMeterStatus activity) {
			attach(activity);
		}

		void detach() {
			activity = null;
		}

		void attach(ActivityWaterMeterStatus activity) {
			this.activity = activity;
		}

		@Override
		protected Void doInBackground(Void... arg) {

			try {
				response_str = null;

				// add premise item to database
				JSONWaterReadingItem item = new JSONWaterReadingItem();
    			item.district_id=Integer.valueOf(((JSONSpinnerItem)activity.lstDistricts.get(activity.spnDistrict.getSelectedItemPosition())).id);

				item.gps_latitude_created = activity.myLat;
				item.gps_longitude_created = activity.myLng;
				item.gps_altitude_created = activity.myAlt;
				if (activity.manualEnabled!=true) {
					// regular submit
					item.manual_source = 0;
					if (activity.hcn != null) {
						item.house_connection_number = activity.hcn.trim();
					}
				} else {
					item.manual_source = 1;
					item.house_connection_number = activity.txtHouseConnectionNumber
							.getText().toString().trim();
				}
				item.sec_db_number = activity.txtSecDbNumber
						.getText().toString().trim();
				item.water_meter_number = activity.txtWaterMeterNumber
						.getText().toString().trim();
				item.area_name = activity.txtAreaName
						.getText().toString().trim();
				item.photo_water_meter = activity.pathImgWaterMeter;
				item.water_meter_reading = activity.txtWaterMeterReading
						.getText().toString().trim();
				item.premise_name = activity.txtPremiseName
						.getText().toString().trim();
				ArrayList<String> pictures = new ArrayList<String>();
				if (activity.lstPictures.size() > 0) {
					for (JSONPictureItem picItem : activity.lstPictures) {
						if (picItem.path != null)
							pictures.add(picItem.path);
					}
				}

				if (pictures.size() > 0) {
					Gson gson = new Gson();
					item.dynamic_photos = gson.toJson(pictures);
					Log.w("DYNAMIC PHOT COUNT", "DB NUM:" + pictures.size());
				} else {
					item.dynamic_photos = "";
				}

				MyDataSource dataSource = new MyDataSource(this.activity);
				dataSource.open();

				long insertStatus = dataSource
						.addWaterMeterReadingItemToDb(item);
				Log.w("INSART STATUS", "STATUS:" + insertStatus);
				dataSource.close();
				if (insertStatus != -1)
					response_str = "OK";
				else
					response_str = "ERROR:ERROR WHILE SAVING ITEM TO DB.PLEASE TRY AGAIN. IF this error continues please CONTACT SUPPROT AND NOTIFY ABOUT THIS ERROR.";
			} catch (Exception e) {
				Log.w("GRE��KA U SNIMANJU U BAZU", "ERR", e);
			}
			return null;

		}

		int getProgress() {
			return (percent);
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			percent = progress[0];
			if (activity != null)
				activity.updateProgress(percent);

		}

		@Override
		protected void onPostExecute(Void result) {
			if (activity != null) {
				try {
					activity.markAsDone();

				} catch (Exception e) {
					Log.w("GRE��KA U MARKIRANJU APLIKACIJE KAO DONE", "ERR", e);
				}
			}

		}

	}

	private void updateProgress(int percent) {
		dialog.setProgress(percent);
	}

	private void markAsDone() {
		if (dialog != null)
			dialog.dismiss();
		if (task.response_str != null && task.response_str.contains("ERROR")) {
			new AlertDialog.Builder(this)
					.setTitle(getResources().getString(R.string.errors))
					.setMessage(task.response_str.substring(6))
					.setPositiveButton("Dismiss",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							}).show();
			task.response_str = null;
			task = null;
		} else if (task.response_str != null
				&& task.response_str.contains("OK")) {
			clearAll();

			// set alarm for service !!!
			AlarmManager service = (AlarmManager) this
					.getSystemService(Context.ALARM_SERVICE);
			Intent i = new Intent(this, RecieverStartService.class);
			PendingIntent pending = PendingIntent.getBroadcast(this, 0, i,
					PendingIntent.FLAG_CANCEL_CURRENT);
			Calendar cal = Calendar.getInstance();
			service.cancel(pending);
			//
			// Fetch every 1 min
			// InexactRepeating allows Android to optimize the energy
			// consumption
			service.setRepeating(AlarmManager.RTC_WAKEUP,
					cal.getTimeInMillis(), 60 * 15 * 1000, pending);

			new AlertDialog.Builder(this)
					.setTitle(getResources().getString(R.string.success))
					.setMessage(
							getResources().getString(R.string.form_succ_saved))
					.setPositiveButton(getResources().getString(R.string.OK),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							}).show();
			task = null;

		}
	}

	/** SEARCH PREMISE ASYNC TASK **/
	static class SearchPremise extends AsyncTask<Void, Integer, Void> {

		private ActivityWaterMeterStatus activity = null;
		private String response_str = null;

		SearchPremise(ActivityWaterMeterStatus activity) {
			attach(activity);
		}

		void detach() {
			activity = null;
		}

		void attach(ActivityWaterMeterStatus activity) {
			this.activity = activity;
		}

		@Override
		protected Void doInBackground(Void... arg) {

			try {
				response_str = null;
				// search premise item
				String urlString = activity.getResources().getString(
						R.string.url_search_water_meter_premise);
				HttpClient client = new DefaultHttpClient();
				HttpEntity resEntity;
				HttpPost post = new HttpPost(urlString);
				CustomMultiPartEntity reqEntity = new CustomMultiPartEntity(
						new ProgressListener() {
							public void transferred(long num) {

							}
						});

				SharedPreferences prefs = activity.getSharedPreferences(
						GlobalFunctionsAndConstants.PREFS_NAME, 0);
				JSONUSerItem user = new JSONUSerItem();
				user.username = prefs.getString("serviceUsername", "");
				user.password = prefs.getString("servicePass", "");
				if (user.password.equals("")) {
					user.username = prefs.getString("username", "");
					user.password = prefs.getString("pass", "");
				}
				reqEntity.addPart("username", new StringBody(user.username));
				reqEntity.addPart("pass", new StringBody(user.password));

				// get sarch fields and add to req entity
				String hcn = "";
				String wmn = "";
				String sec = "";
				hcn = this.activity.txtSearchHcn.getText().toString().trim();
				wmn = this.activity.txtSearchWaterMeterNumber.getText()
						.toString().trim();
				sec = this.activity.txtSearchSec.getText().toString().trim();

				if (hcn.length() > 0) {
					reqEntity.addPart("house_connection_number",
							new StringBody(hcn));
				}
				if (wmn.length() > 0) {
					reqEntity
							.addPart("water_meter_number", new StringBody(wmn));
				}
				if (sec.length() > 0) {
					reqEntity.addPart("sec", new StringBody(sec));
				}

				post.setEntity(reqEntity);
				HttpResponse response = client.execute(post);
				resEntity = response.getEntity();
				response_str = EntityUtils.toString(resEntity);

			} catch (Exception e) {
				Log.w("PREMISE SARCH CONN ERROR", "ERROR", e);
				response_str = "ERROR: "
						+ activity.getResources().getString(
								R.string.require_connection);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (activity != null) {
				try {
					activity.markSearchAsDone();

				} catch (Exception e) {
				}
			}

		}

	}
	
	
	/** SEARCH PREMISE ASYNC TASK **/
	static class checkData extends AsyncTask<Void, Integer, Void> {

		private ActivityWaterMeterStatus activity = null;
		private String response_str = null;

		checkData(ActivityWaterMeterStatus activity) {
			attach(activity);
		}

		void detach() {
			activity = null;
		}

		void attach(ActivityWaterMeterStatus activity) {
			this.activity = activity;
		}

		@Override
		protected Void doInBackground(Void... arg) {

			try {
				response_str = null;
				// search premise item
				String urlString = activity.getResources().getString(
						R.string.url_check_water_meter_number);
				HttpClient client = new DefaultHttpClient();
				HttpEntity resEntity;
				HttpPost post = new HttpPost(urlString);
				CustomMultiPartEntity reqEntity = new CustomMultiPartEntity(
						new ProgressListener() {
							public void transferred(long num) {

							}
						});

				SharedPreferences prefs = activity.getSharedPreferences(
						GlobalFunctionsAndConstants.PREFS_NAME, 0);
				JSONUSerItem user = new JSONUSerItem();
				user.username = prefs.getString("serviceUsername", "");
				user.password = prefs.getString("servicePass", "");
				if (user.password.equals("")) {
					user.username = prefs.getString("username", "");
					user.password = prefs.getString("pass", "");
				}
				reqEntity.addPart("username", new StringBody(user.username));
				reqEntity.addPart("pass", new StringBody(user.password));

				// get sarch fields and add to req entity
				String hcn = "";
				String wmn = "";
				String sec = "";
				String manualhcn = "";
				String manualwmn = "";
				String manualsec = "";
				hcn = this.activity.txtSearchHcn.getText().toString().trim();
				wmn = this.activity.txtSearchWaterMeterNumber.getText()
						.toString().trim();
				sec = this.activity.txtSearchSec.getText().toString().trim();
				//manual columns
				
				manualhcn = this.activity.txtHouseConnectionNumber.getText().toString().trim();
				manualwmn = this.activity.txtWaterMeterNumber.getText()
						.toString().trim();
				manualsec = this.activity.txtSecDbNumber.getText().toString().trim();
	
				
				//hcn check
				if (hcn.length() > 0) {
					reqEntity.addPart("hcn",
							new StringBody(hcn));
				}else{
					reqEntity.addPart("hcn",
							new StringBody(manualhcn));
				}
				//wmn check
				if (wmn.length() > 0) {
					reqEntity
							.addPart("wmn", new StringBody(wmn));
				}else{
					reqEntity
					.addPart("wmn", new StringBody(manualwmn));	
				}
				//sec check
				if (sec.length() > 0) {
					reqEntity.addPart("sec", new StringBody(sec));
				}else{
					reqEntity.addPart("sec", new StringBody(manualsec));

				}

				post.setEntity(reqEntity);
				HttpResponse response = client.execute(post);
				resEntity = response.getEntity();
				response_str = EntityUtils.toString(resEntity);

			} catch (Exception e) {
				Log.w("PREMISE CHECK CONN ERROR", "ERROR", e);
				response_str = "ERROR: "
						+ activity.getResources().getString(
								R.string.require_connection);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (activity != null) {
				try {
					activity.checkDataPost();

				} catch (Exception e) {
				}
			}

		}

	}

	private void checkDataPost() {
		if (dialog != null)
			dialog.dismiss();
		if (checkTask == null)
			return;
		String response_str = checkTask.response_str;
		if (response_str != null) {
			if (response_str.contains("ERROR")) {
				///show alert
				new AlertDialog.Builder(this)
				.setTitle(getResources().getString(R.string.errors))
				.setMessage(
						getResources().getString(
								R.string.wmn_data_validation_failed))
				.setPositiveButton(
						getResources().getString(R.string.OK),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						}).show();
			} else {
				getGPSLocation();
			}
		}

		checkTask = null;
	}
	
	
	
	
	private void markSearchAsDone() {
		if (dialog != null)
			dialog.dismiss();
		if (searchTask == null)
			return;
		String response_str = searchTask.response_str;
		if (response_str != null) {
			if (response_str.contains("ERROR")) {
				Log.w("PREMISE SEARCH ERROR", "PREMISE ERROR:" + response_str);
				Toast.makeText(this, response_str, Toast.LENGTH_LONG).show();
			} else if (response_str.contains("NOT FOUND")) {
				new AlertDialog.Builder(this)
						.setTitle(getResources().getString(R.string.errors))
						.setMessage(
								getResources().getString(
										R.string.premise_not_found))
						.setPositiveButton(
								getResources().getString(R.string.OK),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
									}
								}).show();
			} else if (response_str.contains("NO CUSTOMER")) {
				new AlertDialog.Builder(this)
						.setTitle(getResources().getString(R.string.errors))
						.setMessage(
								getResources().getString(
										R.string.error_no_customer))
						.setPositiveButton(
								getResources().getString(R.string.OK),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
									}
								}).show();
			} else {
				Gson gson = new Gson();
				JSONRejectedPremiseItem premise;
				try {
					premise = gson.fromJson(response_str,
							JSONRejectedPremiseItem.class);
					Log.w("PREMISE SEARCH", "parsiran PREMISE!!!");
					// fill the fields with values :)

					clearAll();
					didSearch = true;
					fillFieldsWithData(premise);
					this.hcn = premise.house_connection_number;
					toggleForm(true);
					txtWaterMeterReading.requestFocus();
					txtSearchHcn.setText("");
					txtSearchWaterMeterNumber.setText("");
					loadedPremise = premise;
				} catch (Exception e) {
					Log.w("GRE��KA", "NE MOGU PARSIRATI NA��ENI PREMISE!!!"
							+ response_str, e);
				}
			}
		}

		searchTask = null;
	}
	
	


	private void fillFieldsWithData(JSONRejectedPremiseItem tmp) {

		if (tmp.premise_group_id!=4) {
			if (tmp.premise_name != null) {
				txtPremiseName.setText(tmp.premise_name);
			}
			lnPremiseName.setVisibility(View.VISIBLE);
		}else{
			lnPremiseName.setVisibility(View.GONE);
		}
		if (tmp.full_name == null || tmp.full_name.equals(""))
		lnFullName.setVisibility(View.GONE);
		else {
			txtFullName.setText(tmp.full_name);
			lnFullName.setVisibility(View.VISIBLE);
		}

		if (tmp.district == null || tmp.district.equals(""))
			lnDistrict.setVisibility(View.GONE);
		else {
	    	
	    	
			txtDistrict.setText(tmp.district);
			lnDistrict.setVisibility(View.VISIBLE);
		}
		
		if (Integer.valueOf(tmp.district_id)==null || Integer.valueOf(tmp.district_id)==0){
			JSONSpinnerItem tmps=new JSONSpinnerItem();
			tmps.id=-1;
			spnDistrict.setSelection(adptDistrict.getPosition(tmps));
			Log.w("DISTRICT NULL","DISTRICT NULL");
		}else {
			JSONSpinnerItem tmps=new JSONSpinnerItem();
			tmps.id=Integer.valueOf(tmp.district_id);
			spnDistrict.setSelection(adptDistrict.getPosition(tmps));

		}

		if (tmp.house_connection_number == null
				|| tmp.house_connection_number.equals(""))
			lnHcn.setVisibility(View.GONE);
		else {
			txtHcn.setText(tmp.house_connection_number);
			lnHcn.setVisibility(View.VISIBLE);
		}

		if (tmp.premise_type == null || tmp.premise_type.equals(""))
			lnType.setVisibility(View.GONE);
		else {
			txtType.setText(tmp.premise_type);
			lnType.setVisibility(View.VISIBLE);
		}

		if (tmp.water_meter_number == null || tmp.water_meter_number.equals(""))
			lnWmn.setVisibility(View.GONE);
		else {
			txtWmn.setText(tmp.water_meter_number);
			lnWmn.setVisibility(View.VISIBLE);
		}

		if (tmp.photo_house != null) {
			btnPhoto1.setVisibility(View.VISIBLE);
		}
		if (tmp.photo_house_2 != null) {
			btnPhoto2.setVisibility(View.VISIBLE);
		}
		if (tmp.photo_house_3 != null) {
			btnPhoto3.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString("mCurrentPhotoPath", mCurrentPhotoPath);
		if (pathImgWaterMeter != null) {
			outState.putString("pathImgWaterMeter", pathImgWaterMeter);
			Log.w("SAVING WATER METER PIC", "JPG");
		}
		if (myLat != null && myLng != null && myAlt != null) {
			outState.putString("myLat", myLat);
			outState.putString("myLng", myLng);
			outState.putString("myAlt", myAlt);
		}
		outState.putBoolean("canDeleteImgWaterMeter", canDeleteImgWaterMeter);
		outState.putBoolean("didSearch", didSearch);

		// save states of text fields
		outState.putString("txtWaterMeterReading", txtWaterMeterReading
				.getText().toString());
		outState.putString("hcn", hcn);

		outState.putBoolean("gettingLocation", gettingLocation);
		outState.putString("txtSearchWaterMeterNumber",txtSearchWaterMeterNumber.getText().toString());
		outState.putString("txtSearchHcn", txtSearchHcn.getText().toString());
		outState.putString("txtSearchSec", txtSearchSec.getText().toString());
		outState.putString("txtPremiseName", txtPremiseName.getText().toString());
		outState.putParcelable("loadedPremise", loadedPremise);
		outState.putString("txtHouseConnectionNumber", txtHouseConnectionNumber.getText().toString());
		outState.putString("txtSecDbNumber", txtSecDbNumber.getText().toString());
		outState.putString("txtWaterMeterNumber", txtWaterMeterNumber.getText().toString());
		outState.putString("txtAreaName", txtAreaName.getText().toString());
		outState.putBoolean("manualEnabled", manualEnabled);
		outState.putInt("spnDistrict", spnDistrict.getSelectedItemPosition());
		// save text views
		outState.putString("txtHcn", txtHcn.getText().toString().trim());
		outState.putString("txtWmn", txtWmn.getText().toString().trim());
		outState.putString("txtFullName", txtFullName.getText().toString()
				.trim());
		outState.putString("txtType", txtType.getText().toString().trim());
		outState.putString("txtDistrict", txtDistrict.getText().toString()
				.trim());

		// snimi listu dinami��kih slika
		outState.putParcelableArrayList("lstPictures", lstPictures);
		outState.putString("currentDynamicTag", currentDynamicTag);
		outState.putInt("dynamicImageIndexCounter", dynamicImageIndexCounter);

		super.onSaveInstanceState(outState);
		Log.w("SAVING INSTANCE", "SAVING!!!");

	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		if (savedInstanceState != null) {
			Log.w("RESTORING STATE", "RESTORING!!!");
			pathImgWaterMeter = savedInstanceState
					.getString("pathImgWaterMeter");
			myLat = savedInstanceState.getString("myLat");
			myLng = savedInstanceState.getString("myLng");
			myAlt = savedInstanceState.getString("myAlt");
			manualEnabled=savedInstanceState.getBoolean("manualEnabled");
			
			if (manualEnabled) {
				enableForm();
			}
			// invalidate imageviews to force images from camera to show if
			// activity gets destroyed and instance state is saved!!!
			if (pathImgWaterMeter != null) {
				setPic(imgWaterMeter, pathImgWaterMeter);
			}
			canDeleteImgWaterMeter = savedInstanceState
					.getBoolean("canDeleteImgWaterMeter");

			hcn = savedInstanceState.getString("hcn");
			gettingLocation = savedInstanceState.getBoolean("gettingLocation");
			txtSearchWaterMeterNumber.setText(savedInstanceState.getString(
					"txtSearchWaterMeterNumber", ""));
			txtSearchHcn.setText(savedInstanceState.getString("txtSearchHcn",
					""));
			txtHouseConnectionNumber.setText(savedInstanceState.getString("txtHouseConnectionNumber",
					""));
			txtSecDbNumber.setText(savedInstanceState.getString("txtSecDbNumber",
					""));
			txtWaterMeterNumber.setText(savedInstanceState.getString("txtWaterMeterNumber",
					""));
			txtAreaName.setText(savedInstanceState.getString("txtAreaName",
					""));
			txtSearchSec.setText(savedInstanceState.getString("txtSearchSec",
					""));
			didSearch = savedInstanceState.getBoolean("didSearch", false);
			loadedPremise = savedInstanceState.getParcelable("loadedPremise");
			spnDistrict.setSelection(savedInstanceState.getInt("spnDistrict",0));
			
			txtWaterMeterReading.setText(savedInstanceState.getString(
					"txtWaterMeterReading", ""));
			txtPremiseName.setText(savedInstanceState.getString(
					"txtPremiseName", ""));
			mCurrentPhotoPath = savedInstanceState
					.getString("mCurrentPhotoPath");
			if (mCurrentPhotoPath != null)
				f = new File(mCurrentPhotoPath);

			// get text views
			if (loadedPremise != null)
				fillFieldsWithData(loadedPremise);

			// ucitaj listu dinami��kih slika
			lstPictures = savedInstanceState
					.getParcelableArrayList("lstPictures");
			if (lstPictures.size() > 0) {
				recreatePictureBoxes();
			}
			currentDynamicTag = savedInstanceState
					.getString("currentDynamicTag");
			dynamicImageIndexCounter = savedInstanceState
					.getInt("dynamicImageIndexCounter");

		}
	}

	private void recreatePictureBoxes() {
		if (lstPictures != null) {
			for (JSONPictureItem pic : lstPictures) {
				createNewPictureBox(pic);
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.w("RESUME", "RESUMING!!!");
		if (gettingLocation == true) {
			Log.w("GETTING LOC CREATE", "TRUE");
			getGPSLocation();
		}

		// enable disable form
		toggleForm(didSearch);

	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Log.w("RESTARTING", "RESTARTING!!!");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.w("STOPING", "STOPING AND SAVING USER!!!");
		GlobalFunctionsAndConstants.savePrefs(this);
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		if (task != null) {
			task.detach();
			return (task);
		}
		if (searchTask != null) {
			searchTask.detach();
			return (searchTask);
		}
		return null;
	}

	/**
	 * postavi context menu kad se klikne na sliku ako je slika vec postavljena
	 * onda ce imat opciju brisi, a ako nije onda samo capture i choose
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		// context menu can be opened only if user has searched (enable
		// /disable)
		if (didSearch) {

			switch (v.getId()) {
			case R.id.imgWaterMeter:
				createImageContextMenu(menu, R.id.takeWaterMeterPhoto,
						R.id.selectWaterMeterPhoto, R.id.deleteWaterMeterPhoto);
				break;
			}

			// ako je dinami��ka slika
			String tag = null;
			if (v.getTag() != null)
				tag = String.valueOf(v.getTag());
			if (tag != null) {
				Log.w("DYNAMIC IMG TAG", tag);
				if (tag.contains("img")) {
					int dynamicId = Integer.valueOf(tag.substring(3));
					createDynamicImageContextMenu(menu, dynamicId);
				}
			}
		}
	}

	private void createImageContextMenu(ContextMenu menu, int idTake,
			int idSelect, int idDelete) {
		String path = pathImgWaterMeter;

		menu.add(0, idTake, 0, "Take photo with camera");
		menu.add(0, idSelect, 0, "Select photo from gallery");
		if (path == null)
			menu.setHeaderTitle("Select photo");
		else {
			menu.setHeaderTitle("Edit photo");
			if (lstPictures.size() == 1) {
				// moze brisati samo ako nema dinamickih slika
				JSONPictureItem tmp = lstPictures.get(0);
				if (tmp.path == null)
					menu.add(0, idDelete, 0, "Delete photo");
			}
		}
	}

	private void createDynamicImageContextMenu(ContextMenu menu, int id) {
		JSONPictureItem tmp = new JSONPictureItem();
		tmp.setId(id);
		JSONPictureItem pictureItem = lstPictures.get(lstPictures.indexOf(tmp));
		String path = null;
		if (pictureItem != null)
			path = pictureItem.path;

		menu.add(0, R.id.takePhoto, 0, "Take photo with camera");
		menu.add(0, R.id.selectPhoto, 0, "Select photo from gallery");
		if (path == null)
			menu.setHeaderTitle("Select photo");
		else {
			menu.setHeaderTitle("Edit photo");
			menu.add(0, R.id.deletePhoto, 0, "Delete photo");
		}
		currentDynamicTag = "img" + id;
		Log.w("CURRNET DYNAMIC TAG", currentDynamicTag);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		int id = item.getItemId();

		switch (id) {
		case R.id.takeWaterMeterPhoto:
			startImageCapture(REQUEST_TAKE_WATER_METER_PHOTO);
			break;
		case R.id.deleteWaterMeterPhoto:
			deleteImageFile(canDeleteImgWaterMeter, pathImgWaterMeter,
					imgWaterMeter);
			pathImgWaterMeter = null;
			JSONPictureItem tmpDel = lstPictures.get(0);
			ImageView tmpImgDel = (ImageView) llPictures.findViewWithTag(tmpDel
					.getTag());
			llPictures.removeView(tmpImgDel);
			lstPictures.clear();
			break;
		case R.id.selectWaterMeterPhoto:
			selectImageFromGal(REQUEST_SELECT_WATER_METER);
			break;
		case R.id.takePhoto:
			startImageCapture(REQUEST_TAKE_PHOTO);
			break;
		case R.id.selectPhoto:
			selectImageFromGal(REQUEST_SELECT_PHOTO);
			break;
		case R.id.deletePhoto:
			JSONPictureItem tmp = new JSONPictureItem();
			tmp.setId(Integer.valueOf(currentDynamicTag.substring(3)));
			JSONPictureItem pictureDelete = lstPictures.get(lstPictures
					.indexOf(tmp));
			ImageView tmpImg = (ImageView) llPictures
					.findViewWithTag(currentDynamicTag);

			deleteImageFile(pictureDelete.canDelete, pictureDelete.path, tmpImg);
			currentDynamicTag = null;
			lstPictures.remove(pictureDelete);
			llPictures.removeView(tmpImg);
			break;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case REQUEST_TAKE_WATER_METER_PHOTO:
				if (imgWaterMeter != null) {
					setPic(imgWaterMeter, mCurrentPhotoPath);
					canDeleteImgWaterMeter = true;
					galleryAddPic();
				}
				pathImgWaterMeter = mCurrentPhotoPath;
				clearImageFileHandlers();
				if (lstPictures.size() == 0)
					createNewPictureBox(null);
				break;
			case REQUEST_SELECT_WATER_METER:
				setImageFromGalleryResult(data);
				canDeleteImgWaterMeter = false;
				if (lstPictures.size() == 0)
					createNewPictureBox(null);
				break;
			case REQUEST_TAKE_PHOTO:
				ImageView tmpImg = (ImageView) llPictures
						.findViewWithTag(currentDynamicTag);
				if (tmpImg != null) {
					setPic(tmpImg, mCurrentPhotoPath);
					galleryAddPic();
				}

				JSONPictureItem tmp = new JSONPictureItem();
				tmp.setId(Integer.valueOf(currentDynamicTag.substring(3)));
				JSONPictureItem picture = lstPictures.get(lstPictures
						.indexOf(tmp));
				picture.canDelete = true;
				picture.path = mCurrentPhotoPath;
				clearImageFileHandlers();
				currentDynamicTag = null;
				if (lstPictures.indexOf(picture) == (lstPictures.size() - 1))
					createNewPictureBox(null);
				break;
			case REQUEST_SELECT_PHOTO:
				setDynamicImageFromGalleryResult(data);
				currentDynamicTag = null;
				break;
			}
		}
	}

	private void setImageFromGalleryResult(Intent data) {
		Uri selectedImage = data.getData();
		String[] filePathColumn = { MediaStore.Images.Media.DATA };

		Cursor cursor = getContentResolver().query(selectedImage,
				filePathColumn, null, null, null);
		cursor.moveToFirst();

		int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
		pathImgWaterMeter = cursor.getString(columnIndex);
		setPic(imgWaterMeter, pathImgWaterMeter);

		cursor.close();
	}

	private void setDynamicImageFromGalleryResult(Intent data) {
		Uri selectedImage = data.getData();
		String[] filePathColumn = { MediaStore.Images.Media.DATA };

		Cursor cursor = getContentResolver().query(selectedImage,
				filePathColumn, null, null, null);
		cursor.moveToFirst();

		int columnIndex = cursor.getColumnIndex(filePathColumn[0]);

		JSONPictureItem tmpSelect = new JSONPictureItem();
		tmpSelect.setId(Integer.valueOf(currentDynamicTag.substring(3)));
		JSONPictureItem pictureSelect = lstPictures.get(lstPictures
				.indexOf(tmpSelect));
		pictureSelect.canDelete = false;
		pictureSelect.path = cursor.getString(columnIndex);

		ImageView tmpImg = (ImageView) llPictures
				.findViewWithTag(currentDynamicTag);
		setPic(tmpImg, pictureSelect.path);

		if (lstPictures.indexOf(pictureSelect) == (lstPictures.size() - 1))
			createNewPictureBox(null);
		cursor.close();
	}

	private void startImageCapture(int requestCode) {
		try {
			f = setUpPhotoFile();
			mCurrentPhotoPath = f.getAbsolutePath();
			Intent i = new Intent(
					android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
			startActivityForResult(i, requestCode);
		} catch (IOException e) {
		}
	}

	private void selectImageFromGal(int requestCode) {
		Intent i = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(i, requestCode);
	}

	private void deleteImageFile(Boolean canDelete, String path, ImageView img) {
		if (canDelete) {
			File f1 = new File(path);
			if (f1.delete()) {
				sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
						Uri.parse("file://"
								+ Environment.getExternalStorageDirectory())));
			}
		}
		img.setImageDrawable(getResources().getDrawable(R.drawable.capture));
	}

	private OnClickListener photoListener = new OnClickListener() {

		public void onClick(View v) {
			v.showContextMenu();

		}
	};

	private void clearImageFileHandlers() {
		mCurrentPhotoPath = null;
		f = null;
	}

	private File setUpPhotoFile() throws IOException {

		File f = createImageFile();
		mCurrentPhotoPath = f.getAbsolutePath();

		return f;
	}

	private File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		String imageFileName = "NWC_water_reading_" + timeStamp + "";
		File image = File.createTempFile(imageFileName.substring(0, 27),
				".jpg", getAlbumDir());
		mCurrentPhotoPath = image.getAbsolutePath();
		return image;
	}

	private File getAlbumDir() {
		File storageDir = null;

		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {

			storageDir = mAlbumStorageDirFactory.getAlbumStorageDir("NWC");

			if (storageDir != null) {
				if (!storageDir.mkdirs()) {
					if (!storageDir.exists()) {
						Log.d("CameraSample", "failed to create directory");
						return null;
					}
				}
			}

		} else {
			Log.v(getString(R.string.app_name),
					"External storage is not mounted READ/WRITE.");
		}

		return storageDir;
	}

	private void galleryAddPic() {
		Intent mediaScanIntent = new Intent(
				Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		File f = new File(mCurrentPhotoPath);
		Log.w("Adding gallery", "path:" + mCurrentPhotoPath);
		Uri contentUri = Uri.fromFile(f);
		mediaScanIntent.setData(contentUri);
		this.sendBroadcast(mediaScanIntent);
	}

	/**
	 * Compress picture and set it to imageview
	 */
	private void setPic(ImageView v, String path) {
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
		int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

		// Decode the image file into a Bitmap sized to fill the View
		bmOptions.inSampleSize = scaleFactor;
		bmOptions.inPurgeable = true;
		bmOptions.inJustDecodeBounds = false;

		Bitmap bitmap = BitmapFactory.decodeFile(path, bmOptions);
		v.setImageBitmap(bitmap);

		// decode original file and save it compressed
		int dimW = 1600;
		int dimH = 1200;

		// set width to be always the larger size of image
		if (photoH > photoW) {
			dimW = 1200;
			dimH = 1600;
		}

		// if larger size > 1600 we will compress
		if ((photoW > 1600 && photoW > photoH)
				|| (photoH > 1600 && photoH > photoW)) {
			try {
				bmOptions.inDither = false;
				bmOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
				bmOptions.inScaled = false;
				bmOptions.inSampleSize = photoW / dimW;
				bitmap = BitmapFactory.decodeFile(path, bmOptions);

				// Resize
				Matrix matrix = new Matrix();
				matrix.postScale((float) dimW / photoW, (float) dimW / photoW);
				Bitmap compressedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
						bitmap.getWidth(), bitmap.getHeight(), matrix, true);
				bitmap = null;
				// Bitmap compressedBitmap=Bitmap.createScaledBitmap(bitmap,
				// dimW, dimH, false);

				FileOutputStream os = new FileOutputStream(path);
				compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, os);
				compressedBitmap = null;
				Log.w("COMPRESS", "KOMPRESUJEM");
			} catch (Exception e) {
				Log.w("CANT COMPRESS", "ERROR", e);
			}

		}
	}

	private void clearAll() {
		mCurrentPhotoPath = null;
		imgWaterMeter.setImageDrawable(getResources().getDrawable(
				R.drawable.capture));

		pathImgWaterMeter = null;

		canDeleteImgWaterMeter = false;

		// my location
		myLat = null;
		myLng = null;
		myAlt = null;

		for (JSONPictureItem pic : lstPictures) {
			if (pic.tag != null)
				llPictures.removeView(llPictures.findViewWithTag(pic.tag));
		}
		lstPictures.clear();

		// define text fields
		txtWaterMeterReading.setText("");

		txtSearchHcn.setText("");
		txtSearchWaterMeterNumber.setText("");
		didSearch = false;
		manualEnabled=false;
		
		btnPhoto1.setVisibility(View.GONE);
		btnPhoto2.setVisibility(View.GONE);
		btnPhoto3.setVisibility(View.GONE);
		lnHouseConnectionNumber.setVisibility(LinearLayout.GONE);
		lnSecDbNumber.setVisibility(LinearLayout.GONE);
		lnWaterMeterNumber.setVisibility(LinearLayout.GONE);
		lnPremiseName.setVisibility(View.GONE);
		txtPremiseName.setText("");
		txtSearchSec.setText("");
		txtHouseConnectionNumber.setText("");
		txtSecDbNumber.setText("");
		txtWaterMeterNumber.setText("");
		txtAreaName.setText("");
		loadedPremise = null;
		btnSearch.setEnabled(true);
		lnDistrict.setVisibility(View.GONE);
		lnFullName.setVisibility(View.GONE);
		lnHcn.setVisibility(View.GONE);
		lnType.setVisibility(View.GONE);
		lnWmn.setVisibility(View.GONE);

		toggleForm(false);
	}

	// FUNCTION FOR ACQUIRING GPS LOCATION
	private void getGPSLocation() {
		MyLocation.LocationResult locationResult = new MyLocation.LocationResult() {
			@Override
			public void gotLocation(android.location.Location location,
					int provider) {
				GlobalFunctionsAndConstants.endLoading();
				gettingLocation = false;
				Log.w("LOCATION LISTENER", "GOT LOCATION RESPONSE!!!!");
				View windowRoot = getWindow().getDecorView().findViewById(
						android.R.id.content);
				if (location == null) {

					windowRoot.post(new Runnable() {
						public void run() {
							Toast.makeText(
									getApplicationContext(),
									getResources()
											.getString(
													R.string.location_service_unavailable),
									Toast.LENGTH_LONG).show();
						}
					});
					return;
				}
				// GPS toast location
				final android.location.Location loc = location;
				windowRoot.post(new Runnable() {
					public void run() {
						Toast.makeText(
								getApplicationContext(),
								getResources().getString(
										R.string.location_acquired)
										+ loc.getProvider(), Toast.LENGTH_SHORT)
								.show();
						// String[]
						// args={String.valueOf(loc.getLatitude()),String.valueOf(loc.getLongitude()),String.valueOf(loc.getAltitude())};

						myLat = String.valueOf(loc.getLatitude());
						myLng = String.valueOf(loc.getLongitude());
						myAlt = String.valueOf(loc.getAltitude());
						ActivityWaterMeterStatus.this.setGpsLocation();
					}
				});

			}
		};
		myLocation = new MyLocation();
		gettingLocation = true;
		if (myLocation.getLocation(this, locationResult) == false) {
			Toast.makeText(
					this,
					getResources().getString(
							R.string.location_service_unavailable),
					Toast.LENGTH_LONG).show();
			gettingLocation = false;
		} else {
			GlobalFunctionsAndConstants.startLoading(this, null, this
					.getResources().getString(R.string.getting_gps));
		}

	}

	private void setGpsLocation() {
		dialog = new ProgressDialog(this);
		dialog.setMessage(getResources().getString(R.string.sending));
		dialog.setIndeterminate(false);
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.setProgress(0);
		dialog.show();
		task = new SubmitWaterMeterForm(this);
		task.execute();
	}

	/**
	 * Enable or disable input form
	 * 
	 * @param Boolean
	 *            enable
	 */
	private void toggleForm(Boolean enable) {
		txtWaterMeterReading.setEnabled(enable);
		btnSubmit.setEnabled(enable);
	}
	/**
	 * Enable manual hcn entry
	 * Hide premise name then since premise does not exists
	 */

	private void enableForm(){
		lnHouseConnectionNumber.setVisibility(LinearLayout.VISIBLE);
		lnSecDbNumber.setVisibility(LinearLayout.VISIBLE);
		lnWaterMeterNumber.setVisibility(LinearLayout.VISIBLE);
		lnPremiseName.setVisibility(View.VISIBLE);
		didSearch=true;
		manualEnabled=true;

	}
	@Override
	protected void onPause() {
		super.onPause();
		if (myLocation != null)
			myLocation.cancelUpdates();

		GlobalFunctionsAndConstants.endLoading();
	}

	private void checkData(){
		dialog = new ProgressDialog(this);
		dialog.setMessage(getResources().getString(R.string.sending));
		dialog.setIndeterminate(false);
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.setProgress(0);
		dialog.show();
		checkTask = new checkData(this);
		checkTask.execute();
	}
	
	
	
	// start preise search task
	private void searchPremise() {
		dialog = new ProgressDialog(this);
		dialog.setMessage(getResources().getString(R.string.sending));
		dialog.setIndeterminate(false);
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.setProgress(0);
		dialog.show();
		searchTask = new SearchPremise(this);
		searchTask.execute();
	}
}
