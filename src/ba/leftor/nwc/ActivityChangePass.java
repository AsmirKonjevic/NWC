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
import android.widget.TextView;
import android.widget.Toast;

public class ActivityChangePass extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.changepass);
		
		final TextView txtNewPass=(TextView)findViewById(R.id.txtNewPass);
		final TextView txtConfirmPass=(TextView)findViewById(R.id.txtConfirmPass);
		final Button btnChangePass=(Button)findViewById(R.id.btnChangePass);
		
		btnChangePass.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				boolean error=false;
				String newPass=txtNewPass.getText().toString().trim();
				String confirmPass=txtConfirmPass.getText().toString().trim();
				if( newPass.length() < 6 ){
					txtNewPass.setError(getResources().getString(R.string.min_6));
					error=true;
				} else{
					txtNewPass.setError(null);
				}
				if( !newPass.equals(confirmPass)){
					txtNewPass.setError(getResources().getString(R.string.pass_not_match));
					error=true;
				} else{
					if(!error)
						txtNewPass.setError(null);
				}
				
				if(!error){
					//change pass thread start!
					//check for internet connection
					if (GlobalFunctionsAndConstants.isNetworkAvailable(ActivityChangePass.this)) {
						String[] params = new String[2];
						params[0] = newPass;
						params[1] = confirmPass;
						new ChangeUserPass().execute(params);
					} else {
						GlobalFunctionsAndConstants.createInternetDisabledAlert(
								ActivityChangePass.this, false);
					}
				}
				
			}
		});
	}
	
    private class ChangeUserPass extends AsyncTask<String, Void, Boolean>
    {
    	
    	@Override
		protected void onPreExecute() {
			super.onPreExecute();
			GlobalFunctionsAndConstants.startLoading(ActivityChangePass.this, null, ActivityChangePass.this.getResources().getString(R.string.contacting_server));
		}

		private String newPass,confirmPass, statusResponse;
		@Override
		protected Boolean doInBackground(String... params) {
			
			String url;
			
			newPass=params[0];
			confirmPass=params[1];
			
			url=getString(R.string.url_change_pass);
			
			
			HttpClient client = new DefaultHttpClient();
            HttpEntity resEntity;
            HttpPost post = new HttpPost(url);
            MultipartEntity reqEntity=new MultipartEntity();
            String response_str="";
            HttpResponse response=null;
            
            JSONUSerItem nwcUser=GlobalFunctionsAndConstants.getUser(ActivityChangePass.this);
            
            try{     
	            reqEntity.addPart("username", new StringBody(nwcUser.username));
	            reqEntity.addPart("pass", new StringBody(nwcUser.password));
	            reqEntity.addPart("newPass", new StringBody(params[0]));
	            reqEntity.addPart("confirmNewPass", new StringBody(params[1]));
				
				
	            post.setEntity(reqEntity);
	            response = client.execute(post);
	            resEntity = response.getEntity();
	            response_str = EntityUtils.toString(resEntity);
	            Log.w("RESPONSE LOGIN", "RESPONSE"+response_str);
			} catch (Exception e) {
				Log.w("LOGIN POST FAILED", "NEMA KONEKCIJE");
				return false;
			}
            
            statusResponse=null;
            if (response_str != null) {
            	try {
            		statusResponse = response_str;
            		if(statusResponse.contains("ERROR")) return false;
            		else if(statusResponse.contains("OK")) return true;
            		return false;
                } catch (Exception e) {
                    return false;
                }
            }
            return false;
            

		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			GlobalFunctionsAndConstants.endLoading();
			if(result==true){
				Log.w("RESPONSE CHANGE", "START:"+statusResponse);
				Toast.makeText(ActivityChangePass.this, ActivityChangePass.this.getResources().getString(R.string.login_successfull), Toast.LENGTH_LONG).show();
				
				SharedPreferences prefs=ActivityChangePass.this.getSharedPreferences(GlobalFunctionsAndConstants.PREFS_NAME, 0);
				String lastUser=prefs.getString("username", null);
				String lastPass=prefs.getString("pass", null);
				Editor e=prefs.edit();
				if(lastPass!=null && lastUser!=null){
					
					e.putString("pass", newPass);
					
				}
				e.putString("servicePass", newPass);//da moze servis slat od zadnjeg logovanog korisnika
				e.commit();
				
				JSONUSerItem nwcUser=GlobalFunctionsAndConstants.getUser(ActivityChangePass.this);
				nwcUser.password=newPass;
				GlobalFunctionsAndConstants.savePrefs(ActivityChangePass.this);
				startActivity(new Intent(ActivityChangePass.this,ActivityDashboard.class));
				ActivityChangePass.this.finish();
				
			}
			else {
				if(statusResponse!=null)
					Toast.makeText(ActivityChangePass.this, statusResponse, Toast.LENGTH_LONG).show();
				else
					Toast.makeText(ActivityChangePass.this, "SERVER ERROR!", Toast.LENGTH_LONG).show();
				
			}
		}     
    }
	
}
