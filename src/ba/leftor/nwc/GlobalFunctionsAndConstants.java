package ba.leftor.nwc;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.util.Log;
import android.widget.TextView;

import com.google.gson.Gson;

public class GlobalFunctionsAndConstants {
		public static final String PREFS_NAME="mkdprefs"; //prefs file
		private static ProgressDialog dialog;

		public static JSONUSerItem USER;
		
		
		/**
		 * Starta loader
		 * @param Context c
		 * @param OnCancelListener listener
		 * @String text
		 */
		public static void startLoading(Context c, OnCancelListener listener,String text){
			try{
			dialog=ProgressDialog.show(c, "NWC", text, true, true);
	        //ako nesto nece da ucita onda moze i cancel
	        dialog.setOnCancelListener(listener);
			} catch (Exception e) {
			}
		}
		
		/**
		 * Zatvara loader
		 */
		public static void endLoading(){
			try {
				dialog.dismiss();
			} catch (Exception e) {
			}
		}
		
		
		/**
		 * brise sve preference vezane za globalne varijeble 
		 *(poziva loading activity pri svakom novom startu aplikacije)
		 * @param context
		 * @return void
		 */
		public static void clearPrefs(Context c){
			SharedPreferences prefs=c.getSharedPreferences(PREFS_NAME, 0);
			Editor e=prefs.edit();
			
			Gson gson=new Gson();
			String tmp=gson.toJson(USER);
			e.putString("JSON_user", tmp);
			
			e.commit();				
		}		
		
		
		/**
		 * snima LOGOVANI_KORISNIK i USER_IP stati�ke varijeble 
		 * na on PAUSE i ONRESUME metodama da bi sprije�ili NULL POINTER EXCEPTION
		 * kada Dalvik ubije aplikaciju dok aplikacija radi u pozadini (app lifecycle)
		 * pobolj�ana sigurnost da ne bi padala aplikacija!!
		 * @param context
		 * @return void
		 */
		public static void savePrefs(Context c){
			SharedPreferences prefs=c.getSharedPreferences(PREFS_NAME, 0);
			Editor e=prefs.edit();
			//ako nisu null snimi ih ali prvo JSON SERIALIZE da bi mogli ko string snimit
			if(USER!=null){
				Gson gson=new Gson();
				String tmp=gson.toJson(USER);
				e.putString("JSON_user", tmp);
			}
			//Log.w("SAVEPREFS", "IP:"+USER_IP);
			e.commit();				
		}
		
		/**
		 * u�itava LOGOVANI_KORISNIK i USER_IP stati�ke varijeble 
		 * na on PAUSE i ONRESUME metodama da bi sprije�ili NULL POINTER EXCEPTION
		 * kada Dalvik ubije aplikaciju dok aplikacija radi u pozadini (app lifecycle)
		 * pobolj�ana sigurnost da ne bi padala aplikacija!!
		 * @param context
		 * @return void
		 */
		public static void loadPrefs(Context c){
			SharedPreferences prefs=c.getSharedPreferences(PREFS_NAME, 0);
			//prvo provjeri da nije null inace ce prvi on resume ponistit sve varijable
			//jer nece bit ucitane u preferencama
			String tmp;
			if(USER==null){
				tmp=prefs.getString("JSON_user", null);
				if(tmp!=null){
					Gson gson=new Gson();
					USER=gson.fromJson(tmp, JSONUSerItem.class);
				}
			}
			if(USER.password == null) {
				tmp=prefs.getString("JSON_user", null);
				if(tmp!=null){
					Gson gson=new Gson();
					USER=gson.fromJson(tmp, JSONUSerItem.class);
				}
			}
		}
		
		public static JSONUSerItem getUser(Context c){
			if(USER==null)
				GlobalFunctionsAndConstants.loadPrefs(c);
			if(USER.username == null || USER.password == null)
				GlobalFunctionsAndConstants.loadPrefs(c);
			return USER;
		}
		
		

	    //provjerava da li postoji internet i izbaci dijalog
	    public static void checkConnection(Activity c) {
	        if(!isNetworkAvailable(c))
	        	createInternetDisabledAlert(c, false);
	    }

	    
	    //provjerava da li postoji internet konekcija
	    public static boolean isNetworkAvailable(Context c) {
	        ConnectivityManager connectivityManager 
	              = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
	        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	        boolean connected = activeNetworkInfo != null
                    && activeNetworkInfo.isAvailable() && activeNetworkInfo.isConnected();
	        return connected;
	    }

