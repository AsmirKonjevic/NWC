package ba.leftor.nwc;

import android.os.Parcel;
import android.os.Parcelable;

public class JSONRejectedCustomerItem implements Parcelable {
	public int id;
	public String gps_latitude;
	public String gps_longitude;
	public String gps_altitude;
	public String house_connection_number;
	public String first_validation_comment;
	public String full_name;
	public String po_box;
	public String zip;
	public String contact_name;
	public String contact_street_name;
	public String contact_po_box;
	public String contact_zip_code;
	
	public JSONRejectedCustomerItem(){}
	
	public JSONRejectedCustomerItem(Parcel in){
		readFromParcel(in);
	}
	
	public static final Parcelable.Creator<JSONRejectedCustomerItem> CREATOR = new Parcelable.Creator<JSONRejectedCustomerItem>() 
	{
	     public JSONRejectedCustomerItem createFromParcel(Parcel in) 
	     {
	    	 return new JSONRejectedCustomerItem(in);
	     }
	
	     public JSONRejectedCustomerItem[] newArray (int size) 
	     {
	         return new JSONRejectedCustomerItem[size];
	     }
	};
	
	public int describeContents ()
	{
	   return 0;
	}
	
	public void writeToParcel (Parcel dest, int flags)
	{
	    dest.writeInt(id);
	    dest.writeString (gps_latitude);
	    dest.writeString (gps_longitude);
	    dest.writeString (gps_altitude);
	    dest.writeString (house_connection_number);
	    dest.writeString (first_validation_comment);
	    dest.writeString (full_name);
	    dest.writeString (po_box);
	    dest.writeString (zip);
	    dest.writeString (contact_name);
	    dest.writeString (contact_street_name);
	    dest.writeString (contact_po_box);
	    dest.writeString (contact_zip_code);
	}
	
	private void readFromParcel(Parcel in) {
		id= in.readInt();
		gps_latitude = in.readString();
		gps_longitude = in.readString();
		gps_altitude = in.readString();
		house_connection_number = in.readString();
		first_validation_comment = in.readString();
		full_name = in.readString();
		po_box = in.readString();
		zip = in.readString();
		contact_name = in.readString();
		contact_street_name = in.readString();
		contact_po_box = in.readString();
		contact_zip_code = in.readString();
	}
	
	
	@Override
	public boolean equals(Object o) {
		JSONRejectedCustomerItem other=(JSONRejectedCustomerItem) o;
		if(other.house_connection_number == this.house_connection_number || other.id == this.id)
			return true;
		else
			return false;
	}

}

