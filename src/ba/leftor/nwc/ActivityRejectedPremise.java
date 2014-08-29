package ba.leftor.nwc;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import ba.leftor.nwc.CustomMultiPartEntity.ProgressListener;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ActivityRejectedPremise extends Activity{
	
	//definiši listview i adapter
	private ListView lstRejected;
	private ArrayList<JSONRejectedPremiseItem> lista;
	private PremiseAdapter adapter;
	
	//definiši varijable start i count za listanje (limit start i limit end)
	private int start=0;
	private static final int count=20;//ucitavat cemo uvijek po 20
	private GetRejectedPremises listingThread=null;
	private ProgressDialog dialog;
	private int listPosition=0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rejected_premise_form);
		
		// display user name on top
		TextView txtWelcome = (TextView) findViewById(R.id.txtWelcome);
		JSONUSerItem user = GlobalFunctionsAndConstants.getUser(this);
		if (user.fullName.length() > 1)
			txtWelcome.setText(ActivityRejectedPremise.this.getResources().getString(R.string.welcome) + user.fullName);
		else
			txtWelcome.setText(ActivityRejectedPremise.this.getResources().getString(R.string.welcome) + user.username);
		
		
		lstRejected=(ListView)findViewById(R.id.lstRejected);
		
		lista = new ArrayList<JSONRejectedPremiseItem>();
		
		
		///iluzija beskonacne liste
        lstRejected.setOnScrollListener(new OnScrollListener() {
			
   			//mora se implementirat al ne treba
           	public void onScrollStateChanged(AbsListView view, int scrollState) {				
   			}
   			
           	//eh ovdje je magija za iluziju beskonacne liste :)
   			public void onScroll(AbsListView view, int firstVisibleItem,
   					int visibleItemCount, int totalItemCount) {
   				
   				
   				//ako je zadnji element liste vidljiv onda ucitaj sljedecih 20
   				if(((firstVisibleItem + visibleItemCount) == totalItemCount)) Log.w("Kraj liste","SCROLL KRAJ NEXT");
   				if(((firstVisibleItem + visibleItemCount) == totalItemCount) && (totalItemCount % count == 0 && totalItemCount != 0) 
   						&& (listingThread != null && listingThread.getStatus()!=Status.RUNNING))
   				{
   					listingThread=new GetRejectedPremises(ActivityRejectedPremise.this);
   					dialog = new ProgressDialog(ActivityRejectedPremise.this);
   			        dialog.setMessage(getResources().getString(R.string.sending));
   			        dialog.setIndeterminate(false);
   			        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
   			        dialog.setProgress(0);
   			        dialog.show();
   					listingThread.execute();
   					Log.w("Sljedecih", "20");
   				} 
   				
   			}
   		});
	}
	
	
	
	@Override
	protected void onResume() {
		super.onResume();
		//ako je bila prije ucitana lista
		if(lista != null && lista.size()>0){
			if(adapter==null) {
				adapter=new PremiseAdapter(this, lista);
				lstRejected.setAdapter(adapter);
			} else {
				adapter.notifyDataSetChanged();
			}
			lstRejected.setSelectionFromTop(listPosition, 0);
		}
		
		//rotation aware task
		try {
			listingThread=(GetRejectedPremises)getLastNonConfigurationInstance();
		} catch (Exception e) {
		}
		if(listingThread!=null){
			dialog = new ProgressDialog(this);
            dialog.setMessage(getResources().getString(R.string.sending));
            dialog.setIndeterminate(false);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setProgress(0);
            dialog.show();
            listingThread.attach(this);
			
            if (listingThread.response_str!=null) {
				fillList();
			}
		} else {
			listingThread=new GetRejectedPremises(this);
			if(lista==null || lista.size()<1) {
				dialog = new ProgressDialog(this);
		        dialog.setMessage(getResources().getString(R.string.sending));
		        dialog.setIndeterminate(false);
		        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		        dialog.setProgress(0);
		        dialog.show();
				listingThread.execute();
			}
			
		}
	}



	/** GetRejectedPremises ASYNC TASK **/
    static class GetRejectedPremises extends AsyncTask<Void, Integer, Void> {

    	private ActivityRejectedPremise activity=null;
        private String response_str=null;

        GetRejectedPremises(ActivityRejectedPremise activity) {
            attach(activity);
        }
        void detach() {
        	activity=null;
        }
          
        void attach(ActivityRejectedPremise activity) {
        	this.activity=activity;
        }

        @Override
        protected Void doInBackground(Void... arg) {
        	
            try {
            	response_str=null;
            	//search premise item
            	String urlString = activity.getResources().getString(R.string.url_rejected_premises);
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
                
                reqEntity.addPart("start", new StringBody(String.valueOf(activity.start)));
                reqEntity.addPart("count", new StringBody(String.valueOf(activity.count)));
                
                //get sarch fields and add to req entity
//                String hcn=this.activity.txtSearchHcn.getText().toString().trim();
//				String wmn=this.activity.txtSearchWaterMeterNumber.getText().toString().trim();
//				
//                if(hcn.length()>0){
//                	reqEntity.addPart("house_connection_number", new StringBody(hcn));
//                }
//                if(wmn.length()>0){
//                	reqEntity.addPart("water_meter_number", new StringBody(wmn));
//                }
                
                
                post.setEntity(reqEntity);
                HttpResponse response = client.execute(post);
                resEntity = response.getEntity();
                response_str = EntityUtils.toString(resEntity);
                
            } catch (Exception e) {
                Log.w("REJECTED PREMISE LIST CONN ERROR","ERROR",e);
                response_str="ERROR: " + activity.getResources().getString(R.string.require_connection);
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void result) {
            if(activity!=null){
	        	try {
	                activity.fillList();
	            } catch(Exception e) {
	            }
            }

        }

    }
    
    /**
     * Fill list when async task completes getting rejected premisses
     */
    private void fillList(){
    	if(dialog!=null)
    		dialog.dismiss();
    	if(listingThread==null) return;
    	String response_str=listingThread.response_str;
    	
    	//provjeri response
    	if(response_str != null) {
    		if(response_str.contains("ERROR")) {
        		Log.w("REJECTED PREMISE LIST ERROR","LIST ERROR:"+response_str);
        		Toast.makeText(this, response_str, Toast.LENGTH_LONG).show();
        		return;
        	} else {
        		Gson gson=new Gson();
        		java.lang.reflect.Type type = new TypeToken<List<JSONRejectedPremiseItem>>(){}.getType();
        		List<JSONRejectedPremiseItem> response = null;
        		try {
                    response = gson.fromJson(response_str,type);
                } catch (Exception e) {
                    Log.w(getClass().getSimpleName(), "GRESKA: " + e.getMessage() + " -ne mogu parsirati json listu rejected premisa");
                    return;
                }
        		Log.w("RESPONSE", response_str);
        		if (response != null) {
        			
            		for (JSONRejectedPremiseItem rp : response) {
						lista.add(rp);
					}
        			start+=response.size();
        			if(adapter==null) {
        				adapter=new PremiseAdapter(this, lista);
        				lstRejected.setAdapter(adapter);
        			} else {
        				adapter.notifyDataSetChanged();
        			}
        		}
			}
    	}
    }
	
	
	/**
	 * Adapter klasa za listu rejected premisa
	 * @author Semir
	 *
	 */
	 class PremiseAdapter extends BaseAdapter{
			private Context context;
			private List<JSONRejectedPremiseItem> data;
			private LayoutInflater inflater;
			private Button showMore;

			public PremiseAdapter(Context c,List<JSONRejectedPremiseItem> ls){
				context=c;
				data=ls;
				inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				
			}
			
			public int getCount() {
				return data.size();
			}

			public Object getItem(int arg0) {
				return data.get(arg0);
			}

			public long getItemId(int arg0) {
				return data.get(arg0).id;
			}

			public View getView(int position, View convertView, ViewGroup parent) {
		        
				JSONRejectedPremiseItem tmp=data.get(position);
				
				View vi=convertView;
		        if(convertView==null)
		            vi = inflater.inflate(R.layout.rejected_premise_list_item, null);
		               
		        TextView txtFullName=(TextView)vi.findViewById(R.id.txtFullName);
		        TextView txtDistrict=(TextView)vi.findViewById(R.id.txtDistrict);
		        TextView txtHcn=(TextView)vi.findViewById(R.id.txtHcn);
		        TextView txtType=(TextView)vi.findViewById(R.id.txtType);
		        TextView txtWmn=(TextView)vi.findViewById(R.id.txtWmn);
		        TextView txtReason=(TextView)vi.findViewById(R.id.txtReason);
		        Button btnEdit=(Button)vi.findViewById(R.id.btnEdit);
		        Button btnMap=(Button)vi.findViewById(R.id.btnMap);
		        LinearLayout lnFullName=(LinearLayout)vi.findViewById(R.id.lnFullName);
		        LinearLayout lnDistrict=(LinearLayout)vi.findViewById(R.id.lnDistrict);
		        LinearLayout lnHcn=(LinearLayout)vi.findViewById(R.id.lnHcn);
		        LinearLayout lnWmn=(LinearLayout)vi.findViewById(R.id.lnWmn);
		        LinearLayout lnType=(LinearLayout)vi.findViewById(R.id.lnType);
		        LinearLayout lnReason=(LinearLayout)vi.findViewById(R.id.lnReason);
		        
		        if(tmp.full_name == null )
		        	lnFullName.setVisibility(View.GONE);
		        else {
		        	txtFullName.setText(tmp.full_name);
		        	lnFullName.setVisibility(View.VISIBLE);
		        }
		        
		        if(tmp.district == null )
		        	lnDistrict.setVisibility(View.GONE);
		        else {
		        	txtDistrict.setText(tmp.district);
		        	lnDistrict.setVisibility(View.VISIBLE);
		        }
		        
		        if(tmp.house_connection_number == null )
		        	lnHcn.setVisibility(View.GONE);
		        else {
		        	txtHcn.setText(tmp.house_connection_number);
		        	lnHcn.setVisibility(View.VISIBLE);
		        }
		        
		        if(tmp.premise_type == null )
		        	lnType.setVisibility(View.GONE);
		        else {
		        	txtType.setText(tmp.premise_type);
		        	lnType.setVisibility(View.VISIBLE);
		        }
		        
		        if(tmp.water_meter_number == null )
		        	lnWmn.setVisibility(View.GONE);
		        else {
		        	txtWmn.setText(tmp.water_meter_number);
		        	lnWmn.setVisibility(View.VISIBLE);
		        }
		        
		        if(tmp.first_validation_comment == null )
		        	lnReason.setVisibility(View.GONE);
		        else {
		        	txtReason.setText(tmp.first_validation_comment);
		        	lnReason.setVisibility(View.VISIBLE);
		        }
		        
		        final int idRejected=tmp.id;
		        final String hcn=tmp.house_connection_number;
		        final String rejectedLat=tmp.gps_latitude;
		        final String rejectedLng=tmp.gps_longitude;
		        final String rejectedAlt=tmp.gps_altitude;
		        
		        btnEdit.setOnClickListener(new OnClickListener() {
					
					public void onClick(View v) {
						Intent i=new Intent(ActivityRejectedPremise.this, ActivityPremise.class);
						i.putExtra("rejectedId", idRejected);
						i.putExtra("rejectedHcn", hcn);
						startActivityForResult(i, 1);
					}
				});
		        
		        btnMap.setOnClickListener(new OnClickListener() {
					
					public void onClick(View arg0) {
						Intent i=new Intent(ActivityRejectedPremise.this, ActivityRejectedMap.class);
						i.putExtra("myLat", rejectedLat);
						i.putExtra("myLng", rejectedLng);
						i.putExtra("myAlt", rejectedAlt);
						startActivity(i);
					}
				});
		        
		        
		        
				return vi;
			}
	 }
	
	@Override
	protected void onStop() {
		super.onStop();
		Log.w("STOPING","STOPING AND SAVING USER!!!");
		GlobalFunctionsAndConstants.savePrefs(this);
	} 
	@Override
	public Object onRetainNonConfigurationInstance() {
	    if(listingThread!=null){
	    	listingThread.detach();
	    	return(listingThread);
	    }
	    return null;
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if(lista!=null){
			outState.putParcelableArrayList("lista", (ArrayList<? extends Parcelable>) lista);
		}
		outState.putInt("start", start);
		outState.putInt("position", lstRejected.getFirstVisiblePosition());
		super.onSaveInstanceState(outState);
	}
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		lista=savedInstanceState.getParcelableArrayList("lista");
		start=savedInstanceState.getInt("start");
		listPosition=savedInstanceState.getInt("position");
		super.onRestoreInstanceState(savedInstanceState);
	}



	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 1){
			if (resultCode == RESULT_OK) {
				Log.w("RESULT FROM INTENT","IZMJENJEN PROBAJ UZET ID");
				int id=data.getIntExtra("rejectedId", 0);
				if(id>0){
					Log.w("VRACEN REJECTED","ID:"+id);
					JSONRejectedPremiseItem tmp=new JSONRejectedPremiseItem();
					tmp.id=id;
					if(lista!=null){
						lista.remove(tmp);
						adapter.notifyDataSetChanged();
					}
				}
			}
		}
	}
	
	
}
