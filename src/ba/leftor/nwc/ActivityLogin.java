package ba.leftor.nwc;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

public class ActivityLogin extends Activity {
	
	private EditText txtEmail;
	private EditText txtPass;
	private CheckBox chkRememberMe;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.login);
        
        txtEmail=(EditText)findViewById(R.id.txtEmail);
		txtPass=(EditText)findViewById(R.id.txtPass);
		chkRememberMe=(CheckBox)findViewById(R.id.chkRememberMe);
		
		//check if user saved login state !
		SharedPreferences prefs=ActivityLogin.this.getSharedPreferences(GlobalFunctionsAndConstants.PREFS_NAME, 0);
		String lastUser=prefs.getString("username", null);
		String lastPass=prefs.getString("pass", null);
		if(lastPass!=null && lastUser!=null){
			txtEmail.setText(lastUser);
			txtPass.setText(lastPass);
			chkRememberMe.setChecked(true);
			
			//chekc if there are districts in catalog
			MyDataSource ds=new MyDataSource(this);
			ds.open();
			int districtCount=ds.catalogDistrictItemCount();
			int waterMeterCount=ds.catalogWaterMeterStatusItemCount();
			ds.close();
			if(districtCount > 0 && waterMeterCount>0)
				tryLogin(true);
			else 
				tryLogin(false);
		}
			
		
		
        Button btnLogin=(Button)findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				tryLogin(false);
			}
		});
    }
    
    private void tryLogin(Boolean offline){
    	//check for internet connection
		if (GlobalFunctionsAndConstants.isNetworkAvailable(ActivityLogin.this)) {
			String[] params = new String[3];
			params[0] = txtEmail.getText().toString();
			params[1] = txtPass.getText().toString();
			if (chkRememberMe.isChecked())
				params[2] = "yes";
			else
				params[2] = "no";
			new LoginUser().execute(params);
		} else {
			if(offline==false){
				GlobalFunctionsAndConstants.createInternetDisabledAlert(ActivityLogin.this, false);
			} else {
				GlobalFunctionsAndConstants.USER=new JSONUSerItem();
				GlobalFunctionsAndConstants.USER.username=txtEmail.getText().toString();
            	GlobalFunctionsAndConstants.USER.password=txtPass.getText().toString();
            	GlobalFunctionsAndConstants.USER.fullName=txtEmail.getText().toString();
            	
            	SharedPreferences prefs=ActivityLogin.this.getSharedPreferences(GlobalFunctionsAndConstants.PREFS_NAME, 0);
            	GlobalFunctionsAndConstants.USER.premisePermission=prefs.getInt("premisePermission", 0);
            	GlobalFunctionsAndConstants.USER.customerPermission=prefs.getInt("customerPermission", 0);
            	GlobalFunctionsAndConstants.USER.rejectedCustomerPermission=prefs.getInt("rejectedCustomerPermission", 0);
            	GlobalFunctionsAndConstants.USER.rejectedPremisePermission=prefs.getInt("rejectedPremisePermission", 0);
            	GlobalFunctionsAndConstants.USER.waterMeterStatusPermission=prefs.getInt("waterMeterStatusPermission", 0);
            	GlobalFunctionsAndConstants.USER.waterLeakPermission=prefs.getInt("waterLeakPermission", 0);
            	GlobalFunctionsAndConstants.USER.reconnectionPermission=prefs.getInt("reconnectionPermission", 0);
            	GlobalFunctionsAndConstants.USER.androidViolationHcnSearch=prefs.getInt("androidViolationHcnSearch", 0);
            	GlobalFunctionsAndConstants.USER.androidViolationSubmitGps=prefs.getInt("androidViolationSubmitGps", 0);

            	startActivity(new Intent(ActivityLogin.this,ActivityDashboard.class));
            	this.finish();
			}
		}
    }
    
 
	/** THREAD FOR USER LOGIN - AND LOADING CATALOG AFTER SUCCESSFULL LOGIN 
	 * IF USER LAST LOGIN DATE IS NULL THEN IT'S FIRST LOGIN SO WE HAVE TO REDIRECT HIM TO CHANGE PASSWORD FORM
	 * **/
    private class LoginUser extends AsyncTask<String, Void, Boolean>
    {
    	private JSONStartupData startupData;
    	
    	@Override
		protected void onPreExecute() {
			super.onPreExecute();
			GlobalFunctionsAndConstants.startLoading(ActivityLogin.this, null, ActivityLogin.this.getResources().getString(R.string.contacting_server));
		}

		private String username,pass,rememberMe;
		@Override
		protected Boolean doInBackground(String... params) {
			
			String url;
			
			username=params[0];
			pass=params[1];
			rememberMe=params[2];
			
			url=getString(R.string.url_login);
			
			
			HttpClient client = new DefaultHttpClient();
            HttpEntity resEntity;
            HttpPost post = new HttpPost(url);
            MultipartEntity reqEntity=new MultipartEntity();
            String response_str="";
            HttpResponse response=null;
            
            try{     
	            reqEntity.addPart("username", new StringBody(params[0]));
	            reqEntity.addPart("pass", new StringBody(params[1]));
				
				
	            post.setEntity(reqEntity);
	            response = client.execute(post);
	            resEntity = response.getEntity();
	            response_str = EntityUtils.toString(resEntity);
	            Log.w("RESPONSE LOGIN", response_str);
			} catch (Exception e) {
				Log.w("LOGIN POST FAILED", "NEMA KONEKCIJE");
				return false;
			}
            
            startupData=null;
            if (resEntity != null) {
            	Log.w("Response from server :",response_str);
            	Gson gson = new Gson();
            	try {
            		startupData = gson.fromJson(response_str,JSONStartupData.class);
                } catch (Exception e) {
                    return false;
                }
            }
            if(startupData==null) return false;
            if (startupData.user != null) {
            	GlobalFunctionsAndConstants.USER=startupData.user;
            	Log.w("PERMISIJE","CUST:"+startupData.user.customerPermission+" -- PREM:"+startupData.user.premisePermission+" -- REJPRM:"+startupData.user.rejectedPremisePermission);
            	GlobalFunctionsAndConstants.USER.password=params[1];
                return true;
            } else {
            	return false;
            }	

		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			GlobalFunctionsAndConstants.endLoading();
			if(result==true){
				
				Toast.makeText(ActivityLogin.this, ActivityLogin.this.getResources().getString(R.string.login_successfull), Toast.LENGTH_LONG).show();
				
				SharedPreferences prefs=ActivityLogin.this.getSharedPreferences(GlobalFunctionsAndConstants.PREFS_NAME, 0);
				Editor e=prefs.edit();
				if(rememberMe=="yes"){
					e.putString("username", username);
					e.putString("pass", pass);
					if(GlobalFunctionsAndConstants.USER!=null){
						e.putInt("premisePermission", GlobalFunctionsAndConstants.USER.premisePermission);
						e.putInt("customerPermission", GlobalFunctionsAndConstants.USER.customerPermission);
						e.putInt("rejectedPremisePermission", GlobalFunctionsAndConstants.USER.rejectedPremisePermission);
						e.putInt("rejectedCustomerPermission", GlobalFunctionsAndConstants.USER.rejectedCustomerPermission);
						e.putInt("waterMeterStatusPermission", GlobalFunctionsAndConstants.USER.waterMeterStatusPermission);
						e.putInt("waterLeakPermission", GlobalFunctionsAndConstants.USER.waterLeakPermission);
						e.putInt("reconnectionPermission", GlobalFunctionsAndConstants.USER.reconnectionPermission);
						e.putInt("androidViolationHcnSearch", GlobalFunctionsAndConstants.USER.androidViolationHcnSearch);
						e.putInt("androidViolationSubmitGps", GlobalFunctionsAndConstants.USER.androidViolationSubmitGps);

					
					}
					
					e.commit();
				} else{
					e.remove("username");
					e.remove("pass");
				}
				e.putString("serviceUsername", username);//da moze servis slat od zadnjeg logovanog korisnika
				e.putString("servicePass", pass);//da moze servis slat od zadnjeg logovanog korisnika
				e.putInt("newVersion", startupData.appVersion);
				e.commit();
				
				
				//save catalog data to db and go to dashboard (or CHANGE PASS FORM if it's first login!)
				if(saveCatalogDataToDb(startupData)){
					GlobalFunctionsAndConstants.savePrefs(ActivityLogin.this);
					JSONUSerItem nwcUser=GlobalFunctionsAndConstants.getUser(ActivityLogin.this);
					
					if(nwcUser.lastLoginDate == null){
						startActivity(new Intent(ActivityLogin.this,ActivityChangePass.class));
					} else {
						startActivity(new Intent(ActivityLogin.this,ActivityDashboard.class));
					}
					
					ActivityLogin.this.finish();
				}
			}
			else {
				Toast.makeText(ActivityLogin.this, ActivityLogin.this.getResources().getString(R.string.login_failed), Toast.LENGTH_LONG).show();
				
			}
		}     
    }
    
    
    private Boolean saveCatalogDataToDb(JSONStartupData data){
    	//check if all catalogs exist
    	if(data.lstInhabitantTypes==null) return false;
    	if(data.lstNationalities==null) return false;
       	if(data.lstTarsheedTypes==null) return false;
       	if(data.lstPremiseTypes==null) return false;
       	if(data.lstDistricts==null) return false;
       	if(data.lstWaterMeterStatuses==null) return false;
       	if(data.lstViolationPenalties==null) return false;
    	
    	//open datasource
    	MyDataSource dataSource=new MyDataSource(this);
    	dataSource.open();
    	//clear all catalogs
    	dataSource.deleteAllCatalogItems();
    	//insert new data
    	for (JSONSpinnerItem item : data.lstInhabitantTypes) {
			dataSource.addCatalogItemToDb(item, "inhabitant_type");
		}
    	for (JSONSpinnerItem item : data.lstNationalities) {
			dataSource.addCatalogItemToDb(item, "nationality");
		}
    	for (JSONSpinnerItem item : data.lstTarsheedTypes) {
			dataSource.addCatalogItemToDb(item, "tarsheed_type");
		}
    	
    	for (JSONSpinnerItem item : data.lstPremiseTypes) {
			dataSource.addCatalogItemToDb(item, "premise_type");
		}
    	
    	for (JSONSpinnerItem item : data.lstDistricts) {
			dataSource.addCatalogItemToDb(item, "district");
		}
    	
    	for (JSONSpinnerItem item : data.lstWaterMeterStatuses) {
			dataSource.addCatalogItemToDb(item, "water_meter_status");
		}
    	
    	for (JSONSpinnerItem item : data.lstViolationPenalties) {
			dataSource.addCatalogItemToDb(item, "violation_penalty");
		}
    	
    	dataSource.close();
    	return true;
    }

}