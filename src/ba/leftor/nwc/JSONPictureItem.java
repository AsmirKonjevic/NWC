package ba.leftor.nwc;

import android.os.Parcel;
import android.os.Parcelable;

public class JSONPictureItem implements Parcelable {
	public int id;
	public String tag;
	public String path;
	public Boolean canDelete=false;
	
	public JSONPictureItem(){}

	
	public JSONPictureItem(Parcel in){
		readFromParcel(in);
	}
	
	public static final Parcelable.Creator<JSONPictureItem> CREATOR = new Parcelable.Creator<JSONPictureItem>() 
	{
	     public JSONPictureItem createFromParcel(Parcel in) 
	     {
	    	 return new JSONPictureItem(in);
	     }
	
	     public JSONPictureItem[] newArray (int size) 
	     {
	         return new JSONPictureItem[size];
	     }
	};
	
	public int describeContents ()
	{
	   return 0;
	}
	
	public void writeToParcel (Parcel dest, int flags)
	{
	    dest.writeInt(id);
	    dest.writeString (tag);
	    dest.writeString (path);
	    if(!canDelete)
	    	dest.writeInt(0);
	    else 
	    	dest.writeInt(1);
	}
	
	private void readFromParcel(Parcel in) {
		id= in.readInt();
		tag = in.readString();
		path = in.readString();
		int canDel = in.readInt();
		if(canDel==0)
			canDelete=false;
		else
			canDelete=true;
		
	}
	
	
	
	
	@Override
	public boolean equals(Object o) {
		JSONPictureItem tmp=(JSONPictureItem) o;
		if(this.getId()==tmp.getId() || this.getTag()==tmp.getTag())
			return true;
		else 
			return false;
	}


	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Boolean getCanDelete() {
		return canDelete;
	}

	public void setCanDelete(Boolean canDelete) {
		this.canDelete = canDelete;
	}
	
}
