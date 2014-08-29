package ba.leftor.nwc;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class ActivityMap extends MapActivity {

	private ImageButton btnOverlaySwitch;
	private ImageButton btnOverlayLocate;
	private Button btnSetLocation;
	
	private MapView map;
	
	//my location
	private MyLocation myLocation;
	private Boolean gettingLocation=false;
	
	private String myLat;
	private String myLng;
	private String myAlt;
	private String premiseLat;
	private String premiseLng;
	private String premiseAlt;
	private int mapZoom;
	
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.mapselector);
		
		//find in layout
		btnOverlaySwitch=(ImageButton) findViewById(R.id.btnOverlaySwitch);
		btnOverlayLocate=(ImageButton) findViewById(R.id.btnOverlayLocate);
		btnSetLocation=(Button)findViewById(R.id.btnSetLocation);
		
		map=(MapView)findViewById(R.id.mapview);
		
		mapZoom=20;
		map.getController().setZoom(mapZoom);
	    map.setBuiltInZoomControls(false);
	    
	    Bundle b = getIntent().getExtras();
	    if(b != null) {
	    	premiseLat=b.getString("premiseLat", null);
	    	myLat=premiseLat;
	    	premiseLng=b.getString("premiseLng", null);
	    	myLng=premiseLng;
	    	premiseAlt=b.getString("premiseAlt", null);
	    	myAlt=premiseAlt;
	    	if(premiseLat != null && premiseLng != null && premiseAlt != null){
	    		setLocationMarker();
	    		Toast.makeText(this, this.getResources().getString(R.string.pin_info), Toast.LENGTH_LONG).show();
	    	}
	    }
	    
	    
	    
	    btnOverlayLocate.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				getGPSLocation();
				
			}
		});	    
	    
	    btnOverlaySwitch.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				map.setSatellite(!map.isSatellite());
				
			}
		});
	    
	    btnSetLocation.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				//check if location is set first!
				if(myLat==null || myLng==null || myAlt==null || premiseLat==null || premiseLng==null || premiseAlt==null){
					new AlertDialog.Builder(ActivityMap.this)
				    .setTitle(getResources().getString(R.string.error_location_not_set))
				    .setMessage(getResources().getString(R.string.please_click_get_location))
				    .setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int which) { 
				            dialog.dismiss();
				        }
				     })
				     .show();
				} else {
					Intent resultData = new Intent();
					resultData.putExtra("myLat", myLat);
					resultData.putExtra("myLng", myLng);
					resultData.putExtra("myAlt", myAlt);
					resultData.putExtra("premiseLat", premiseLat);
					resultData.putExtra("premiseLng", premiseLng);
					resultData.putExtra("premiseAlt", premiseAlt);
					setResult(Activity.RESULT_OK, resultData);
					finish();
				}
				
			}
		});
		
	}
	
	
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		myLat=savedInstanceState.getString("myLat", null);
		myLng=savedInstanceState.getString("myLng", null);
		myAlt=savedInstanceState.getString("myAlt", null);
		premiseLat=savedInstanceState.getString("premiseLat", null);
		premiseLng=savedInstanceState.getString("premiseLng", null);
		premiseAlt=savedInstanceState.getString("premiseAlt", null);
		mapZoom=savedInstanceState.getInt("mapZoom", 17);
		gettingLocation=savedInstanceState.getBoolean("gettingLocation");
		if(map!=null) map.getController().setZoom(mapZoom);
	}



	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if(myLat!=null)
			outState.putString("myLat", myLat);
		if(myLng!=null)
			outState.putString("myLng", myLng);
		if(myAlt!=null)
			outState.putString("myAlt", myAlt);
		if(premiseLat!=null)
			outState.putString("premiseLat", premiseLat);
		if(premiseLng!=null)
			outState.putString("premiseLng", premiseLng);
		if(premiseAlt!=null)
			outState.putString("premiseAlt", premiseAlt);
		
		outState.putInt("mapZoom", map.getZoomLevel());
		outState.putBoolean("gettingLocation", gettingLocation);
		super.onSaveInstanceState(outState);
	}



	//FUNCTION FOR ACQUIRING GPS LOCATION
	private void getGPSLocation(){
		MyLocation.LocationResult locationResult = new MyLocation.LocationResult(){
			@Override
			public void gotLocation(android.location.Location location,int provider) {
				GlobalFunctionsAndConstants.endLoading();
				
				Log.w("LOCATION LISTENER","GOT LOCATION RESPONSE!!!!");
				GlobalFunctionsAndConstants.endLoading();
				gettingLocation=false;
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
						Toast.makeText(getApplicationContext(), getResources().getString(R.string.location_acquired)+" "+loc.getProvider(), Toast.LENGTH_SHORT).show();
						String[] args={String.valueOf(loc.getLatitude()),String.valueOf(loc.getLongitude()),String.valueOf(loc.getAltitude())};
						myLat=String.valueOf(loc.getLatitude());
						myLng=String.valueOf(loc.getLongitude());
						myAlt=String.valueOf(loc.getAltitude());
						premiseLat=String.valueOf(loc.getLatitude());
						premiseLng=String.valueOf(loc.getLongitude());
						premiseAlt=String.valueOf(loc.getAltitude());
						
						setLocationMarker();
					}
				});
				
			}
		};
		myLocation=new MyLocation();
		gettingLocation=true;
		if(myLocation.getLocation(this, locationResult)==false){
			Toast.makeText(this, getResources().getString(R.string.location_service_unavailable), Toast.LENGTH_LONG).show();
			gettingLocation=false;
		} else 
			GlobalFunctionsAndConstants.startLoading(this, null, this.getResources().getString(R.string.getting_gps));

			
	}	
	
	private void setLocationMarker(){
		Drawable marker=getResources().getDrawable(R.drawable.marker);
	    
	    marker.setBounds(0, 0, marker.getIntrinsicWidth(),
	                            marker.getIntrinsicHeight());
	    
	    map.getOverlays().clear();
	    map.getOverlays().add(new SitesOverlay(marker));
	    
	    map.getController().setCenter(getPoint(Double.valueOf(premiseLat),
	    		Double.valueOf(premiseLng)));
	    
	}

	@Override
	  public void onResume() {
	    super.onResume();
	    //get gps position
	    if(myLat!=null){
	    	if(map!=null){
		    	map.getOverlays().clear();
		    	setLocationMarker();
		    	map.refreshDrawableState();
	    	}
	    }
	    if(gettingLocation==true)
	    	getGPSLocation();
	    
	  }  
	  
	  @Override
	  public void onPause() {
	    super.onPause();
	    if(myLocation!=null)
			myLocation.cancelUpdates();
	    GlobalFunctionsAndConstants.endLoading();
	  }  
	  
	  @Override
	  protected boolean isRouteDisplayed() {
	    return(false);
	  }
	  
	  @Override
	  public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_S) {
	      map.setSatellite(!map.isSatellite());
	      return(true);
	    }
	    else if (keyCode == KeyEvent.KEYCODE_Z) {
	      map.displayZoomControls(true);
	      return(true);
	    }
	    
	    return(super.onKeyDown(keyCode, event));
	  }

	  private GeoPoint getPoint(double lat, double lon) {
	    return(new GeoPoint((int)(lat*1000000.0),
	                          (int)(lon*1000000.0)));
	  }
	    
	  private class SitesOverlay extends ItemizedOverlay<OverlayItem> {
	    private List<OverlayItem> items=new ArrayList<OverlayItem>();
	    private Drawable marker=null;
	    private OverlayItem inDrag=null;
	    private ImageView dragImage=null;
	    private int xDragImageOffset=0;
	    private int yDragImageOffset=0;
	    private int xDragTouchOffset=0;
	    private int yDragTouchOffset=0;
	    
	    public SitesOverlay(Drawable marker) {
	      super(marker);
	      this.marker=marker;
	      
	      dragImage=(ImageView)findViewById(R.id.drag);
	      xDragImageOffset=dragImage.getDrawable().getIntrinsicWidth()/2;
	      yDragImageOffset=dragImage.getDrawable().getIntrinsicHeight();
	      
	      items.add(new OverlayItem(getPoint(Double.valueOf(premiseLat),
		    		Double.valueOf(premiseLng)),
	                                "My Location", "Premise Location"));

	      populate();
	    }
	    
	    @Override
	    protected OverlayItem createItem(int i) {
	      return(items.get(i));
	    }
	    
	    @Override
	    public void draw(Canvas canvas, MapView mapView,
	                      boolean shadow) {
	      super.draw(canvas, mapView, shadow);
	      
	      boundCenterBottom(marker);
	    }
	    
	    @Override
	    public int size() {
	      return(items.size());
	    }
	    
	    @Override
	    public boolean onTouchEvent(MotionEvent event, MapView mapView) {
	      final int action=event.getAction();
	      final int x=(int)event.getX();
	      final int y=(int)event.getY();
	      boolean result=false;
	      
	      if (action==MotionEvent.ACTION_DOWN) {
	        for (OverlayItem item : items) {
	          Point p=new Point(0,0);
	          
	          map.getProjection().toPixels(item.getPoint(), p);
	          
	          if (hitTest(item, marker, x-p.x, y-p.y)) {
	            result=true;
	            inDrag=item;
	            items.remove(inDrag);
	            populate();

	            xDragTouchOffset=0;
	            yDragTouchOffset=0;
	            
	            setDragImagePosition(p.x, p.y);
	            dragImage.setVisibility(View.VISIBLE);

	            xDragTouchOffset=x-p.x;
	            yDragTouchOffset=y-p.y;
	            
	            break;
	          }
	        }
	      }
	      else if (action==MotionEvent.ACTION_MOVE && inDrag!=null) {
	        setDragImagePosition(x, y);
	        result=true;
	      }
	      else if (action==MotionEvent.ACTION_UP && inDrag!=null) {
	        dragImage.setVisibility(View.GONE);
	        
	        GeoPoint pt=map.getProjection().fromPixels(x-xDragTouchOffset,
	                                                   y-yDragTouchOffset);
	        
	        premiseLat=String.valueOf(pt.getLatitudeE6()/1e6);
	        premiseLng=String.valueOf(pt.getLongitudeE6()/1e6);
	        
	        OverlayItem toDrop=new OverlayItem(pt, inDrag.getTitle(),
	                                           inDrag.getSnippet());
	        
	        items.add(toDrop);
	        populate();
	        
	        inDrag=null;
	        result=true;
	      }
	      
	      return(result || super.onTouchEvent(event, mapView));
	    }
	    
	    private void setDragImagePosition(int x, int y) {
	      RelativeLayout.LayoutParams lp=
	        (RelativeLayout.LayoutParams)dragImage.getLayoutParams();
	            
	      lp.setMargins(x-xDragImageOffset-xDragTouchOffset,
	                      y-yDragImageOffset-yDragTouchOffset, 0, 0);
	      dragImage.setLayoutParams(lp);
	    }
	  }

}
