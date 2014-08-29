package ba.leftor.nwc;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class ActivityRejectedMap extends MapActivity  {

	private MapView map;
	private int mapZoom;
	private String myLat;
	private String myLng;
	private String myAlt;
	
	
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.maprejected);
		
		Bundle extras = getIntent().getExtras();
		if( extras != null) {
			myLat = extras.getString("myLat", null);
			myLng = extras.getString("myLng", null);
			myAlt = extras.getString("myAlt", null);
		}
		
		map=(MapView)findViewById(R.id.mapview);
		
		mapZoom=17;
		map.getController().setZoom(mapZoom);
	    map.setBuiltInZoomControls(false);
	    
	    
	}
	
	
	private void setLocationMarker(){
		Drawable marker=getResources().getDrawable(R.drawable.marker);
	    
	    marker.setBounds(0, 0, marker.getIntrinsicWidth(),
	                            marker.getIntrinsicHeight());
	    
	    map.getOverlays().clear();
	    map.getOverlays().add(new SitesOverlay(marker));
	    
	    map.getController().setCenter(getPoint(Double.valueOf(myLat),
	    		Double.valueOf(myLng)));
	    
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
    
	}  
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		
		myLat=savedInstanceState.getString("myLat", null);
		myLng=savedInstanceState.getString("myLng", null);
		myAlt=savedInstanceState.getString("myAlt", null);
		mapZoom=savedInstanceState.getInt("mapZoom", 17);
		if(map!=null) map.getController().setZoom(mapZoom);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt("mapZoom", map.getZoomLevel());
		
		if(myLat!=null)
			outState.putString("myLat", myLat);
		if(myLng!=null)
			outState.putString("myLng", myLng);
		if(myAlt!=null)
			outState.putString("myAlt", myAlt);
		
		super.onSaveInstanceState(outState);
	}
	
	
	private GeoPoint getPoint(double lat, double lon) {
	    return(new GeoPoint((int)(lat*1000000.0),
	                          (int)(lon*1000000.0)));
	  }
	    
	  private class SitesOverlay extends ItemizedOverlay<OverlayItem> {
	    private List<OverlayItem> items=new ArrayList<OverlayItem>();
	    private Drawable marker=null;
	    
	    public SitesOverlay(Drawable marker) {
	      super(marker);
	      this.marker=marker;
	      
	      
	      items.add(new OverlayItem(getPoint(Double.valueOf(myLat),
		    		Double.valueOf(myLng)),
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
	  }
	
	
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

}
