package ba.leftor.nwc;

import android.os.Parcel;
import android.os.Parcelable;

public class JSONRejectedPremiseItem implements Parcelable {
	public int id;
	public String gps_latitude;
	public String gps_longitude;
	public String gps_altitude;

	public String premise_name;
	public String house_connection_number;
	public String water_meter_number;
	public String stc_db_number;
	public String sec_db_number;
	public String first_validation_comment;
	public String premise_type;
	public String district;
	public String full_name;
	public String photo_house;
	public String photo_house_2;
	public String photo_house_3;
	public int district_id;
	public int user_id;
	public int premise_group_id;
	
	public JSONRejectedPremiseItem(){}
	
	public JSONRejectedPremiseItem(Parcel in){
		readFromParcel(in);
	}
	
	public static final Parcelable.Creator<JSONRejectedPremiseItem> CREATOR = new Parcelable.Creator<JSONRejectedPremiseItem>() 
	{
	     public JSONRejectedPremiseItem createFromParcel(Parcel in) 
	     {
	    	 return new JSONRejectedPremiseItem(in);
	     }
	
	     public JSONRejectedPremiseItem[] newArray (int size) 
	     {
	         return new JSONRejectedPremiseItem[size];
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
	    dest.writeString (water_meter_number);
	    dest.writeString (stc_db_number);
	    dest.writeString (sec_db_number);
	    
	    dest.writeString (first_validation_comment);
	    dest.writeString (premise_type);
	    dest.writeString (district);
	    dest.writeString (full_name);
	    dest.writeString (photo_house);
	    dest.writeString (photo_house_2);
	    dest.writeString (photo_house_3);
	    dest.writeInt (user_id);
	    dest.writeString (premise_name);
	    dest.writeInt (premise_group_id);
	    dest.writeInt (district_id);
	    
	}
	
	private void readFromParcel(Parcel in) {
		id= in.readInt();
		gps_latitude = in.readString();
		gps_longitude = in.readString();
		gps_altitude = in.readString();
		house_connection_number = in.readString();
		water_meter_number = in.readString();
		stc_db_number = in.readString();
		sec_db_number = in.readString();
		first_validation_comment = in.readString();
		premise_type = in.readString();
		district = in.readString();
		full_name = in.readString();
		photo_house = in.readString();
		photo_house_2 = in.readString();
		photo_house_3 = in.readString();
		user_id = in.readInt();
		premise_name = in.readString();
		premise_group_id = in.readInt();
		district_id = in.readInt();
	}
	
	
	@Override
	public boolean equals(Object o) {
		JSONRejectedPremiseItem other=(JSONRejectedPremiseItem) o;
		if(other.house_connection_number == this.house_connection_number || other.id == this.id)
			return true;
		else
			return false;
	}

}