	    //prikazuje dijalog zbog nepostojanja konekcije gdje ima opcija da se ode u podesavanja
	    public static void createInternetDisabledAlert(final Activity c,final Boolean finish){
	    	AlertDialog.Builder builder = new AlertDialog.Builder(c);
	    	builder.setMessage(c.getResources().getString(R.string.require_connection))
	    	     .setCancelable(false)
	    	     .setPositiveButton(c.getResources().getString(R.string.settings),
	    	          new DialogInterface.OnClickListener(){
	    	          public void onClick(DialogInterface dialog, int id){
	    	        	  c.startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
	    	          }
	    	     });
	    	     builder.setNegativeButton(c.getResources().getString(R.string.close),
	    	          new DialogInterface.OnClickListener(){
	    	          public void onClick(DialogInterface dialog, int id){
	    	               dialog.cancel();
	    	               if(finish==true) c.finish();
	    	          }
	    	     });
	    	AlertDialog alert = builder.create();
	    	alert.show();
	    }
	    
	    //prikazuje dijalog zbog nepostojanja konekcije gdje ima opcija da se ode u podesavanja i izlaz iz aplikacije
	    public static void createInternetDisabledAlertFinish(final Activity c){
	    	AlertDialog.Builder builder = new AlertDialog.Builder(c);
	    	builder.setMessage(c.getResources().getString(R.string.require_connection))
	    	     .setCancelable(false)
	    	     .setPositiveButton(c.getResources().getString(R.string.settings),
	    	          new DialogInterface.OnClickListener(){
	    	          public void onClick(DialogInterface dialog, int id){
	    	        	  c.startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
	    	        	  c.finish();
	    	          }
	    	     });
	    	     builder.setNegativeButton(c.getResources().getString(R.string.close),
	    	          new DialogInterface.OnClickListener(){
	    	          public void onClick(DialogInterface dialog, int id){
	    	               dialog.cancel();
	    	               c.finish();
	    	          }
	    	     });
	    	AlertDialog alert = builder.create();
	    	alert.show();
	    }
	    
	    //validacija emaila
	    public static boolean isEmailValid(String email) {
	        boolean isValid = false;

	        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
	        CharSequence inputStr = email;

	        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
	        Matcher matcher = pattern.matcher(inputStr);
	        if (matcher.matches()) {
	            isValid = true;
	        }
	        return isValid;
	    }	
	    
	    /**
		 * SEC mora biti formata xx-xxxxxxx gdje su x brojevi
		 * @param String sec
		 * @return Boolean
		 */
	    public static boolean isSECValid(String sec) {
	        boolean isValid = false;

	        String expression = "^[0-9]{1,2}[-][0-9]{3,8}$";
	        CharSequence inputStr = sec;

	        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
	        Matcher matcher = pattern.matcher(inputStr);
	        if (matcher.matches()) {
	            isValid = true;
	        }
	        return isValid;
	    }	  
	    
	    /**
		 * HCN moze sadrzavati samo brojeve, razmak i (-) slash karakter
		 * @param String hcn
		 * @return Boolean
		 */
	    public static boolean isHCNValid(String sec) {
	        boolean isValid = false;

	        String expression = "^[A-Za-z0-9-\\s]+$";
	        CharSequence inputStr = sec;

	        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
	        Matcher matcher = pattern.matcher(inputStr);
	        if (matcher.matches()) {
	            isValid = true;
	        }
	        return isValid;
	    }	  
	    
	    //formatiraj float na 2 decimale
	    public static String f2dec(float f){
	    	NumberFormat numberFormat = DecimalFormat.getInstance(); 
	    	numberFormat.setMaximumFractionDigits(2);
	    	numberFormat.setMinimumFractionDigits(2);
	    	String t=numberFormat.format(f); 
	    	t=t.replace(".", ",");
	    	return t.trim();
	    }
	    
	  ///kopira ulazni i izlazni stream
	    public static void CopyStream(InputStream is, OutputStream os)
	    {
	        final int buffer_size=1024;
	        try
	        {
	            byte[] bytes=new byte[buffer_size];
	            for(;;)
	            {
	              int count=is.read(bytes, 0, buffer_size);
	              if(count==-1)
	                  break;
	              os.write(bytes, 0, count);
	            }
	        }
	        catch(Exception ex){}
	    }
	    
	    
}
