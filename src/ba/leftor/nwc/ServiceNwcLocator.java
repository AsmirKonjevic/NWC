package ba.leftor.nwc;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import ba.leftor.nwc.CustomMultiPartEntity.ProgressListener;

public class ServiceNwcLocator extends IntentService {

	int sendLocation = 0;

	/**
	 * Default intent service constructor
	 */
	public ServiceNwcLocator() {
		super("ServiceNwcLocator");
	}

	/**
	 * Main method of IntentService!
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		if (sendLocation < 2) {
			getLocation();
		}
	}

	private void getLocation() {

		// /get location and call send location :)
		MyLocation.LocationResult locationResult = new MyLocation.LocationResult() {
			@Override
			public void gotLocation(android.location.Location location,
					int provider) {
				GlobalFunctionsAndConstants.endLoading();

				Log.w("LOCATOR SERVICE", "GOT LOCATION RESPONSE!!!!");
				if (location == null) {
					Log.w("LOCATOR SERVICE",
							"LOCATION UNAVAILABLE, CANT GET LOCATION");
					return;
				}
				// imamo lokaciju, salji je
				sendLocation(String.valueOf(location.getLatitude()),
						String.valueOf(location.getLongitude()),
						String.valueOf(location.getAltitude()));

			}
		};
		MyLocation myLocation = new MyLocation();

		if (myLocation.getLocation(this, locationResult) == false) {
			Log.w("LOCATOR SERVICE", "GPS OFF");
		}

	}

	@Override
	public void onDestroy() {
		// Tell the user we stopped.
		Log.w("LOCATOR SERVICE", "DESTROYED, STOP");
	}

	private void sendLocation(String lat, String lon, String alt) {
		try {
			sendLocation = sendLocation + 1;
			if (sendLocation < 2) {
				// load user
				SharedPreferences prefs = this.getSharedPreferences(
						GlobalFunctionsAndConstants.PREFS_NAME, 0);
				JSONUSerItem user = new JSONUSerItem();
				user.username = prefs.getString("serviceUsername", "");
				user.password = prefs.getString("servicePass", "");
				if (user.password.equals("")) {
					user.username = prefs.getString("username", "");
					user.password = prefs.getString("pass", "");
				}

				String urlString = this.getApplicationContext().getResources()
						.getString(R.string.url_send_location);
				// fix timeout errors
				HttpParams httpParameters = new BasicHttpParams();
				int timeoutConnection = 25000;
				HttpConnectionParams.setConnectionTimeout(httpParameters,
						timeoutConnection);
				int timeoutSocket = 25000;
				HttpConnectionParams
						.setSoTimeout(httpParameters, timeoutSocket);
				DefaultHttpClient client = new DefaultHttpClient(httpParameters);

				HttpEntity resEntity;
				HttpPost post = new HttpPost(urlString);
				client.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(
						0, false));

				CustomMultiPartEntity reqEntity = new CustomMultiPartEntity(
						new ProgressListener() {
							public void transferred(long num) {

							}
						});

				// testing
				SimpleDateFormat s = new SimpleDateFormat("ddMMyyyyhhmmss");
				String format = s.format(new Date());

				/** parse gps **/

				reqEntity.addPart("gps_lat", new StringBody(lat));
				reqEntity.addPart("gps_long", new StringBody(lon));
				reqEntity.addPart("gps_alt", new StringBody(alt));
				reqEntity.addPart("version", new StringBody("48"));
				reqEntity.addPart("timestamp", new StringBody(format));
				reqEntity.addPart("retries",
						new StringBody(String.valueOf(sendLocation)));
				reqEntity.addPart("username", new StringBody(user.username));
				reqEntity.addPart("pass", new StringBody(user.password));

				post.setEntity(reqEntity);
				HttpResponse response = client.execute(post);
				resEntity = response.getEntity();
				String responseStr = EntityUtils.toString(resEntity);
				if (responseStr != null) {
					if (responseStr.contains("OK")) {
						Log.w("NWC_SERVICE_LOCATOR", "SUCCESS");
					} else {
						Log.w("NWC_SERVICE_LOCATORR", "FAILED:" + responseStr);
					}
				}
			}
			return;
		} catch (Exception e) {
			Log.w("NWC_SERVICE_LOCATORR", "ERROR WHILE CONNECTING", e);
			return;
		}
	}

}
