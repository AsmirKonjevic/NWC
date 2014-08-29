package ba.leftor.nwc;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;

public class ActivityShowPhoto extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_photo);
		
		Bundle b=getIntent().getExtras();
		String photo=b.getString("photo", null);
		if (photo != null) {
			ImageView imgPhoto=(ImageView)findViewById(R.id.imgPhoto);
			ImageLoader imgLoader=new ImageLoader(this);
			imgLoader.DisplayImage(photo, imgPhoto, true);
		}
	}
	
}
